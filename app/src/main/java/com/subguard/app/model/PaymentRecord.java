package com.subguard.app.model;

public class PaymentRecord {
    private int id;
    private int subscriptionId;
    private double amount;
    private String paymentDate;
    private String subscriptionName;

    public PaymentRecord() {}

    public PaymentRecord(int subscriptionId, double amount, String paymentDate, String subscriptionName) {
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.subscriptionName = subscriptionName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public String getSubscriptionName() { return subscriptionName; }
    public void setSubscriptionName(String subscriptionName) { this.subscriptionName = subscriptionName; }
}
