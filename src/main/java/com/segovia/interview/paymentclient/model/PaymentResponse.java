package com.segovia.interview.paymentclient.model;

public class PaymentResponse {

    private int status;
    private String message;
    private String conversationID;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", conversationID='" + conversationID + '\'' +
                '}';
    }
}
