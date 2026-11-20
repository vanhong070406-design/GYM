package com.gymmanagement.model;

import java.util.Map;

/** Người đang đăng nhập và quyền của họ. Tài khoản luôn thuộc về một nhân viên (Quản lý hoặc Lễ tân). */
public class UserSession {
    private final String username;
    private final String staffId;
    private final String fullName;
    private final String positionName;
    private final boolean mustChangePassword;
    private final Map<String, AccessLevel> permissions;

    public UserSession(String username, String staffId, String fullName, String positionName,
                       boolean mustChangePassword, Map<String, AccessLevel> permissions) {
        this.username = username;
        this.staffId = staffId;
        this.fullName = fullName;
        this.positionName = positionName;
        this.mustChangePassword = mustChangePassword;
        this.permissions = permissions;
    }

    public String getUsername() { return username; }
    public String getStaffId() { return staffId; }
    public String getFullName() { return fullName; }
    public String getPositionName() { return positionName; }
    public boolean isMustChangePassword() { return mustChangePassword; }

    public boolean hasAnyPermission() { return !permissions.isEmpty(); }
    public boolean canView(String screen) { return permissions.containsKey(screen); }
    public boolean canManage(String screen) { return permissions.get(screen) == AccessLevel.MANAGE; }
}
