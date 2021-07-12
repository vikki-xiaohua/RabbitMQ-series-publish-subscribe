package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitConfig implements RabbitListenerConfigurer {
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;


    @Bean(name = "connectionFactory")
    CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitHost);
        cachingConnectionFactory.setUsername(rabbitUsername);
        cachingConnectionFactory.setPassword(rabbitPassword);
        cachingConnectionFactory.setConnectionTimeout(15000);

        return cachingConnectionFactory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());

    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
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
        //return new Queue(Constants.TOPIC_BATCH_QUEUE_FX_RATE_API, true);

        Map<String, Object> params = new HashMap<>();
//        params.put("x-dead-letter-exchange", Constants.ERROR_BATCH_EXCHANGE);
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


//    @Bean(name = "errorBatchExchange")
//    public DirectExchange errorBatchExchange() {
//        return new DirectExchange(Constants.ERROR_BATCH_EXCHANGE, true, false);
//    }

    @Bean(name = "errorBatchQueue")
    public Queue errorBatchQueue() {
        return new Queue(Constants.ERROR_BATCH_QUEUE, true);
    }

    @Bean(name = "errorBatchBinding")
    public Binding errorBatchBinding(@Qualifier("errorBatchQueue") Queue errorQueue, @Qualifier("errorExchange") TopicExchange errorExchange) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(Constants.ERROR_BATCH_ROUTING_KEY);
    }

}
