package com.gymmanagement.model;

/**
 * Model đại diện cho Phân công vệ sinh (AreaCleaning)
 * Các trường chính: StaffID, AreaID
 * Các trường bổ sung: CleaningDate, Shift, Status
 */
public class AreaCleaning {
    private String staffId;
    private String areaId;
    private String cleaningDate;
    private String shift;  // "Ca sáng", "Ca chiều", "Ca đêm"
    private String status; // "Đã hoàn thành", "Đang thực hiện", "Chưa làm"

    public AreaCleaning() {}

    public AreaCleaning(String staffId, String areaId, String cleaningDate, String shift, String status) {
        this.staffId = staffId;
        this.areaId = areaId;
        this.cleaningDate = cleaningDate;
        this.shift = shift;
        this.status = status;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    public String getCleaningDate() { return cleaningDate; }
    public void setCleaningDate(String cleaningDate) { this.cleaningDate = cleaningDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Staff: " + staffId + " -> Area: " + areaId + " (" + shift + ")";
    }
}
