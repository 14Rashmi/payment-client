package com.segovia.interview.paymentclient.model;

import java.util.*;

public class CallbackDetail {
    private static Set<String> conversationIds = new HashSet<>();
    private static Set<PaymentStatus> callbackResponse = new HashSet<>();

    private CallbackDetail() {
    }

    // Adding an item to the list
    public static void addConversationId(String id) {
        synchronized (conversationIds) {
            conversationIds.add(id);
        }
    }

    // Removing an item from the list
    public static void removeConversationId(String id) {
        synchronized (conversationIds) {
            conversationIds.remove(id);
        }
    }

    public static void addServerResponse(PaymentStatus paymentStatus) {
        synchronized (callbackResponse) {
            callbackResponse.add(paymentStatus);
        }
    }

    // Removing an item from the list
    public static void removeServerResponse(PaymentStatus paymentStatus) {
        synchronized (callbackResponse) {
            callbackResponse.remove(paymentStatus);
        }
    }

    public static void removeAllServerResponse(Set<PaymentStatus> paymentStatuses) {
        synchronized (callbackResponse) {
            callbackResponse.removeAll(paymentStatuses);
        }
    }

    public static Set<String> getConversationIds() {
        return conversationIds;
    }

    public static Set<PaymentStatus> getServerResponses() {
        return callbackResponse;
    }
}
