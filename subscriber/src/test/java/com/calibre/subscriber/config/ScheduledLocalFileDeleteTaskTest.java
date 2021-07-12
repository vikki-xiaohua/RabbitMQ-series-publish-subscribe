package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
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
public class ScheduledLocalFileDeleteTaskTest {
    @SpyBean
    private ScheduledLocalFileDeleteTask scheduledLocalFileDeleteTask;

    @Test
    public void jobInvoked() {
        Awaitility.await().atMost(java.time.Duration.ofMillis(Constants.SCHEDULED_FILE_DELETE_INITIAL_DELAY_MILLISECONDS + Constants.SCHEDULED_FILE_DELETE_FIXED_RATE_MILLISECONDS + 500))
                .untilAsserted(() -> Mockito.verify(scheduledLocalFileDeleteTask, Mockito.times(1)).deleteLocalFiles());
    }

    @Test
    public void jobInvoke_fail() {
        Assertions.assertThrows(ConditionTimeoutException.class, () -> {
            Awaitility.await().atMost(java.time.Duration.ofMillis(Constants.SCHEDULED_FILE_DELETE_INITIAL_DELAY_MILLISECONDS - 500))
                    .untilAsserted(() -> Mockito.verify(scheduledLocalFileDeleteTask, Mockito.times(1)).deleteLocalFiles());
        });

    }
}
