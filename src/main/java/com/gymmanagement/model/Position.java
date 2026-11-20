package com.gymmanagement.model;

/** Chức vụ. canLogin: nhân viên chức vụ này được cấp tài khoản đăng nhập (hiện chỉ Quản lý và Lễ tân). */
public class Position {
    public static final String MANAGER = "QL";
    public static final String RECEPTIONIST = "LT";
    public static final String TECHNICIAN = "KT";

    private final String positionId;
    private final String positionName;
    private final boolean canLogin;

    public Position(String positionId, String positionName, boolean canLogin) {
        this.positionId = positionId;
        this.positionName = positionName;
        this.canLogin = canLogin;
    }

    public String getPositionId() { return positionId; }
    public String getPositionName() { return positionName; }
    public boolean canLogin() { return canLogin; }

    @Override
    public String toString() {
        return positionName;
    }
}