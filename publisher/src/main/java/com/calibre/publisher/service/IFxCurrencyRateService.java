package com.calibre.publisher.service;

import com.calibre.publisher.model.FxCurrencyRate;
import com.calibre.publisher.model.FxCurrencyRateCsvRow;

import java.util.List;

public interface IFxCurrencyRateService {

    FxCurrencyRate getFxRateByCurrencyPair(String currencyPair);

    void getFxRateListAndSend(String currencyPair, List<FxCurrencyRateCsvRow> toSendList);

}
