package com.example.functionexecutor.repository;

import com.example.functionexecutor.entity.ExecutableFunction;
import com.example.functionexecutor.entity.FunctionLang;
import com.example.functionexecutor.entity.JSFunction;
import com.example.functionexecutor.entity.PythonFunction;

public class FunctionFactory {
    public static ExecutableFunction from (String script, FunctionLang lang) throws Exception {
        return switch (lang) {
            case JS -> new JSFunction(script);
            case PYTHON -> new PythonFunction(script);
            default -> throw new IllegalArgumentException("Unknown function language - " + lang.name());
        };
    }
}
