package com.calibre.publisher.controller;

import com.calibre.publisher.config.HazelcastCache;
import com.calibre.publisher.config.RabbitPublisher;
import com.calibre.publisher.model.FxCurrencyRate;
import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.service.IEmailService;
import com.calibre.publisher.service.IFxCurrencyRateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(controllers = FxCurrencyRateController.class)
@ActiveProfiles("test")
public class FxCurrencyRateControllerTest {
    @Autowired
    FxCurrencyRateController controller;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HazelcastCache cache;
    @MockBean
    private IFxCurrencyRateService IFxCurrencyRateService;
    @MockBean
    private IEmailService iEmailService;
    @MockBean
    private RabbitPublisher rabbitPublisher;

    @Test
    public void test_status_200_response() {
        List<String> currencyPairList = new ArrayList<>();
        currencyPairList.add("AUD");
        currencyPairList.add("EUR");
        ReflectionTestUtils.setField(controller, "currencyPairList", currencyPairList);
        String currencyPair = "AUD";

        FxCurrencyRate fxCurrencyRate = new FxCurrencyRate();
        fxCurrencyRate.setCode(currencyPair);
        fxCurrencyRate.setClose(1.0);

        Mockito.when(IFxCurrencyRateService.getFxRateByCurrencyPair(Mockito.anyString())).thenReturn(fxCurrencyRate);

        ResponseEntity<FxCurrencyRateCsvRow> response = controller.queryByCurrencyPair(currencyPair);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void test_status_400_response() {
        List<String> currencyPairList = new ArrayList<>();
        currencyPairList.add("AUD");
        currencyPairList.add("EUR");
        ReflectionTestUtils.setField(controller, "currencyPairList", currencyPairList);
        String currencyPair = "NZD";

        FxCurrencyRate fxCurrencyRate = new FxCurrencyRate();
        fxCurrencyRate.setCode(currencyPair);
        fxCurrencyRate.setClose(1.0);

        Mockito.when(IFxCurrencyRateService.getFxRateByCurrencyPair(Mockito.anyString())).thenReturn(fxCurrencyRate);

        ResponseEntity<FxCurrencyRateCsvRow> response = controller.queryByCurrencyPair(currencyPair);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_status_404_response() {
        List<String> currencyPairList = new ArrayList<>();
        currencyPairList.add("AUD");
        currencyPairList.add("EUR");
        ReflectionTestUtils.setField(controller, "currencyPairList", currencyPairList);
        String currencyPair = "EUR";

        Mockito.when(IFxCurrencyRateService.getFxRateByCurrencyPair(Mockito.anyString())).thenReturn(null);

        ResponseEntity<FxCurrencyRateCsvRow> response = controller.queryByCurrencyPair(currencyPair);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void test_status_500_response() {
        List<String> currencyPairList = new ArrayList<>();
        currencyPairList.add("AUD");
        currencyPairList.add("EUR");
        ReflectionTestUtils.setField(controller, "currencyPairList", currencyPairList);
        String currencyPair = "EUR";

        Mockito.when(IFxCurrencyRateService.getFxRateByCurrencyPair(Mockito.anyString())).thenThrow(new RuntimeException());

        ResponseEntity<FxCurrencyRateCsvRow> response = controller.queryByCurrencyPair(currencyPair);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
