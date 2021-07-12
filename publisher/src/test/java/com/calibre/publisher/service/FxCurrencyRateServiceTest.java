package com.calibre.publisher.service;

import com.calibre.publisher.config.HazelcastCache;
import com.calibre.publisher.model.FxCurrencyRate;
import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class FxCurrencyRateServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HazelcastCache cacheClient;

    @InjectMocks
    private IFxCurrencyRateService fxRateService = new FxCurrencyRateServiceImpl(restTemplate, cacheClient);

    @Test
    public void test_getFxRateByCurrencyPair() {
        String currencyPair = "AUDUSD";
        FxCurrencyRate fxCurrencyRate = getFxCurrencyRate(currencyPair);

        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).
                thenReturn(fxCurrencyRate);

        Assertions.assertEquals(currencyPair, fxRateService.getFxRateByCurrencyPair(currencyPair).getCode());
        Assertions.assertEquals(1.0, fxRateService.getFxRateByCurrencyPair(currencyPair).getClose());
    }


    @Test
    public void test_asyncRestCallAndBatchSend() {
        String currencyPair = "AUDNZD";
        FxCurrencyRate fxCurrencyRate = getFxCurrencyRate(currencyPair);

        List<FxCurrencyRateCsvRow> toSendList = new ArrayList<>();

        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any(), Mockito.any(HashMap.class))).
                thenReturn(fxCurrencyRate);

        fxRateService.getFxRateListAndSend(currencyPair, toSendList);

        Assertions.assertEquals(1.0, toSendList.size());
        Assertions.assertEquals(currencyPair, toSendList.get(0).getForex());
    }

    private FxCurrencyRate getFxCurrencyRate(String currencyPair) {
        FxCurrencyRate fxCurrencyRate = new FxCurrencyRate();
        fxCurrencyRate.setCode(currencyPair);
        fxCurrencyRate.setClose(1.0);
        return fxCurrencyRate;
    }
}
