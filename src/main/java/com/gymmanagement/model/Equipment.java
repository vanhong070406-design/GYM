package com.gymmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model đại diện cho Thiết bị (Equipment)
 */
public class Equipment {
    private String equipmentId;
    private String equipmentName;
    private BigDecimal value;
    private LocalDate purchaseDate;
    private String status;
    private String areaId;

    public Equipment() {}

    public Equipment(String equipmentId, String equipmentName, BigDecimal value, LocalDate purchaseDate, String status, String areaId) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.value = value;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.areaId = areaId;
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
    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    @Override
    public String toString() {
        return equipmentId + " - " + equipmentName;
    }
}
