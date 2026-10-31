package com.gymmanagement.model;

/**
 * Model đại diện cho Nhân viên (Staff)
 * Các trường: StaffID, FullName, Phone, Salary, Shift, HireDate, ManagerID
 */
public class Staff {
    private String staffId;
    private String fullName;
    private String phone;
    private double salary;
    private String shift; // "Ca sáng (06:00 - 14:00)", "Ca chiều (14:00 - 22:00)", "Ca hành chính"
    private String hireDate;
    private String managerId;

    public Staff() {}

    public Staff(String staffId, String fullName, String phone, double salary, 
                 String shift, String hireDate, String managerId) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.phone = phone;
        this.salary = salary;
        this.shift = shift;
        this.hireDate = hireDate;
        this.managerId = managerId;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    @Override
    public String toString() {
        return staffId + " - " + fullName;
    }
}
