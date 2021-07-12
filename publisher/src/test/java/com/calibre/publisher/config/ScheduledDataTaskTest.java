package com.calibre.publisher.config;

import com.calibre.publisher.util.Constants;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Disabled("These tests works, but have to wait for too long a time")
public class ScheduledDataTaskTest {
    @SpyBean
    private ScheduledDataTask scheduledDataTask;

    @Test
    public void jobInvoked() {
        Awaitility.await().atMost(java.time.Duration.ofMillis(Constants.SCHEDULED_DATA_REQUEST_FIXED_RATE_MILLISECONDS + Constants.SCHEDULED_DATA_REQUEST_RATE_INITIAL_DELAY_MILLISECONDS + 500))
                .untilAsserted(() -> Mockito.verify(scheduledDataTask, Mockito.times(1)).scheduleTaskWithFixedRate());
    }

    @Test
    public void jobInvoke_fail() {
        Assertions.assertThrows(ConditionTimeoutException.class, () -> {
            Awaitility.await().atMost(java.time.Duration.ofMillis(Constants.SCHEDULED_DATA_REQUEST_FIXED_RATE_MILLISECONDS - 500))
                    .untilAsserted(() -> Mockito.verify(scheduledDataTask, Mockito.times(1)).scheduleTaskWithFixedRate());
        });

    }
}
