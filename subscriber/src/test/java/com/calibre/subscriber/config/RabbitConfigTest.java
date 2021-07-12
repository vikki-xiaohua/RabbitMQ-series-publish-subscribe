package com.calibre.subscriber.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class RabbitConfigTest {
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(RabbitConfig.class).withSystemProperties("spring.profiles.active=test");

    @Test
    public void should_check_presence_of_beans() {
        context.run(it -> {
            assertThat(it).hasSingleBean(CachingConnectionFactory.class);
            assertThat(it).hasSingleBean(MessageHandlerMethodFactory.class);
            assertThat(it).hasSingleBean(MappingJackson2MessageConverter.class);
            assertThat(it).hasSingleBean(ObjectMapper.class);
            assertThat(it).hasBean("queue");
            assertThat(it).hasBean("exchange");
            assertThat(it).hasBean("binding");

            assertThat(it).hasBean("batchQueue");
            assertThat(it).hasBean("batchBinding");

            assertThat(it).hasBean("errorExchange");
            assertThat(it).hasBean("errorQueue");
            assertThat(it).hasBean("errorBinding");

            assertThat(it).hasBean("errorBatchQueue");
            assertThat(it).hasBean("errorBatchBinding");
        });
    }
}
