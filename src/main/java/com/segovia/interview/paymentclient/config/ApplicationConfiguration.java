package com.segovia.interview.paymentclient.config;

import com.segovia.interview.paymentclient.service.SessionTokenManager;
import com.segovia.interview.paymentclient.service.impl.SessionTokenManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class ApplicationConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SessionTokenManager sessionTokenManager(RestTemplate restTemplate) {
        return new SessionTokenManagerImpl(restTemplate);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.segovia.interview.paymentclient.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
