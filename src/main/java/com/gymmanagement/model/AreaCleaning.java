package com.gymmanagement.model;

import java.time.LocalDate;

/**
 * Model đại diện cho Phân công vệ sinh; khóa chính (areaId, cleaningDate, shiftId)
 * staffName, areaName, shiftName: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class AreaCleaning {
    private String staffId;
    private String areaId;
    private LocalDate cleaningDate;
    private String shiftId;
    private String status;
    private String staffName;
    private String areaName;
    private String shiftName;

    public AreaCleaning() {}

    public AreaCleaning(String staffId, String areaId, LocalDate cleaningDate, String shiftId, String status) {
        this.staffId = staffId;
        this.areaId = areaId;
        this.cleaningDate = cleaningDate;
        this.shiftId = shiftId;
        this.status = status;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }
    public LocalDate getCleaningDate() { return cleaningDate; }
    public void setCleaningDate(LocalDate cleaningDate) { this.cleaningDate = cleaningDate; }
    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    @Override
    public String toString() {
        return "Staff: " + staffId + " -> Area: " + areaId + " (" + shiftId + ")";
    }
}
