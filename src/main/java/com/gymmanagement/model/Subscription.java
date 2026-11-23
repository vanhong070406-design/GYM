package com.gymmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Model đại diện cho Đăng ký gói tập (Subscription)
 * price: giá gói chốt lúc đăng ký (cột Subscription.Price); gói đổi giá sau đó không làm đổi số tiền phải thu.
 * memberName, packageName, durationDays, paidAmount: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này.
 */
public class Subscription {
    public static final String STATUS_ACTIVE = "Đang hoạt động";
    public static final String STATUS_EXPIRED = "Hết hạn";
    public static final String STATUS_PAUSED = "Tạm dừng";
    public static final List<String> STATUSES = List.of(STATUS_ACTIVE, STATUS_EXPIRED, STATUS_PAUSED);
    /** Còn từ chừng này ngày trở xuống thì coi là sắp hết hạn. */
    public static final int EXPIRING_DAYS = 7;

    /** Tình trạng để hiển thị; "Sắp hết hạn" chỉ tính ra, không lưu trong database. */
    public enum State { ACTIVE, EXPIRING, EXPIRED, PAUSED }

    private String subscriptionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String memberId;
    private String packageId;
    private String memberName;
    private String packageName;
    private BigDecimal price = BigDecimal.ZERO;
    private int durationDays;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    public Subscription() {}

    public Subscription(String subscriptionId, LocalDate startDate, LocalDate endDate, String status, String memberId, String packageId) {
        this.subscriptionId = subscriptionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.memberId = memberId;
        this.packageId = packageId;
    }

    /** Số ngày từ hôm nay đến ngày kết thúc; âm nếu đã qua ngày kết thúc. Ngày kết thúc vẫn còn tập được. */
    public long daysLeft(LocalDate today) {
        return ChronoUnit.DAYS.between(today, endDate);
    }

    public State state(LocalDate today) {
        if (STATUS_PAUSED.equals(status)) return State.PAUSED;
        if (STATUS_EXPIRED.equals(status) || daysLeft(today) < 0) return State.EXPIRED;
        if (daysLeft(today) <= EXPIRING_DAYS) return State.EXPIRING;
        return State.ACTIVE;
    }

    /** Số tiền còn phải thu so với giá chốt lúc đăng ký; không âm. */
    public BigDecimal remaining() {
        return price.subtract(paidAmount).max(BigDecimal.ZERO);
    }

    public boolean isFullyPaid() {
        return remaining().signum() == 0;
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
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price == null ? BigDecimal.ZERO : price; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount; }

    /** Hiện trong ô chọn đăng ký ở màn Thanh toán. */
    @Override
    public String toString() {
        String member = memberName == null ? memberId : memberName;
        String pkg = packageName == null ? packageId : packageName;
        return subscriptionId + " – " + member + " · " + pkg;
    }
}
