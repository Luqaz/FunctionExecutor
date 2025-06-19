package com.example.functionexecutor.config;

import com.example.functionexecutor.FunctionExecutorApplication;
import com.example.functionexecutor.repository.FunctionFactory;
import com.example.functionexecutor.entity.ExecutableFunction;
import com.example.functionexecutor.repository.FunctionHolder;
import com.example.functionexecutor.entity.FunctionLang;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Configuration
public class FunctionExecutorConfiguration {

    @Bean
    public FunctionHolder jsonConfig() throws Exception {
        List<ExecutableFunction> functionList = new LinkedList<>();
        int interval = 0;
        ObjectMapper mapper = new ObjectMapper();
        try(InputStream is = FunctionExecutorApplication.class.getClassLoader().getResourceAsStream("data/config.json")) {
            if(is == null) {
                throw new IllegalStateException("Missing config.json in resources/data");
            }

            JsonNode root = mapper.readTree(is);
            for(var it = root.fieldNames(); it.hasNext();) {
                String key = it.next();
                JsonNode node = root.get(key);
                if(key.contains("function")) {
                    String scriptText = node.asText();
                    FunctionLang lang = detectLang(scriptText);
                    scriptText = normalizeScript(scriptText, lang);
                    functionList.add(FunctionFactory.from(scriptText, lang));
                } else if(key.contains("interval")) {
                    interval = node.asInt(0);
                }
            }
        }
        return new FunctionHolder(functionList, interval);
    }

    private FunctionLang detectLang(String script) {
        if (script.contains("function")) return FunctionLang.JS;
        if (script.contains("lambda")) return FunctionLang.PYTHON;
        return FunctionLang.UNKNOWN;
    }

    private String normalizeScript(String raw, FunctionLang lang) {
        return switch (lang) {
            case JS -> "var execute = " + raw;
            case PYTHON -> "execute = " + raw;
            default -> raw;
        };
    }
}
