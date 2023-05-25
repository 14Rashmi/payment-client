package com.segovia.interview.paymentclient.model;

import com.opencsv.bean.CsvBindByName;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public class PaymentRequest {

    @CsvBindByName(column = "Recipient")
    private String msisdn;
    @CsvBindByName(column = "Amount")
    private BigDecimal amount;
    @CsvBindByName(column = "Currency")
    private String currency;
    @CsvBindByName(column = "ID")
    private String reference;

    @Value("http://api.example.com")
    private String url;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "{" +
                "msisdn='" + msisdn + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", reference='" + reference + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
