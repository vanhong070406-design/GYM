package com.gymmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Model đại diện cho Thiết bị (Equipment)
 */
public class Equipment {
    // Trạng thái thiết bị, phải khớp ràng buộc CK_Equipment_Status trong database/01_schema.sql
    public static final String STATUS_GOOD = "Hoạt động tốt";
    public static final String STATUS_NEEDS_PARTS = "Cần thay thế linh kiện";
    public static final String STATUS_MAINTENANCE_DUE = "Tới hạn bảo trì định kỳ";
    public static final List<String> STATUSES = List.of(STATUS_GOOD, STATUS_NEEDS_PARTS, STATUS_MAINTENANCE_DUE);

    private String equipmentId;
    private String equipmentName;
    private BigDecimal value;
    private LocalDate purchaseDate;
    private String status;

    public Equipment() {}

    public Equipment(String equipmentId, String equipmentName, BigDecimal value, LocalDate purchaseDate, String status) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.value = value;
        this.purchaseDate = purchaseDate;
        this.status = status;
    }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return equipmentId + " - " + equipmentName;
    }
}