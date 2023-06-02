package com.segovia.interview.paymentclient.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.segovia.interview.paymentclient.service.SessionTokenManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SessionTokenManagerImpl implements SessionTokenManager {
    private static final Logger LOGGER = Logger.getLogger(SessionTokenManagerImpl.class.getName());

    RestTemplate restTemplate;
    private static String token;
    private static long tokenReceivedTime;

    @Value("${apikey}")
    private String apiKey;

    @Value("${accountName}")
    private String accountName;

    @Value("${authUrl}")
    private String authUrl;

    @Value("${tokenTimeOut}")
    private int tokenTimeOut;

    private static final String API_KEY = "Api-Key";
    private static final String ACCOUNT = "account";

    public SessionTokenManagerImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getToken() {
        if (token!= null && System.currentTimeMillis() - tokenReceivedTime < tokenTimeOut)
            return token;
        else
            return this.generateNewToken();
    }

    public static void setToken(String token) {
        SessionTokenManagerImpl.token = token;
    }

    public static long getTokenReceivedTime() {
        return tokenReceivedTime;
    }

    public static void setTokenReceivedTime(long tokenReceivedTime) {
        SessionTokenManagerImpl.tokenReceivedTime = tokenReceivedTime;
    }

    private String generateNewToken() {
        HttpHeaders headers = getHttpHeaders();

        // Create the request body
        String requestBody = "{\"" + ACCOUNT + "\": \"" + accountName + "\"}";

        // Create the HttpEntity object with headers and request body
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make the POST request
            ResponseEntity<String> responseEntity =  restTemplate.exchange(authUrl, HttpMethod.POST, entity, String.class);
            setTokenReceivedTime(System.currentTimeMillis());
            String newToken = new Gson().fromJson(responseEntity.getBody(), JsonObject.class).get("token").getAsString();
            setToken(newToken); // Will set the token
            return newToken;
        } catch (HttpClientErrorException e) {
            // Handle HTTP client errors (4xx)
            LOGGER.log(Level.SEVERE, "Client error occurred. Status code: "+e.getStatusCode());
        } catch (Exception e) {
            // Handle other exceptions
            LOGGER.log(Level.SEVERE, "An error occurred: {}", e.getMessage());
        }
        return null;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_KEY, apiKey);
        return headers;
    }
}
