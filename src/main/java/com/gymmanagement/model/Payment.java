package com.gymmanagement.model;

/**
 * Model đại diện cho Thanh toán (Payment)
 * Các trường: PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID
 */
public class Payment {
    private String paymentId;
    private String paymentDate;
    private String paymentMethod; // "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"
    private double amount;
    private String subscriptionId;
    private String receptionistId;

    public Payment() {}

    public Payment(String paymentId, String paymentDate, String paymentMethod, 
                   double amount, String subscriptionId, String receptionistId) {
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.subscriptionId = subscriptionId;
        this.receptionistId = receptionistId;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getReceptionistId() { return receptionistId; }
    public void setReceptionistId(String receptionistId) { this.receptionistId = receptionistId; }

    @Override
    public String toString() {
        return paymentId + " - " + amount + " VND";
    }
}
