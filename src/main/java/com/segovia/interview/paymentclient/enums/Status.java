package com.segovia.interview.paymentclient.enums;

public enum Status {
    SUCCEEDED(0, "Transaction succeeded"),
    PENDING(100, "Transaction pending"),
    INVALID(1000,"Invalid request"),
    PHONE_INVALID(20000, "Recipient phone number was not valid"),
    RECEPIENT_ACCOUNT_LOCKED(20001, "Recipient account is locked"),
    SENDER_ACCOUNT_LOCKED(20002, "Sender account is locked"),
    RECEPIENT_WALLET_FULL(20003, "Recipient wallet is full"),
    SENDER_BAL_INSIFFICIENT(20004, "Insufficient balance in sender account"),
    TEMPORARY_FAILURE(20005, "Temporary failure"),
    RECIPIENT_PHONE_NOT_REGISTERED(20014, "Recipient phone number isn't registered for mobile money"),
    DUPLICATE(30006, "Duplicate reference"),
    FAILED(1, "Failed"),
    UNKNOWN(2, "Unknown");

    private int code;
    private String description;
    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionFromCode(int code) {
        for (Status status: Status.values()) {
            if (status.code == code) {
                return status.description;
            }
        }
        return "Unknown";
    }

    public static int getCode(Status status) {
        return status.code;
    }


}
