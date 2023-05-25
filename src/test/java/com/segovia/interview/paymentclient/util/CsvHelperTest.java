package com.segovia.interview.paymentclient.util;

import com.segovia.interview.paymentclient.model.PaymentRequest;
import com.segovia.interview.paymentclient.model.PaymentStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class CsvHelperTest {

    CsvHelper csvHelper = new CsvHelper();

    @Test
    public void testReadFromCsv() {
        // Prepare the CSV input for testing
        String csvInput = "ID,Recipient,Amount,Currency\n" +
                "aaaaaaaa,254999999999,10,KES\n" +
                "bbbbbbbb,12125551000,50,USD\n" +
                "xyz12345,12125551005,150.35,USD\n" +
                "29387431,12125550000,37,NGN";

        // Convert the CSV input to an InputStream
        InputStream inputStream = new ByteArrayInputStream(csvInput.getBytes(StandardCharsets.UTF_8));
        System.setIn(inputStream); // Set the input stream to System.in

        // Call the method being tested
        List<PaymentRequest> paymentRequests = csvHelper.readFromCsv();

        // Assert the expected results
        Assertions.assertNotNull(paymentRequests);
        Assertions.assertEquals(4, paymentRequests.size());

        // Verify the contents of the paymentRequests list
        PaymentRequest paymentRequest1 = paymentRequests.get(0);
        Assertions.assertEquals("aaaaaaaa", paymentRequest1.getReference());
        Assertions.assertEquals(BigDecimal.valueOf(10), paymentRequest1.getAmount());
        Assertions.assertEquals("KES", paymentRequest1.getCurrency());
        Assertions.assertEquals("254999999999", paymentRequest1.getMsisdn());
    }

    @Test
    public void testWriteCsv() {
        // Prepare the objects for testing
        Set<PaymentStatus> objects = new HashSet<>();
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setStatus(0);
        paymentStatus.setFee("0.57");
        paymentStatus.setMessage("kljkl");
        paymentStatus.setReference("89789");
        paymentStatus.setCustomerReference("CS1");

        PaymentStatus paymentStatus1 = new PaymentStatus();
        paymentStatus1.setStatus(0);
        paymentStatus1.setFee("0.67");
        paymentStatus1.setMessage("kljkjhhkl");
        paymentStatus1.setReference("7898");
        paymentStatus1.setCustomerReference("CS2");

        objects.add(paymentStatus);
        objects.add(paymentStatus1);

        // Redirect System.out to capture the CSV output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);

        // Call the method being tested
        csvHelper.writeCsv(objects);

        // Get the CSV output as a string
        String csvOutput = outputStream.toString().trim();

        // Assert the expected CSV output
        String expectedOutput1 = "\"ID\",\"Server-generated ID\",\"Status\",\"Fee\",\"Details\"";
        String expectedOutput2 = "\"CS2\",\"7898\",\"Transaction succeeded\",\"0.67\",\"kljkjhhkl\"";
        String expectedOutput3 = "\"CS1\",\"89789\",\"Transaction succeeded\",\"0.57\",\"kljkl\"";
        Assertions.assertTrue(csvOutput.contains(expectedOutput1));
        Assertions.assertTrue(csvOutput.contains(expectedOutput2));
        Assertions.assertTrue(csvOutput.contains(expectedOutput3));

    }
}
