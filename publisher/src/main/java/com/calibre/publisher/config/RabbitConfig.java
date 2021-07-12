package com.calibre.publisher.config;

import com.calibre.publisher.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitConfig {
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    private ConfirmCallbackService confirmCallbackService;
    private ReturnCallbackService returnCallbackService;

    @Autowired
    public RabbitConfig(ConfirmCallbackService confirmCallbackService, ReturnCallbackService returnCallbackService) {
        this.confirmCallbackService = confirmCallbackService;
        this.returnCallbackService = returnCallbackService;
    }

    @Bean(name = "connectionFactory")
    CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitHost);
        cachingConnectionFactory.setUsername(rabbitUsername);
        cachingConnectionFactory.setPassword(rabbitPassword);
        cachingConnectionFactory.setConnectionTimeout(15000);
        cachingConnectionFactory.setPublisherReturns(true);
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        return cachingConnectionFactory;
    }

    @Bean(name = "rabbitTemplate")
    @Primary
    public RabbitTemplate rabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(Constants.TOPIC_EXCHANGE_FX_RATE_API);
        rabbitTemplate.setEncoding(StandardCharsets.UTF_8.name());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        ThreadPoolTaskScheduler taskExecutor = new ThreadPoolTaskScheduler();
        taskExecutor.setPoolSize(2);
        rabbitTemplate.setTaskExecutor(taskExecutor);

        rabbitTemplate.setConfirmCallback(confirmCallbackService);
        rabbitTemplate.setReturnsCallback(returnCallbackService);

        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8.name());
        return converter;
    }

    @Bean(name = "queue")
    Queue queue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", Constants.ERROR_EXCHANGE);
        params.put("x-dead-letter-routing-key", Constants.ERROR_ROUTING_KEY);

        return QueueBuilder.durable(Constants.TOPIC_QUEUE_FX_RATE_API).withArguments(params).build();
    }

    @Bean(name = "exchange")
    TopicExchange exchange() {
        return new TopicExchange(Constants.TOPIC_EXCHANGE_FX_RATE_API);
    }

    @Bean(name = "binding")
    Binding binding(@Qualifier("queue") Queue queue, @Qualifier("exchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constants.TOPIC_FX_RATE_API_ROUTING_KEY);
    }

    @Bean(name = "batchQueue")
    Queue batchQueue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", Constants.ERROR_EXCHANGE);
        params.put("x-dead-letter-routing-key", Constants.ERROR_BATCH_ROUTING_KEY);

        return QueueBuilder.durable(Constants.TOPIC_BATCH_QUEUE_FX_RATE_API).withArguments(params).build();
    }

    @Bean(name = "batchBinding")
    Binding batchBinding(@Qualifier("batchQueue") Queue queue, @Qualifier("exchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constants.TOPIC_BATCH_FX_RATE_API_ROUTING_KEY);
    }

    @Bean(name = "errorExchange")
    public TopicExchange errorExchange() {
        return new TopicExchange(Constants.ERROR_EXCHANGE, true, false);
    }

    @Bean(name = "errorQueue")
    public Queue errorQueue() {
        return new Queue(Constants.ERROR_QUEUE, true);
    }

    @Bean(name = "errorBinding")
    public Binding errorBinding(@Qualifier("errorQueue") Queue errorQueue, @Qualifier("errorExchange") TopicExchange errorExchange) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(Constants.ERROR_ROUTING_KEY);
    }

    @Bean(name = "errorBatchQueue")
    public Queue errorBatchQueue() {
        return new Queue(Constants.ERROR_BATCH_QUEUE, true);
    }

    @Bean(name = "errorBatchBinding")
    public Binding errorBatchBinding(@Qualifier("errorBatchQueue") Queue errorQueue, @Qualifier("errorExchange") TopicExchange errorExchange) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(Constants.ERROR_BATCH_ROUTING_KEY);
    }

}
