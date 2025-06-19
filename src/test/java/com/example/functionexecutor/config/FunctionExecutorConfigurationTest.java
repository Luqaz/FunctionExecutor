package com.example.functionexecutor.config;

import com.example.functionexecutor.entity.ExecutableFunction;
import com.example.functionexecutor.repository.FunctionHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FunctionExecutorConfiguration.class)
class FunctionExecutorConfigurationTest {
    @Autowired
    ApplicationContext context;

    @Test
    void testBeansAreLoaded() {
        assertThat(context.getBean(FunctionHolder.class)).isNotNull();
    }

    @Test
    void testFunctionsParsed() {
        FunctionHolder holder = context.getBean(FunctionHolder.class);
        assertThat(holder.getFunctions()).isNotNull();
        assertThat(holder.getFunctions().size()).isGreaterThan(0);
        assertThat(holder.getInterval()).isNotEqualTo(0);
        for(ExecutableFunction function : holder.getFunctions()) {
            assertThat(function).isNotNull();
        }
    }
}
