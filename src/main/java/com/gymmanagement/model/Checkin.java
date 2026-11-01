package com.gymmanagement.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho Check-in (Checkin); checkoutTime = null khi chưa check-out
 * memberName: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class Checkin {
    private String checkinId;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String memberId;
    private String receptionistId;
    private String memberName;

    public Checkin() {}

    public Checkin(String checkinId, LocalDateTime checkinTime, LocalDateTime checkoutTime, String memberId, String receptionistId) {
        this.checkinId = checkinId;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.memberId = memberId;
        this.receptionistId = receptionistId;
    }

    public String getCheckinId() { return checkinId; }
    public void setCheckinId(String checkinId) { this.checkinId = checkinId; }
    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getReceptionistId() { return receptionistId; }
    public void setReceptionistId(String receptionistId) { this.receptionistId = receptionistId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    @Override
    public String toString() {
        return checkinId + " - Member: " + memberId + " (" + checkinTime + ")";
    }
}
