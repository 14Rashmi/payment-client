package com.segovia.interview.paymentclient;

import com.segovia.interview.paymentclient.model.PaymentRequest;
import com.segovia.interview.paymentclient.service.PaymentProcessor;
import com.segovia.interview.paymentclient.util.CsvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class PaymentClientApplication implements CommandLineRunner {

	@Autowired
	PaymentProcessor paymentProcessor;

	public static void main(String[] args) {
		SpringApplication.run(PaymentClientApplication.class, args);
	}

	@Override
	public void run(String... args) {
		System.out.println("Please Enter the Csv");
		List<PaymentRequest> paymentRequests =CsvHelper.readFromCsv();
		paymentProcessor.submitOutgoingPayment(paymentRequests);

	}
}
