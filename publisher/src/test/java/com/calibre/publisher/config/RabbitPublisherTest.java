package com.calibre.publisher.config;

import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class RabbitPublisherTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitPublisher rabbitPublisher = new RabbitPublisher(rabbitTemplate, objectMapper);

    @Test
    public void test_send() throws IOException {
        FxCurrencyRateCsvRow fxCurrencyRateCsvRow = new FxCurrencyRateCsvRow("AUDHKD", 1.0);
        rabbitPublisher.send(fxCurrencyRateCsvRow);

    }

    @Test
    public void test_sendList() throws JsonProcessingException {
        FxCurrencyRateCsvRow fxCurrencyRateCsvRow1 = new FxCurrencyRateCsvRow("AUD", 1.0);
        FxCurrencyRateCsvRow fxCurrencyRateCsvRow2 = new FxCurrencyRateCsvRow("NZD", 2.0);

        List<FxCurrencyRateCsvRow> toSendList = new ArrayList<>();
        toSendList.add(fxCurrencyRateCsvRow1);
        toSendList.add(fxCurrencyRateCsvRow2);

        rabbitPublisher.sendList(toSendList);
    }

    @Test
    public void test_send_empty_message() throws IOException {
        rabbitPublisher.send(null);
    }

    @Test
    public void test_sendList_empty_message() throws IOException {
        rabbitPublisher.sendList(null);
        rabbitPublisher.sendList(new ArrayList<>());
    }

}
