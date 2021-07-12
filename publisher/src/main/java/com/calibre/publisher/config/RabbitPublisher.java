package com.calibre.publisher.config;


import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RabbitPublisher {
    private RabbitTemplate rabbitTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public RabbitPublisher(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate, ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(FxCurrencyRateCsvRow fxRateCsvRow) throws JsonProcessingException {
        log.info("send fxRateCsvRow:{}", fxRateCsvRow);
        if (ObjectUtils.isEmpty(fxRateCsvRow)) return;

        String json = objectMapper.writeValueAsString(fxRateCsvRow);

        long timeStamp = System.currentTimeMillis();
        String messageId = timeStamp + "-" + UUID.randomUUID();

        MessageProperties messageProperties = getMessageProperties(timeStamp, messageId);
        messageProperties.setHeader("objectType", "FxCurrencyRateCsvRow.class");

        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(messageId);

        Message message = new Message(json.getBytes(), messageProperties);
        log.info("send fxRateCsvRow message:{}", message);

        rabbitTemplate.convertAndSend(Constants.TOPIC_EXCHANGE_FX_RATE_API, Constants.TOPIC_FX_RATE_ROUTING_KEY_1,
                message, correlationData);
    }

    public void sendList(List<FxCurrencyRateCsvRow> fxRateCsvRowList) throws JsonProcessingException {
        log.info("sendList fxRateCsvRowList:{}", fxRateCsvRowList);
        if (CollectionUtils.isEmpty(fxRateCsvRowList)) return;

        String json = objectMapper.writeValueAsString(fxRateCsvRowList);

        long timeStamp = System.currentTimeMillis();
        String messageId = timeStamp + "-" + UUID.randomUUID();

        MessageProperties messageProperties = getMessageProperties(timeStamp, messageId);
        messageProperties.setHeader("objectType", "List.class");

        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(messageId);

        Message message = new Message(json.getBytes(), messageProperties);
        log.info("sendList message:{}", message);

        rabbitTemplate.convertAndSend(Constants.TOPIC_EXCHANGE_FX_RATE_API,
                Constants.TOPIC_BATCH_FX_RATE_ROUTING_KEY_1, message, correlationData);
    }

    /**
     * Not use a @Bean because we need a new instance everytime request
     *
     * @param timeStamp
     * @param messageId
     * @return
     */
    private MessageProperties getMessageProperties(long timeStamp, String messageId) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setMessageId(messageId);
        messageProperties.setHeader(AmqpHeaders.DELIVERY_TAG, timeStamp);
        messageProperties.setHeader(AmqpHeaders.CORRELATION_ID, messageId);
        return messageProperties;
    }

}
