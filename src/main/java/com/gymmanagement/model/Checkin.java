package com.gymmanagement.model;

/**
 * Model đại diện cho Check-in (Checkin)
 * Các trường: CheckinID, CheckinTime, CheckoutTime, MemberID, ReceptionistID
 */
public class Checkin {
    private String checkinId;
    private String checkinTime;
    private String checkoutTime;
    private String memberId;
    private String receptionistId;

    public Checkin() {}

    public Checkin(String checkinId, String checkinTime, String checkoutTime, 
                   String memberId, String receptionistId) {
        this.checkinId = checkinId;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.memberId = memberId;
        this.receptionistId = receptionistId;
    }

    public String getCheckinId() { return checkinId; }
    public void setCheckinId(String checkinId) { this.checkinId = checkinId; }

    public String getCheckinTime() { return checkinTime; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }

    public String getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(String checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getReceptionistId() { return receptionistId; }
    public void setReceptionistId(String receptionistId) { this.receptionistId = receptionistId; }

    @Override
    public String toString() {
        return checkinId + " - Member: " + memberId + " (" + checkinTime + ")";
    }
}
