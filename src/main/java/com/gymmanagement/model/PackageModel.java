package com.gymmanagement.model;

/**
 * Model đại diện cho Gói tập (Package)
 * Các trường: PackageID, PackageName, Price, Duration, Description
 */
public class PackageModel {
    private String packageId;
    private String packageName;
    private double price;
    private int duration; // Số ngày hoặc số tháng (quy chuẩn số ngày để dễ tính toán)
    private String description;

    public PackageModel() {}

    public PackageModel(String packageId, String packageName, double price, int duration, String description) {
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

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return packageId + " - " + packageName;
    }
}
