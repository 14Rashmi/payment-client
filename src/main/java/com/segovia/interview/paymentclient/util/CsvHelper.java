package com.segovia.interview.paymentclient.util;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.segovia.interview.paymentclient.enums.Status;
import com.segovia.interview.paymentclient.model.PaymentRequest;
import com.segovia.interview.paymentclient.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CsvHelper {

    private static final Logger LOGGER = Logger.getLogger(CsvHelper.class.getName());

    public static List<PaymentRequest> readFromCsv() {
        // Read the CSV input from the user
        Scanner scanner = new Scanner(System.in);
        StringBuilder csvInput = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Stop reading when an empty line is encountered
            if (line.isEmpty()) {
                break;
            }
            csvInput.append(line).append("\n");
        }

        // Process the CSV data
        String csvData = csvInput.toString();
        try {
            // Read CSV data and map it to PaymentRequest objects
            List<PaymentRequest> paymentRequests = new CsvToBeanBuilder<PaymentRequest>(new StringReader(csvData))
                    .withType(PaymentRequest.class)
                    .build()
                    .parse();
            LOGGER.info("CSV Read complete");
            return paymentRequests;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    public void writeCsv(Set<PaymentStatus> objects) {
        try (PrintWriter writer = new PrintWriter(System.out)) {
            CSVWriter csvWriter = new CSVWriter(writer);

            // Write the CSV header
            csvWriter.writeNext(new String[]{"ID","Server-generated ID","Status","Fee","Details"});

            // Write object data to the CSV
            for (PaymentStatus object : objects) {
                csvWriter.writeNext(new String[]{object.getCustomerReference(), object.getReference(), Status.getDescriptionFromCode(object.getStatus()), String.valueOf(object.getFee()), object.getMessage()});
            }
            csvWriter.flush();

            LOGGER.info("CSV output generated successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating CSV output: "+e.getMessage());

        }
    }
}