package com.segovia.interview.paymentclient.service;

import com.segovia.interview.paymentclient.enums.Status;
import com.segovia.interview.paymentclient.model.CallbackDetail;
import com.segovia.interview.paymentclient.model.PaymentRequest;
import com.segovia.interview.paymentclient.model.PaymentResponse;
import com.segovia.interview.paymentclient.model.PaymentStatus;
import com.segovia.interview.paymentclient.util.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class PaymentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessor.class);

    private static final String OS_NAME = "os.name";
    private static final String WIN = "win";
    private static final String MAC = "mac";
    @Value("${tokenTimeOut}")
    private int tokenTimeOut;

    @Value("${callbackTimeOut}")
    private int callbackTimeOut;

    @Value("${callbackEnabled}")
    private boolean callbackEnabled;

    RestTemplate restTemplate;

    SessionTokenManager sessionTokenManager;

    @Value("${payUrl}")
    private String payUrl;

    @Value("${statusUrl}")
    private String statusUrl;

    CsvHelper csvHelper;

    private boolean csvReadComplete = false;

    @Autowired
    public PaymentProcessor(RestTemplate restTemplate, SessionTokenManager sessionTokenManager, CsvHelper csvHelper) {
        this.restTemplate = restTemplate;
        this.sessionTokenManager = sessionTokenManager;
        this.csvHelper = csvHelper;
    }

    /*
    Makes payments for the list of payments read from CSV
     */
    public void submitOutgoingPayment(List<PaymentRequest> paymentRequests) {
        String callbackUrl = getCallbackUrl();
        paymentRequests.forEach(paymentRequest -> {
            if (callbackEnabled) {
                paymentRequest.setUrl(callbackUrl);
            }
            try {
                initiateSinglePayment(paymentRequest);
            } catch (HttpClientErrorException httpClientErrorException) {
                if (httpClientErrorException.getStatusCode() == HttpStatus.FORBIDDEN && System.currentTimeMillis() - SessionTokenManager.getTokenReceivedTime() >= tokenTimeOut) {
                    //If the token gets expired by the time the request is made, this will reset the Token and token received time
                    initiateSinglePayment(paymentRequest);
                }
            } catch (Exception e) {
                PaymentStatus paymentStatus = getPaymentStatusForErrorCondition(paymentRequest, "Server was unable to process the request", Status.getCode(Status.UNKNOWN));
                CallbackDetail.addServerResponse(paymentStatus);
            }
        });
        csvReadComplete = true; // Setting it to true so, that the CSV writing will be done if all callbacks received

        if (callbackEnabled) {
            startCallbackTimeoutTimer();
        } else {
            pollForResult();
        }
    }

    /*
    Makes the payment for each of the single payment items
     */
    public void initiateSinglePayment(PaymentRequest paymentRequest) {
        HttpHeaders headers = getHttpHeaders(sessionTokenManager.getToken());
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);
        ResponseEntity<PaymentResponse> paymentResponse = restTemplate.exchange(payUrl, HttpMethod.POST, entity, PaymentResponse.class);
        if (paymentResponse.getBody() != null && paymentResponse.getStatusCode().is2xxSuccessful()) {
            String conversationId = paymentResponse.getBody().getConversationID();
            if (conversationId != null) {
                CallbackDetail.addConversationId(conversationId);
            } else {
                PaymentStatus paymentStatus = getPaymentStatusForErrorCondition(paymentRequest, paymentResponse.getBody().getMessage(), Status.getCode(Status.FAILED));
                CallbackDetail.addServerResponse(paymentStatus);
            }
        }
    }

    /*
    Creating the Payment Response from Server for Failed Scenarios
     */
    private PaymentStatus getPaymentStatusForErrorCondition(PaymentRequest paymentRequest, String message, int status) {
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setReference("");
        paymentStatus.setCustomerReference(paymentRequest.getReference());
        paymentStatus.setFee("");
        paymentStatus.setStatus(status);
        paymentStatus.setMessage(message);
        return paymentStatus;
    }

    private void startCallbackTimeoutTimer() {
        new Thread(() -> {
            try {
                Thread.sleep(callbackTimeOut);
                callbackEnabled = false;
                pollForResult();
            } catch (InterruptedException e) {
                logger.warn("Callback Timer interrupted");
            }
        }).start();
    }

    /*
    Find the Callback URL based on the OS
     */
    private String getCallbackUrl() {
        String os = System.getProperty(OS_NAME).toLowerCase();
        if (os.contains(WIN) || os.contains(MAC)) {
            return "http://host.docker.internal:8080/callback";
        } else {
            logger.info("Operating System: Other");
        }
        //TODO: Implement Host name for other OS
        return "http://host.docker.internal:8080/callback";
    }

    public void processCallback(PaymentStatus callbackPayload, Set<String> existingConversationIds)  {
        if (existingConversationIds != null && existingConversationIds.contains(callbackPayload.getReference())) { // Checking the condition to ensure this is not an old request which is not part of session
            // Perform any necessary actions based on the callback data
            CallbackDetail.removeConversationId(callbackPayload.getReference());
            CallbackDetail.addServerResponse(callbackPayload);
            Set<PaymentStatus> paymentStatusesReceived = CallbackDetail.getServerResponses();
            if (csvReadComplete && CallbackDetail.getConversationIds().isEmpty() && !paymentStatusesReceived.isEmpty()) {
                csvHelper.writeCsv(CallbackDetail.getServerResponses());
                CallbackDetail.removeAllServerResponse(paymentStatusesReceived);
            }
        }
    }

    @Scheduled(fixedDelay = 60000) // Poll every 1 minute
    public void pollForResult() {
        if (!callbackEnabled) {
            Set<String> ids = CallbackDetail.getConversationIds();
            synchronized (ids) {
                Iterator<String> iterator = ids.iterator();
                while (iterator.hasNext()) {
                    String id = iterator.next();
                    HttpHeaders headers = getHttpHeaders(sessionTokenManager.getToken());
                    HttpEntity<PaymentRequest> entity = new HttpEntity<>(null, headers);
                    PaymentStatus paymentStatus = restTemplate.exchange(statusUrl + id, HttpMethod.GET, entity, PaymentStatus.class).getBody();
                    if (paymentStatus.getStatus() != Status.getCode(Status.PENDING)) {
                        iterator.remove();
                        CallbackDetail.addServerResponse(paymentStatus);
                    }
                }
            }
            Set<PaymentStatus> paymentStatusesReceived = CallbackDetail.getServerResponses();
            if (csvReadComplete && CallbackDetail.getConversationIds().isEmpty() && !paymentStatusesReceived.isEmpty()) {
                csvHelper.writeCsv(CallbackDetail.getServerResponses());
                CallbackDetail.removeAllServerResponse(paymentStatusesReceived);
            }
        }
    }

    private HttpHeaders getHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
