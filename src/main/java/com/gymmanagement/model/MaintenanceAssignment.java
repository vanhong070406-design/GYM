package com.gymmanagement.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Model đại diện cho Phân công bảo trì; khóa chính (staffId, equipmentId, maintenanceDate)
 * staffName, equipmentName: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class MaintenanceAssignment {
    // Trạng thái phiếu, phải khớp ràng buộc CK_Maintenance_Status trong database/01_schema.sql
    public static final String STATUS_IN_PROGRESS = "Đang bảo trì";
    public static final String STATUS_WAITING_PARTS = "Chờ linh kiện";
    public static final String STATUS_DONE = "Đã hoàn thành";
    public static final List<String> STATUSES = List.of(STATUS_IN_PROGRESS, STATUS_WAITING_PARTS, STATUS_DONE);

    private String staffId;
    private String equipmentId;
    private LocalDate maintenanceDate;
    private String note;
    private String status;
    private String staffName;
    private String equipmentName;

    public MaintenanceAssignment() {}

    public MaintenanceAssignment(String staffId, String equipmentId, LocalDate maintenanceDate, String note, String status) {
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
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    @Override
    public String toString() {
        return "Staff: " + staffId + " -> Equipment: " + equipmentId + " (" + status + ")";
    }
}
