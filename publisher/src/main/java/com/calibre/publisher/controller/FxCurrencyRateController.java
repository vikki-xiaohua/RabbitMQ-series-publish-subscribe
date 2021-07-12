package com.calibre.publisher.controller;

import com.calibre.publisher.config.HazelcastCache;
import com.calibre.publisher.config.RabbitPublisher;
import com.calibre.publisher.model.FxCurrencyRate;
import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.service.IEmailService;
import com.calibre.publisher.service.IFxCurrencyRateService;
import com.calibre.publisher.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class FxCurrencyRateController {
    private HazelcastCache cacheClient;
    private IFxCurrencyRateService fxCurrencyRateService;
    private IEmailService emailService;
    private RabbitPublisher rabbitPublisher;

    @Value("${forex.endpoint.currency-pair-list}")
    private List<String> currencyPairList;

    @Autowired
    public FxCurrencyRateController(HazelcastCache cacheClient, IFxCurrencyRateService fxCurrencyRateService,
                                    IEmailService emailService, RabbitPublisher rabbitPublisher) {
        this.cacheClient = cacheClient;
        this.fxCurrencyRateService = fxCurrencyRateService;
        this.emailService = emailService;
        this.rabbitPublisher = rabbitPublisher;
    }

    @GetMapping("/query/{currency_pair}")
    public ResponseEntity<FxCurrencyRateCsvRow> queryByCurrencyPair(@PathVariable("currency_pair") String currencyPair) {
        log.info("controller currencyPair: {}, currencyPairList: {} ", currencyPair, currencyPairList);

        if (StringUtils.isBlank(currencyPair) || CollectionUtils.isEmpty(currencyPairList) || !currencyPairList.contains(currencyPair))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            FxCurrencyRateCsvRow previousFxCurrencyRateCsvRow = cacheClient.getMessage(currencyPair);
            log.info("previousFxCurrencyRateCsvRow: {}", previousFxCurrencyRateCsvRow);

            FxCurrencyRate fxCurrencyRate = fxCurrencyRateService.getFxRateByCurrencyPair(currencyPair);

            if (fxCurrencyRate == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            FxCurrencyRateCsvRow fxCurrencyRateCsvRow = null;

            if (previousFxCurrencyRateCsvRow == null || Double.compare(previousFxCurrencyRateCsvRow.getValue(), fxCurrencyRate.getClose()) != 0) {
                fxCurrencyRateCsvRow = FxCurrencyRateCsvRow.builder().forex(currencyPair).value(fxCurrencyRate.getClose()).build();
                cacheClient.putMessage(currencyPair, fxCurrencyRateCsvRow);
                rabbitPublisher.send(fxCurrencyRateCsvRow);
            }

            return ResponseEntity.status(HttpStatus.OK).body(fxCurrencyRateCsvRow == null ? previousFxCurrencyRateCsvRow : fxCurrencyRateCsvRow);

        } catch (Exception | Error throwable) {
            log.error("exception | error: {}", Arrays.toString(throwable.getStackTrace()));

            if (throwable instanceof Error) {
                emailService.sendEmail(Constants.APPLICATION_ERROR_SUBJECT, Constants.APPLICATION_ERROR_BODY + Arrays.toString(throwable.getStackTrace()) + "</p>", null);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
