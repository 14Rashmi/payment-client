package com.segovia.interview.paymentclient.model;

public class PaymentStatus {

    private int status;
    private String timestamp;

    private String reference;

    private String message;

    private String customerReference;
    private String fee;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "PayResponse{" +
                "status=" + status +
                ", timestamp='" + timestamp + '\'' +
                ", reference='" + reference + '\'' +
                ", message='" + message + '\'' +
                ", customerReference='" + customerReference + '\'' +
                ", fee=" + fee +
                '}';
    }
}
