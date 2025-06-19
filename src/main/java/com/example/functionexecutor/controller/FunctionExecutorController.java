package com.example.functionexecutor.controller;

import com.example.functionexecutor.service.FunctionExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

@RestController
@RequestMapping("/api")
public class FunctionExecutorController {
    @Autowired
    FunctionExecutorService service;

    @GetMapping(value = "/calculate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> calculate(@RequestParam int count, @RequestParam boolean ordered) {
        return service.execute(count, ordered);
    }
}
