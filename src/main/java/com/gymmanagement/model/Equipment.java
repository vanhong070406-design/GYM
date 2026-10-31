package com.gymmanagement.model;

/**
 * Model đại diện cho Thiết bị (Equipment)
 * Các trường: EquipmentID, EquipmentName, Value, PurchaseDate, Status, AreaID
 */
public class Equipment {
    private String equipmentId;
    private String equipmentName;
    private double value;
    private String purchaseDate;
    private String status; // "Tốt", "Đang hoạt động", "Đang bảo trì", "Cần thay thế"
    private String areaId;

    public Equipment() {}

    public Equipment(String equipmentId, String equipmentName, double value, 
                     String purchaseDate, String status, String areaId) {
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

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    @Override
    public String toString() {
        return equipmentId + " - " + equipmentName;
    }
}
