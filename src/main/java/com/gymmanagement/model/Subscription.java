package com.gymmanagement.model;

import java.time.LocalDate;

/**
 * Model đại diện cho Đăng ký gói tập (Subscription)
 * memberName: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class Subscription {
    private String subscriptionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String memberId;
    private String packageId;
    private String memberName;

    public Subscription() {}

    public Subscription(String subscriptionId, LocalDate startDate, LocalDate endDate, String status, String memberId, String packageId) {
        this.subscriptionId = subscriptionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.memberId = memberId;
        this.packageId = packageId;
    }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    @Override
    public String toString() {
        return subscriptionId + " (" + memberId + " - " + packageId + ")";
    }
}
