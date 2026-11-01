package com.gymmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model đại diện cho Nhân viên (Staff); managerId = null khi không có quản lý
 * shift: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class Staff {
    private String staffId;
    private String fullName;
    private String phone;
    private BigDecimal salary;
    private String shiftId;
    private LocalDate hireDate;
    private String managerId;
    private String positionId;
    private String positionName;
    private Shift shift;

    public Staff() {}

    public Staff(String staffId, String fullName, String phone, BigDecimal salary, String shiftId, LocalDate hireDate, String managerId, String positionId) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.phone = phone;
        this.salary = salary;
        this.shiftId = shiftId;
        this.hireDate = hireDate;
        this.managerId = managerId;
        this.positionId = positionId;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    @Override
    public String toString() {
        return staffId + " - " + fullName;
    }
}
