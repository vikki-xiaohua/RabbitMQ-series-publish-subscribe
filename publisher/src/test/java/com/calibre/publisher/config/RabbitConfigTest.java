package com.calibre.publisher.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RabbitConfigTest {
    @Mock
    private ConfirmCallbackService confirmCallbackService;

    @Mock
    private ReturnCallbackService returnCallbackService;

    @InjectMocks
    private RabbitConfig rabbitConfig = new RabbitConfig(confirmCallbackService, returnCallbackService);


    @Test
    public void should_check_presence_of_beans() {
        Assertions.assertNotNull(rabbitConfig.connectionFactory());
        Assertions.assertNotNull(rabbitConfig.rabbitTemplate(rabbitConfig.connectionFactory()));
        Assertions.assertNotNull(rabbitConfig.jsonMessageConverter());

        Assertions.assertNotNull(rabbitConfig.binding(rabbitConfig.queue(), rabbitConfig.exchange()));
        Assertions.assertNotNull(rabbitConfig.batchBinding(rabbitConfig.batchQueue(), rabbitConfig.exchange()));
        Assertions.assertNotNull(rabbitConfig.errorBinding(rabbitConfig.errorQueue(), rabbitConfig.errorExchange()));
        Assertions.assertNotNull(rabbitConfig.errorBatchBinding(rabbitConfig.errorBatchQueue(), rabbitConfig.errorExchange()));

    }
}