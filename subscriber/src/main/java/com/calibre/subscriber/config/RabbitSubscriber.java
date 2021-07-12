package com.calibre.subscriber.config;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.service.ICsvService;
import com.calibre.subscriber.service.IEmailService;
import com.calibre.subscriber.util.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class RabbitSubscriber {
    private static ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd_HHmm"));

    private ICsvService csvService;
    private IEmailService emailService;
    private ObjectMapper objectMapper;
    private HazelcastCache cacheClient;

    @Autowired
    public RabbitSubscriber(ICsvService csvService, IEmailService emailService,
                            ObjectMapper objectMapper, HazelcastCache cacheClient) {
        this.csvService = csvService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.cacheClient = cacheClient;
    }

    @RabbitListener(queues = Constants.TOPIC_QUEUE_FX_RATE_API)
    public void handleMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long timestamp) throws IOException, MessagingException, InterruptedException {
        log.info("handleMessage message: {}, thread id: {} ", message, Thread.currentThread().getId());
        if (ObjectUtils.isEmpty(message) || ObjectUtils.isEmpty(message.getMessageProperties())
                || ArrayUtils.isEmpty(message.getBody())) return;

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        if (StringUtils.isBlank(messageId)) return;

        try {

            if (cacheClient.getMessage(messageId)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            byte[] messageBody = message.getBody();
            log.info("handleMessage message body:{} ", new String(messageBody));

            FxCurrencyRateCsvRow toSendObject = objectMapper.readValue(messageBody, FxCurrencyRateCsvRow.class);
            String fileName = Constants.CSV_FILE_PREFIX + dateFormat.get().format(timestamp) + Constants.CSV_FILE_SUFFIX;

            csvService.createCsvFileAndSendFromSingle(toSendObject, fileName);
            cacheClient.putMessage(messageId, true);

            channel.basicAck(deliveryTag, false);

            log.info("handleMessage csv create and send succeed, fileName: {}", fileName);

        } catch (Exception | Error throwable) {
            logAndEmail(channel, deliveryTag, throwable);
            throw throwable;
        }
    }

    @RabbitListener(queues = Constants.TOPIC_BATCH_QUEUE_FX_RATE_API)
    public void handleBatchMessages(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException, MessagingException, InterruptedException {
        log.info("handleBatchMessages message: {}, thread id: {} ", message, Thread.currentThread().getId());

        if (ObjectUtils.isEmpty(message) || ObjectUtils.isEmpty(message.getMessageProperties()) || ArrayUtils.isEmpty(message.getBody()))
            return;

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        if (StringUtils.isBlank(messageId)) return;

        try {
            if (cacheClient.getMessage(messageId)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            byte[] messageBody = message.getBody();
            log.info("handleBatchMessages message body:{} ", new String(messageBody));

            List<FxCurrencyRateCsvRow> toSendList = objectMapper.readValue(messageBody, new TypeReference<List<FxCurrencyRateCsvRow>>() {
            });

            String fileName = Constants.CSV_FILE_PREFIX + dateFormat.get().format(tag) + Constants.CSV_FILE_SUFFIX;

            csvService.createCsvFileAndSendFromBatch(toSendList, fileName);

            cacheClient.putMessage(messageId, true);

            channel.basicAck(deliveryTag, false);

            log.info("handleBatchMessages csv create and send succeed, fileName: {}", fileName);

        } catch (Exception | Error throwable) {

            logAndEmail(channel, deliveryTag, throwable);

            throw throwable;
        }
    }

    private void logAndEmail(Channel channel, long deliveryTag, Throwable throwable) {
        log.error("exception | error: {}", Arrays.toString(throwable.getStackTrace()));
        try {
            channel.basicNack(deliveryTag, false, false);
            if (throwable instanceof Error) {
                emailService.sendEmail(Constants.APPLICATION_ERROR_SUBJECT, Constants.APPLICATION_ERROR_BODY + Arrays.toString(throwable.getStackTrace()) + "</p>", null);
            }
        } catch (IOException | MessagingException | InterruptedException ioException) {
            log.error("IOException | MessagingException: {}", Arrays.toString(ioException.getStackTrace()));
        }
    }
}
