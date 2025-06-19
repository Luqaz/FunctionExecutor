package com.example.functionexecutor.entity;

import javax.script.*;

public class JSFunction implements ExecutableFunction {
    private final Invocable function;

    public JSFunction(String script) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(script);
        function = (Invocable) engine;
    }

    @Override
    public Double execute(Integer value) throws Exception {
        double randomTime = Math.random() * 1000;
        double randomNumberOfTimes = Math.random() * value;
        int randomWait = (int) randomTime * (int) randomNumberOfTimes;
        Thread.sleep(randomWait);
        Number result = (Number) function.invokeFunction("execute", value);
        return result.doubleValue();
    }
}
