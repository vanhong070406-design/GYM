package com.gymmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model đại diện cho Thanh toán (Payment)
 */
public class Payment {
    private String paymentId;
    private LocalDate paymentDate;
    private String paymentMethod;
    private BigDecimal amount;
    private String subscriptionId;
    private String receptionistId;

    public Payment() {}

    public Payment(String paymentId, LocalDate paymentDate, String paymentMethod, BigDecimal amount, String subscriptionId, String receptionistId) {
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.subscriptionId = subscriptionId;
        this.receptionistId = receptionistId;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getReceptionistId() { return receptionistId; }
    public void setReceptionistId(String receptionistId) { this.receptionistId = receptionistId; }

    @Override
    public String toString() {
        return paymentId + " - " + amount + " VND";
    }
}
