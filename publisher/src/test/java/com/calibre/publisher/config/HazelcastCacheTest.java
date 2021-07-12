package com.calibre.publisher.config;

import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.util.Constants;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HazelcastCacheTest {
    private HazelcastInstance hzInstance;
    private MapConfig mapConfig;
    private HazelcastCache hazelcastCache;
    private FxCurrencyRateCsvRow fxCurrencyRateCsvRow;


    @BeforeEach
    public void before() {
        hzInstance = Hazelcast.newHazelcastInstance();
        mapConfig = new MapConfig(Constants.HAZELCAST_FX_RATE_CACHE_KEY_PREFIX);
        hazelcastCache = new HazelcastCache();
        fxCurrencyRateCsvRow = new FxCurrencyRateCsvRow();
    }

    @AfterEach
    public void after() {
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
        hzInstance = null;
        mapConfig = null;
        hazelcastCache = null;
    }


    @Test
    public void test_basic_put_get() {
        Config config = hzInstance.getConfig();

        config.addMapConfig(mapConfig);
        hazelcastCache.setCacheInstance(hzInstance);

        String test_key = "basic-put-get-key";

        Assertions.assertNull(hazelcastCache.getMessage(test_key));
        hazelcastCache.putMessage(test_key, fxCurrencyRateCsvRow);
        Assertions.assertNotNull(hazelcastCache.getMessage(test_key));
    }

    @Test
    public void test_put_get__max_idle_seconds() throws InterruptedException {
        mapConfig.setMaxIdleSeconds(2);
        mapConfig.setTimeToLiveSeconds(5);

        Config config = hzInstance.getConfig();

        config.addMapConfig(mapConfig);

        hazelcastCache.setCacheInstance(hzInstance);

        String test_key_max_idle_seconds = "put-get-max-idle-key";

        Assertions.assertNull(hazelcastCache.getMessage(test_key_max_idle_seconds));
        hazelcastCache.putMessage(test_key_max_idle_seconds, fxCurrencyRateCsvRow);
        Assertions.assertNotNull(hazelcastCache.getMessage(test_key_max_idle_seconds));

        Thread.sleep(2500);
        Assertions.assertNull(hazelcastCache.getMessage(test_key_max_idle_seconds));
    }


    @Test
    public void test_put_get__time_to_live() throws InterruptedException {
        mapConfig.setMaxIdleSeconds(10);
        mapConfig.setTimeToLiveSeconds(5);

        Config config = hzInstance.getConfig();

        config.addMapConfig(mapConfig);

        hazelcastCache.setCacheInstance(hzInstance);

        String test_key_time_to_live = "put-get-time-to-live-key";

        Assertions.assertNull(hazelcastCache.getMessage(test_key_time_to_live));
        hazelcastCache.putMessage(test_key_time_to_live, fxCurrencyRateCsvRow);
        Thread.sleep(1000);
        Assertions.assertNotNull(hazelcastCache.getMessage(test_key_time_to_live));
        Thread.sleep(5500);
        Assertions.assertNull(hazelcastCache.getMessage(test_key_time_to_live));
    }
}

