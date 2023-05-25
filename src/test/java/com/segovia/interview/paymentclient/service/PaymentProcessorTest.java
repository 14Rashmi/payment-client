package com.segovia.interview.paymentclient.service;
import com.segovia.interview.paymentclient.model.*;
import com.segovia.interview.paymentclient.util.CsvHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PaymentProcessorTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SessionTokenManager sessionTokenManager;

    @Mock
    private CsvHelper csvHelper;

    private PaymentProcessor paymentProcessor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        paymentProcessor = new PaymentProcessor(restTemplate, sessionTokenManager, csvHelper);
        ReflectionTestUtils.setField(paymentProcessor, "payUrl", "mockedPayUrl");
    }

    @Test
    public void testMakeOutgoingPayment_WithCallbackEnabled_ShouldInitiateSinglePayment() {
        // Arrange
        List<PaymentRequest> paymentRequests = Collections.singletonList(new PaymentRequest());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        when(sessionTokenManager.getToken()).thenReturn("mockedToken");
        when(restTemplate.exchange(eq("mockedPayUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(new ResponseEntity<>(new PaymentResponse(), HttpStatus.OK));

        // Act
        paymentProcessor.submitOutgoingPayment(paymentRequests);

        verify(restTemplate, times(1)).exchange(eq("mockedPayUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));
    }

    @Test
    public void testMakeOutgoingPayment_WithCallbackEnabled_AndTokenExpired_ShouldResetTokenAndRetry() {
        // Arrange
        List<PaymentRequest> paymentRequests = Collections.singletonList(new PaymentRequest());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        when(sessionTokenManager.getToken()).thenReturn("mockedToken");
        when(restTemplate.exchange(eq("mockedPayUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
                .thenReturn(new ResponseEntity<>(new PaymentResponse(), HttpStatus.OK));
        ReflectionTestUtils.setField(paymentProcessor, "callbackEnabled", false);
        // Act
        paymentProcessor.submitOutgoingPayment(paymentRequests);

        // Assert
        verify(sessionTokenManager, times(2)).getToken();
        verify(restTemplate, times(2)).exchange(eq("mockedPayUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));
    }


    @Test
    public void testPollForResult_ShouldNotWriteToCsvIfNoResponseFromServer() {
        // Arrange
        ReflectionTestUtils.setField(paymentProcessor, "csvReadComplete", true);
        when(sessionTokenManager.getToken()).thenReturn("mockedToken");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentStatus.class)))
                .thenReturn(new ResponseEntity<>(new PaymentStatus(), HttpStatus.OK));
        doNothing().when(csvHelper).writeCsv(any());
        // Act
        paymentProcessor.pollForResult();

        // Assert
        verify(sessionTokenManager, times(0)).getToken();

    }

    @Test
    public void testInitiateSinglePayment_Successful() {
        // Mock the required objects
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mockToken");
        PaymentRequest paymentRequest = new PaymentRequest(/* initialize with required data */);
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setConversationID("mockConversationId");

        // Mock the behavior of the dependencies
        when(sessionTokenManager.getToken()).thenReturn("mockToken");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(new ResponseEntity<>(paymentResponse, HttpStatus.OK));

        // Call the method under test
        paymentProcessor.initiateSinglePayment(paymentRequest);

        // Verify the expected behavior
        verify(sessionTokenManager, times(1)).getToken();
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));
        // Assert any other expected behavior or outcomes
    }

    @Test
    public void testInitiateSinglePayment_NullConversationId() {
        // Mock the required objects
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mockToken");
        PaymentRequest paymentRequest = new PaymentRequest(/* initialize with required data */);
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setMessage("mockErrorMessage");

        // Mock the behavior of the dependencies
        when(sessionTokenManager.getToken()).thenReturn("mockToken");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(new ResponseEntity<>(paymentResponse, HttpStatus.OK));

        // Call the method under test
        paymentProcessor.initiateSinglePayment(paymentRequest);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));
        //No Exception is thrown
    }

    @Test
    public void testInitiateSinglePaymentAgainIfTokenExpires() {
        // Mock the required objects and data
        List<PaymentRequest> paymentRequests = new ArrayList<>();
        PaymentRequest paymentRequest = new PaymentRequest(/* initialize with required data */);
        paymentRequests.add(paymentRequest);

            ResponseEntity<PaymentResponse> successResponse = ResponseEntity.ok(new PaymentResponse());

        // Mock the RestTemplate behavior
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
                .thenReturn(successResponse);
        // Call the method under test
        paymentProcessor.submitOutgoingPayment(paymentRequests);

        // Verify the expected behavior
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));

    }

    @Test
    public void testDoNotInitiateSinglePaymentAgainIfAnyOtherException() {
        // Mock the required objects and data
        List<PaymentRequest> paymentRequests = new ArrayList<>();
        PaymentRequest paymentRequest = new PaymentRequest(/* initialize with required data */);
        paymentRequests.add(paymentRequest);

        ResponseEntity<PaymentResponse> successResponse = ResponseEntity.ok(new PaymentResponse());

        // Mock the RestTemplate behavior
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(successResponse);
        // Call the method under test
        paymentProcessor.submitOutgoingPayment(paymentRequests);

        // Verify the expected behavior
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(PaymentResponse.class));

    }

    @Test
    public void doNotProcessCsvIfUnknownOrPreviousSessionIdReceivedOnCallback() {
        PaymentStatus callbackPayload = new PaymentStatus();
        callbackPayload.setReference("1");
        Set<String> conersationIds = new HashSet<>(Arrays.asList("2"));
        paymentProcessor.processCallback(callbackPayload, conersationIds);
        verify(csvHelper, never()).writeCsv(any());

    }

}
