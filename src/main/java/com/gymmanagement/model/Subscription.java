package com.gymmanagement.model;

/**
 * Model đại diện cho Đăng ký gói tập (Subscription)
 * Các trường: SubscriptionID, StartDate, EndDate, Status, MemberID, PackageID
 */
public class Subscription {
    private String subscriptionId;
    private String startDate;
    private String endDate;
    private String status; // "Đang hoạt động", "Hết hạn", "Tạm dừng"
    private String memberId;
    private String packageId;

    public Subscription() {}

    public Subscription(String subscriptionId, String startDate, String endDate, 
                        String status, String memberId, String packageId) {
        this.subscriptionId = subscriptionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.memberId = memberId;
        this.packageId = packageId;
    }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    @Override
    public String toString() {
        return subscriptionId + " (" + memberId + " - " + packageId + ")";
    }
}
