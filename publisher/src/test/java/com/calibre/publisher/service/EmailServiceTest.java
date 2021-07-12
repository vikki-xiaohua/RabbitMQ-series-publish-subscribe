package com.calibre.publisher.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import java.io.IOException;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @InjectMocks
    private IEmailService emailService = new EmailServiceImpl();

    @Test
    public void test_sendEmail_success() throws MessagingException, IOException, InterruptedException {
        ReflectionTestUtils.setField(emailService, "host", "smtp.163.com");
        ReflectionTestUtils.setField(emailService, "port", "25");
        ReflectionTestUtils.setField(emailService, "userName", "vikki_xiaohua_yu@163.com");
        ReflectionTestUtils.setField(emailService, "password", "JENNLEZMWVNLOCWB");
        ReflectionTestUtils.setField(emailService, "toAddress", "575267785@qq.com");
        ReflectionTestUtils.setField(emailService, "smtpAuth", "true");

        emailService.sendEmail("subject", "message", new String[]{});
    }


    @Test
    public void test_sendEmail_empty_message_or_subject() throws MessagingException, IOException, InterruptedException {
        ReflectionTestUtils.setField(emailService, "host", "smtp.163.com");
        ReflectionTestUtils.setField(emailService, "port", "25");
        ReflectionTestUtils.setField(emailService, "userName", "userName");
        ReflectionTestUtils.setField(emailService, "password", "password");
        ReflectionTestUtils.setField(emailService, "toAddress", "575267785@qq.com");
        ReflectionTestUtils.setField(emailService, "smtpAuth", "true");

        emailService.sendEmail(null, "message", new String[]{});
        emailService.sendEmail("subject", null, new String[]{});

        emailService.sendEmail("", "message", new String[]{});
        emailService.sendEmail("subject", "", new String[]{});
    }

    @Test
    public void test_sendEmail_MessagingException() {
        ReflectionTestUtils.setField(emailService, "host", "smtp.163.com");
        ReflectionTestUtils.setField(emailService, "port", "25");
        ReflectionTestUtils.setField(emailService, "userName", "vikki_xiaohua_yu@163.com");
        ReflectionTestUtils.setField(emailService, "password", "JENNLEZMWVNLOCWB");
        ReflectionTestUtils.setField(emailService, "toAddress", "575267785@qq.com");
        ReflectionTestUtils.setField(emailService, "smtpAuth", "true");

        emailService.sendEmail("subject-MessagingException", "message-MessagingException", new String[]{"not-exist.txt"});
    }

    @Test
    public void test_sendEmail_AuthenticationFailedException() {
        ReflectionTestUtils.setField(emailService, "host", "smtp.163.com");
        ReflectionTestUtils.setField(emailService, "port", "25");
        ReflectionTestUtils.setField(emailService, "userName", "userName");
        ReflectionTestUtils.setField(emailService, "password", "password");
        ReflectionTestUtils.setField(emailService, "toAddress", "575267785@qq.com");
        ReflectionTestUtils.setField(emailService, "smtpAuth", "true");

        emailService.sendEmail("subject-exception", "message-exception", new String[]{});
    }
}