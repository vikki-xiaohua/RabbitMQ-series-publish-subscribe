package com.calibre.subscriber.config;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.service.CsvServiceImpl;
import com.calibre.subscriber.service.ICsvService;
import com.calibre.subscriber.service.IEmailService;
import com.calibre.subscriber.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.sun.tools.javac.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.AmqpHeaders;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class RabbitSubscriberTest {
    private static ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd_HHmm"));

    @Mock
    private IEmailService iEmailService;

    private ICsvService iCsvService = new CsvServiceImpl(iEmailService);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HazelcastCache hazelcastCache;

    @InjectMocks
    RabbitSubscriber rabbitSubscriber = new RabbitSubscriber(iCsvService, iEmailService, objectMapper, hazelcastCache);

    @Test
    public void test_handleMessage() throws IOException {

        long timeStamp = System.currentTimeMillis();
        FxCurrencyRateCsvRow toSendObject = new FxCurrencyRateCsvRow("AUD", (double) timeStamp);
        Connection mockConnection = new MockConnectionFactory().newConnection();
        assert mockConnection != null;
        Channel channel = mockConnection.createChannel();
        String queueName = channel.queueDeclare(Constants.TOPIC_QUEUE_FX_RATE_API, true, true, true, null).getQueue();
        channel.exchangeDeclare(Constants.TOPIC_EXCHANGE_FX_RATE_API, "topic", true);
        channel.queueBind(queueName, Constants.TOPIC_EXCHANGE_FX_RATE_API, Constants.TOPIC_FX_RATE_API_ROUTING_KEY);

        String json = objectMapper.writeValueAsString(toSendObject);
        String messageId = timeStamp + "-" + UUID.randomUUID();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(messageId);
        messageProperties.setHeader(AmqpHeaders.DELIVERY_TAG, timeStamp);
        Message message = new Message(json.getBytes(), messageProperties);
        String fileName = Constants.CSV_FILE_PREFIX + dateFormat.get().format(timeStamp) + Constants.CSV_FILE_SUFFIX;

        Assertions.assertThrows(NullPointerException.class, () -> {
            rabbitSubscriber.handleMessage(message, channel, timeStamp);
        });

        mockConnection.abort(1000);

        Mockito.verify(hazelcastCache, Mockito.times(1)).getMessage(messageId);
        File fileList = new File(fileName);

        Assertions.assertTrue(fileList.exists());
        fileList.delete();
        Assertions.assertFalse(fileList.exists());
    }

    @Test
    public void test_handleBatchMessages() throws IOException {
        long timeStamp = System.currentTimeMillis();
        FxCurrencyRateCsvRow toSendObject1 = new FxCurrencyRateCsvRow("AUD", (double) timeStamp);
        FxCurrencyRateCsvRow toSendObject2 = new FxCurrencyRateCsvRow("NZD", (double) timeStamp);

        List<FxCurrencyRateCsvRow> toSendList = List.from(new FxCurrencyRateCsvRow[]{toSendObject1, toSendObject2});

        Connection mockConnection = new MockConnectionFactory().newConnection();
        assert mockConnection != null;
        Channel channel = mockConnection.createChannel();
        String queueName = channel.queueDeclare(Constants.TOPIC_BATCH_QUEUE_FX_RATE_API, true, true, true, null).getQueue();
        channel.exchangeDeclare(Constants.TOPIC_EXCHANGE_FX_RATE_API, "topic", true);
        channel.queueBind(queueName, Constants.TOPIC_EXCHANGE_FX_RATE_API, Constants.TOPIC_BATCH_FX_RATE_API_ROUTING_KEY);

        String json = objectMapper.writeValueAsString(toSendList);
        String messageId = timeStamp + "-" + UUID.randomUUID();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(messageId);
        messageProperties.setHeader(AmqpHeaders.DELIVERY_TAG, timeStamp);
        Message message = new Message(json.getBytes(), messageProperties);
        String fileName = Constants.CSV_FILE_PREFIX + dateFormat.get().format(timeStamp) + Constants.CSV_FILE_SUFFIX;

        Assertions.assertThrows(NullPointerException.class, () -> {
            rabbitSubscriber.handleBatchMessages(message, channel, timeStamp);
        });

        mockConnection.abort(1000);

        Mockito.verify(hazelcastCache, Mockito.times(1)).getMessage(messageId);
        File fileList = new File(fileName);

        Assertions.assertTrue(fileList.exists());
        fileList.delete();
        Assertions.assertFalse(fileList.exists());
    }

    @Test
    public void test_handleMessage_emptyMessage() throws IOException, MessagingException, InterruptedException {
        long timeStamp = System.currentTimeMillis();
        Connection mockConnection = new MockConnectionFactory().newConnection();
        assert mockConnection != null;
        Channel channel = mockConnection.createChannel();
        rabbitSubscriber.handleMessage(null, channel, timeStamp);
    }

    @Test
    public void test_handleBatchMessages_emptyMessage() throws IOException, MessagingException, InterruptedException {
        long timeStamp = System.currentTimeMillis();
        Connection mockConnection = new MockConnectionFactory().newConnection();
        assert mockConnection != null;
        Channel channel = mockConnection.createChannel();
        rabbitSubscriber.handleBatchMessages(null, channel, timeStamp);
    }
}


