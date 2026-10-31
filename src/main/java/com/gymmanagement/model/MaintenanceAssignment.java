package com.gymmanagement.model;

/**
 * Model đại diện cho Quản lý bảo trì thiết bị (MaintenanceAssignment)
 * Các trường chính: StaffID, EquipmentID
 * Các trường bổ sung: MaintenanceDate, Note, Status
 */
public class MaintenanceAssignment {
    private String staffId;
    private String equipmentId;
    private String maintenanceDate;
    private String note;
    private String status; // "Đã hoàn thành", "Đang bảo trì", "Chờ linh kiện"

    public MaintenanceAssignment() {}

    public MaintenanceAssignment(String staffId, String equipmentId, String maintenanceDate, String note, String status) {
        this.staffId = staffId;
        this.equipmentId = equipmentId;
        this.maintenanceDate = maintenanceDate;
        this.note = note;
        this.status = status;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public String getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(String maintenanceDate) { this.maintenanceDate = maintenanceDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Staff: " + staffId + " -> Equipment: " + equipmentId + " (" + status + ")";
    }
}
