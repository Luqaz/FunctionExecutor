package com.example.functionexecutor.entity;

import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonFunction implements ExecutableFunction {
    private final PyFunction function;

    public PythonFunction(String script) {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            interpreter.exec(script);
            this.function = interpreter.get("execute", PyFunction.class);
        }
    }

    @Override
    public Double execute(Integer value) throws Exception {
        PyObject result = function.__call__(new PyInteger(value));
        return result.asDouble();
    }
}
