package com.segovia.interview.paymentclient.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SessionTokenManagerTest {

    @Mock
    private RestTemplate restTemplate;

    private SessionTokenManager sessionTokenManager;

    String authUrl = "http://example.com/auth";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionTokenManager = new SessionTokenManager(restTemplate);
        ReflectionTestUtils.setField(sessionTokenManager, "authUrl", authUrl);
        ReflectionTestUtils.setField(sessionTokenManager, "tokenTimeOut", 4000);
    }

    @Test
    public void testSuccessfulTokenGenerationForFirstTime() {
        ReflectionTestUtils.setField(sessionTokenManager, "tokenReceivedTime", 0);
        ReflectionTestUtils.setField(sessionTokenManager, "token", "");
        String responseBody = "{\"token\": \"generated-token\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                        Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
                .thenReturn(responseEntity);
        String generatedToken =sessionTokenManager.getToken();
        Assert.assertEquals("generated-token", generatedToken);
    }

    @Test
    public void testFreshTokenIsGeneratedWhenPreviuosTokenExpired() {
        ReflectionTestUtils.setField(sessionTokenManager, "tokenReceivedTime", System.currentTimeMillis() - 5000);
        ReflectionTestUtils.setField(sessionTokenManager, "token", "Expired-Token");
        String responseBody = "{\"token\": \"new-generated-token\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                        Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
                .thenReturn(responseEntity);
        String generatedToken =sessionTokenManager.getToken();
        Assert.assertEquals("new-generated-token", generatedToken);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class), Mockito.eq(String.class));
    }

    @Test
    public void testOldTokenIsRetunedWhenPreviuosTokenValis() {
        ReflectionTestUtils.setField(sessionTokenManager, "tokenReceivedTime", System.currentTimeMillis());
        ReflectionTestUtils.setField(sessionTokenManager, "token", "Old-Token");

        String generatedToken =sessionTokenManager.getToken();
        Assert.assertEquals("Old-Token", generatedToken);
        Mockito.verify(restTemplate, Mockito.never()).exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class), Mockito.eq(String.class));
    }

    @Test
    public void testFailedTokenGeneration() {
        ReflectionTestUtils.setField(sessionTokenManager, "tokenReceivedTime", 0);
        ReflectionTestUtils.setField(sessionTokenManager, "token", "");

        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        Mockito.when(restTemplate.exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                        Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
                .thenThrow(httpClientErrorException);

        String generatedToken = sessionTokenManager.getToken();

        Assert.assertNull(generatedToken);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.eq(authUrl), Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class), Mockito.eq(String.class));
    }

}
