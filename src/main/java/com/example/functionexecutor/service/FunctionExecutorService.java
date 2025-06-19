package com.example.functionexecutor.service;

import com.example.functionexecutor.entity.ExecutableFunction;
import com.example.functionexecutor.repository.FunctionHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class FunctionExecutorService {
    private final FunctionHolder holder;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Flux<String> execute(int count, boolean ordered) {
        return createExecutionFlow(count, !ordered, ordered ? this::combineResultsSynchronously : this::combineResultsAsynchronously);
    }

    private Flux<String> createExecutionFlow(int count, boolean includeFnIndex, Function<List<Mono<String>>, Flux<String>> combiner) {
        int fnCount = holder.getFunctions().size();
        List<Scheduler> schedulers = createSchedulers(fnCount);
        List<AtomicInteger> buffers = createBuffers(fnCount);
        return Flux.interval(Duration.ofMillis(holder.getInterval()))
                .take(count)
                .onBackpressureBuffer(Duration.ofSeconds(10), count, (el) -> System.out.println("Dropped tick " + el))
                .flatMap(counter -> {
                    List<Mono<String>> executions = new ArrayList<>(fnCount);
                    var schedulerIter = schedulers.iterator();
                    var bufferIter = buffers.iterator();

                    int index = 0;
                    for (var function : holder.getFunctions()) {
                        executions.add(
                                createExecutionMono(
                                        index++,
                                        function,
                                        counter.intValue(),
                                        bufferIter.next(),
                                        schedulerIter.next(),
                                        includeFnIndex
                                )
                        );
                    }

                    return combiner.apply(executions).map(el -> String.format("%d,%s", counter, el));
                });
    }

    private Mono<String> createExecutionMono(int fnIndex, ExecutableFunction function, int input, AtomicInteger buffer, Scheduler scheduler, boolean includeFnIndex) {
        return Mono.fromCallable(() -> {
                    long start = System.nanoTime();
                    double result = function.execute(input);
                    long duration = (System.nanoTime() - start) / 1000;
                    String formattedResult = String.format("%.2f,%d", result, duration);
                    if (includeFnIndex) {
                        formattedResult = String.format("%d,%s", fnIndex, formattedResult);
                    } else {
                        formattedResult = String.format("%s,%d", formattedResult, buffer.get());
                    }
                    return formattedResult;
                })
                .subscribeOn(scheduler)
                .doOnSubscribe(_ -> buffer.incrementAndGet())
                .doFinally(_ -> buffer.decrementAndGet());
    }

    private Flux<String> combineResultsSynchronously(List<Mono<String>> results) {
        Mono<String> combined = Mono.just("");

        for (Mono<String> result : results) {
            combined = combined.zipWith(result)
                    .map(tuple -> {
                        if (!tuple.getT1().isBlank()) {
                            return String.join(",", tuple.getT1(), tuple.getT2());
                        } else {
                            return tuple.getT2();
                        }
                    });
        }

        return combined.flux();
    }

    private Flux<String> combineResultsAsynchronously(List<Mono<String>> results) {
        return Flux.merge(results);
    }

    private List<Scheduler> createSchedulers(int count) {
        List<Scheduler> list = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            list.add(Schedulers.fromExecutor(threadPool));
        }
        return list;
    }

    private List<AtomicInteger> createBuffers(int count) {
        final List<AtomicInteger> bufferList = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            bufferList.add(new AtomicInteger(0));
        }
        return bufferList;
    }
}
