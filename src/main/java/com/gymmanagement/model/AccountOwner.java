package com.gymmanagement.model;

/** Nhân viên hoặc HLV chưa có tài khoản. Đúng một trong staffId, trainerId có giá trị. */
public class AccountOwner {
    private final String staffId;
    private final String trainerId;
    private final String fullName;
    private final String positionName;

    public AccountOwner(String staffId, String trainerId, String fullName, String positionName) {
        this.staffId = staffId;
        this.trainerId = trainerId;
        this.fullName = fullName;
        this.positionName = positionName;
    }

    public String getStaffId() { return staffId; }
    public String getTrainerId() { return trainerId; }

    @Override
    public String toString() {
        return (staffId != null ? staffId : trainerId) + " - " + fullName + " (" + positionName + ")";
    }
}
