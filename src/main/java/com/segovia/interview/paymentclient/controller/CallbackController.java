package com.segovia.interview.paymentclient.controller;

import com.segovia.interview.paymentclient.model.CallbackDetail;
import com.segovia.interview.paymentclient.model.PaymentStatus;
import com.segovia.interview.paymentclient.service.PaymentProcessor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Api(tags = "Callback")
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);
    @Autowired
    PaymentProcessor paymentProcessor;

    @PostMapping("/callback")
    @ApiOperation(value = "Callback Url", notes = "This API will be called by the Docker instance once the Transaction is completed")
    public ResponseEntity<Void> handleCallback(@RequestBody PaymentStatus callbackPayload) {
        logger.info("Callback Payload is {}", callbackPayload);
        // Process the callback payload
        paymentProcessor.processCallback(callbackPayload, CallbackDetail.getConversationIds());
        // Return a success response
        return ResponseEntity.ok().build();
    }
}
