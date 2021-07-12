package com.calibre.subscriber.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskExecutorConfigTest {
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(TaskExecutorConfig.class).withSystemProperties("spring.profiles.active=test");

    @Test
    public void should_check_presence_of_executor() {
        context.run(it -> {
            assertThat(it).hasBean("email-service-taskExecutor");
            assertThat(it).hasBean("csv-service-taskExecutor");
        });
    }
}
