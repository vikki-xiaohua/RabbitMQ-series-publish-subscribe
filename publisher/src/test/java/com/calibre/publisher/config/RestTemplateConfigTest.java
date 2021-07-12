package com.calibre.publisher.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RestTemplateConfigTest {
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(RestTemplateConfig.class).withSystemProperties("spring.profiles.active=test");

    @Test
    public void should_check_presence_of_beans() {
        context.run(it -> {
            assertThat(it).hasSingleBean(RestTemplateBuilder.class);
            assertThat(it).hasSingleBean(RestTemplate.class);
        });
    }
}
