package com.calibre.publisher.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class CallbackServiceTest {

    @Test
    public void should_check_presence_of_beans() {
        ApplicationContextRunner context_confirm = new ApplicationContextRunner()
                .withUserConfiguration(ConfirmCallbackService.class).withSystemProperties("spring.profiles.active=test");

        context_confirm.run(it -> {
            assertThat(it).hasSingleBean(ConfirmCallbackService.class);

        });

        ApplicationContextRunner context_return = new ApplicationContextRunner()
                .withUserConfiguration(ReturnCallbackService.class).withSystemProperties("spring.profiles.active=test");

        context_return.run(it -> {
            assertThat(it).hasSingleBean(ReturnCallbackService.class);

        });

    }
}