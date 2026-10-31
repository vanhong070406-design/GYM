package com.gymmanagement.model;

/**
 * Model đại diện cho Khu vực (Area)
 * Các trường: AreaID, AreaName, Floor, Description
 */
public class Area {
    private String areaId;
    private String areaName;
    private String floor; // "Tầng 1", "Tầng 2", "Tầng M"
    private String description;

    public Area() {}

    public Area(String areaId, String areaName, String floor, String description) {
        this.areaId = areaId;
        this.areaName = areaName;
        this.floor = floor;
        this.description = description;
    }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return areaId + " - " + areaName + " (" + floor + ")";
    }
}
