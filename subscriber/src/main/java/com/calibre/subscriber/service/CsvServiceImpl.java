package com.calibre.subscriber.service;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.model.FxRateCsvHeader;
import com.calibre.subscriber.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Slf4j
@Service
public class CsvServiceImpl implements ICsvService {
    private IEmailService emailService;

    @Autowired
    public CsvServiceImpl(IEmailService emailService) {
        this.emailService = emailService;
    }

    @Async("csv-service-taskExecutor")
    @Override
    public void createCsvFileAndSendFromBatch(List<FxCurrencyRateCsvRow> fxCurrencyRateCsvRowList, String fileName) throws IOException, MessagingException, InterruptedException {
        log.info("createCsvFileAndSendFromList fxCurrencyRateCsvRowList: {},fileName:{},thread id:{}", fxCurrencyRateCsvRowList, fileName, Thread.currentThread().getId());
        if (CollectionUtils.isEmpty(fxCurrencyRateCsvRowList)) return;

        try (Writer writer = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(FxRateCsvHeader.class))
        ) {

            for (FxCurrencyRateCsvRow fxCurrencyRateCsvRow : fxCurrencyRateCsvRowList) {
                csvPrinter.printRecord(fxCurrencyRateCsvRow.getForex(), fxCurrencyRateCsvRow.getValue());
            }
        }

        emailService.sendEmail(Constants.CSV_ATTACHMENT_BATCH_SUBJECT, Constants.CSV_ATTACHMENT_MESSAGE, new String[]{fileName});
    }

    @Async("csv-service-taskExecutor")
    @Override
    public void createCsvFileAndSendFromSingle(FxCurrencyRateCsvRow fxCurrencyRateCsvRow, String fileName) throws IOException, MessagingException, InterruptedException {
        log.info("createCsvFileAndSend fxCurrencyRateCsvRow: {},fileName:{},thread id:{}", fxCurrencyRateCsvRow, fileName, Thread.currentThread().getId());
        if (ObjectUtils.isEmpty(fxCurrencyRateCsvRow)) return;

        try (Writer writer = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(FxRateCsvHeader.class))
        ) {
            csvPrinter.printRecord(fxCurrencyRateCsvRow.getForex(), fxCurrencyRateCsvRow.getValue());
        }

        emailService.sendEmail(Constants.CSV_ATTACHMENT_SUBJECT, Constants.CSV_ATTACHMENT_MESSAGE, new String[]{fileName});
    }

}
