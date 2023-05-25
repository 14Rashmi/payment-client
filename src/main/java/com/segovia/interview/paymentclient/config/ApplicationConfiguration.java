package com.segovia.interview.paymentclient.config;

import com.segovia.interview.paymentclient.service.PaymentProcessor;
import com.segovia.interview.paymentclient.service.SessionTokenManager;
import com.segovia.interview.paymentclient.util.CsvHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SessionTokenManager sessionTokenManager(RestTemplate restTemplate) {
        return new SessionTokenManager(restTemplate);
    }

}
