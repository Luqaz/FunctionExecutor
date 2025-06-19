package com.example.functionexecutor.repository;

import com.example.functionexecutor.entity.ExecutableFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FunctionHolder {
    private List<ExecutableFunction> functions;
    private Integer interval;
}
