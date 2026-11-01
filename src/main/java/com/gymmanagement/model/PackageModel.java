package com.gymmanagement.model;

import java.math.BigDecimal;

/**
 * Model đại diện cho Gói tập (Package)
 */
public class PackageModel {
    private String packageId;
    private String packageName;
    private BigDecimal price;
    private int duration;
    private String description;

    public PackageModel() {}

    public PackageModel(String packageId, String packageName, BigDecimal price, int duration, String description) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.duration = duration;
        this.description = description;
    }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return packageId + " - " + packageName;
    }
}
