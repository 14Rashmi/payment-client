package com.segovia.interview.paymentclient.controller;

import com.segovia.interview.paymentclient.model.CallbackDetail;
import com.segovia.interview.paymentclient.model.PaymentStatus;
import com.segovia.interview.paymentclient.service.PaymentProcessor;
import org.apache.juli.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);
    @Autowired
    PaymentProcessor paymentProcessor;

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody PaymentStatus callbackPayload) throws IOException {
        logger.info("Callback Payload is {}", callbackPayload);
        // Process the callback payload
        paymentProcessor.processCallback(callbackPayload, CallbackDetail.getConversationIds());
        // Return a success response
        return ResponseEntity.ok().build();
    }
}
