package com.segovia.interview.paymentclient.service;

import com.segovia.interview.paymentclient.model.PaymentRequest;
import com.segovia.interview.paymentclient.model.PaymentStatus;

import java.util.List;
import java.util.Set;

public interface PaymentProcessor {

    void submitOutgoingPayment(List<PaymentRequest> paymentRequests);

    void processCallback(PaymentStatus callbackPayload, Set<String> existingConversationIds);
}
