package com.abdil.taxi.model;

public class PaymentResponse {
    private String transactionId;
    private String status;
    private Double amount;
    private String paymentMethod;
    private String message;
    private String paymentUrl;  // Pour carte bancaire

    public PaymentResponse() {}

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public Double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getMessage() { return message; }
    public String getPaymentUrl() { return paymentUrl; }

    // Setters
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setStatus(String status) { this.status = status; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setMessage(String message) { this.message = message; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}