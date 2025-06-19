package com.example.functionexecutor.service;

import com.example.functionexecutor.entity.ExecutableFunction;
import com.example.functionexecutor.repository.FunctionHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FunctionExecutorServiceTest {

    private FunctionExecutorService service;

    @BeforeEach
    void init() throws Exception {
        FunctionHolder mockHolder = mock(FunctionHolder.class);

        ExecutableFunction f1 = mock(ExecutableFunction.class);
        ExecutableFunction f2 = mock(ExecutableFunction.class);
        when(f1.execute(0)).thenReturn(1.0);
        when(f2.execute(0)).thenReturn(2.0);

        when(mockHolder.getFunctions()).thenReturn(List.of(f1, f2));
        when(mockHolder.getInterval()).thenReturn(100);
        service = new FunctionExecutorService(mockHolder);
    }

    @Test
    void testOrderedExecution() {
        Flux<String> result = service.execute(1, true);

        StepVerifier.create(result)
                .assertNext(el -> {
                    String[] rowParts = el.split(",");
                    assertThat(rowParts.length).isEqualTo(7);
                    assertThat(rowParts[0]).isEqualTo("0");
                    assertThat(rowParts[1]).isEqualTo("1.00");
                    assertThat(rowParts[3]).isEqualTo("1");
                    assertThat(rowParts[4]).isEqualTo("2.00");
                    assertThat(rowParts[6]).isEqualTo("1");
                })
                .verifyComplete();
    }

    @Test
    void testNonOrderedExecution() {
        Flux<String> result = service.execute(1, false);

        StepVerifier.create(result)
                .assertNext(el -> {
                    String[] rowParts = el.split(",");
                    assertThat(rowParts.length).isEqualTo(4);
                    assertThat(rowParts[0]).isEqualTo("0");
                    assertThat(rowParts[1]).isEqualTo("0");
                    assertThat(rowParts[2]).isEqualTo("1.00");
                })
                .assertNext(el -> {
                    String[] rowParts = el.split(",");
                    assertThat(rowParts.length).isEqualTo(4);
                    assertThat(rowParts[0]).isEqualTo("0");
                    assertThat(rowParts[1]).isEqualTo("1");
                    assertThat(rowParts[2]).isEqualTo("2.00");
                })
                .verifyComplete();
    }
}
