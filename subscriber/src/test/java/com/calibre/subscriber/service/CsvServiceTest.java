package com.calibre.subscriber.service;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CsvServiceTest {
    @Mock
    private IEmailService iEmailService;

    @InjectMocks
    private ICsvService csvService = new CsvServiceImpl(iEmailService);

    private String fileName = "test-fileName" + System.currentTimeMillis() + ".csv";
    private FxCurrencyRateCsvRow fxCurrencyRateCsvRow = new FxCurrencyRateCsvRow("AUD", 1.0);

    @Test
    public void test_createCsvFileAndSendFromSingle() throws MessagingException, IOException, InterruptedException {
        Mockito.doNothing().when(iEmailService).sendEmail(Constants.CSV_ATTACHMENT_SUBJECT, Constants.CSV_ATTACHMENT_MESSAGE, new String[]{fileName});
        csvService.createCsvFileAndSendFromSingle(fxCurrencyRateCsvRow, fileName);

        deleteTestFile();
    }

    @Test
    public void test_createCsvFileAndSendFromBatch() throws MessagingException, IOException, InterruptedException {
        List<FxCurrencyRateCsvRow> list = new ArrayList<>();
        list.add(fxCurrencyRateCsvRow);

        Mockito.doNothing().when(iEmailService).sendEmail(Constants.CSV_ATTACHMENT_BATCH_SUBJECT, Constants.CSV_ATTACHMENT_MESSAGE, new String[]{fileName});
        csvService.createCsvFileAndSendFromBatch(list, fileName);

        deleteTestFile();
    }

    @Test
    public void test_createCsvFileAndSendFromSingle_emptyObject() throws MessagingException, IOException, InterruptedException {
        csvService.createCsvFileAndSendFromSingle(null, fileName);
        Mockito.verify(iEmailService, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        csvService.createCsvFileAndSendFromSingle(new FxCurrencyRateCsvRow(), fileName);
        Mockito.verify(iEmailService, Mockito.times(1)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        deleteTestFile();
    }

    @Test
    public void test_createCsvFileAndSendFromBatch_emptyList() throws MessagingException, IOException, InterruptedException {
        csvService.createCsvFileAndSendFromBatch(null, fileName);
        Mockito.verify(iEmailService, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        csvService.createCsvFileAndSendFromBatch(new ArrayList<>(), fileName);
        Mockito.verify(iEmailService, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }

    private void deleteTestFile() {
        File fileToDelete = new File(fileName);
        boolean success = fileToDelete.delete();
        Assertions.assertTrue(success);
    }

}
