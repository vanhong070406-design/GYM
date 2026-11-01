package com.gymmanagement.model;

import java.util.Map;

/** Người đang đăng nhập và quyền của họ. Đúng một trong staffId, trainerId có giá trị. */
public class UserSession {
    private final String username;
    private final String staffId;
    private final String trainerId;
    private final String fullName;
    private final String positionName;
    private final boolean mustChangePassword;
    private final Map<String, AccessLevel> permissions;

    public UserSession(String username, String staffId, String trainerId, String fullName, String positionName,
                       boolean mustChangePassword, Map<String, AccessLevel> permissions) {
        this.username = username;
        this.staffId = staffId;
        this.trainerId = trainerId;
        this.fullName = fullName;
        this.positionName = positionName;
        this.mustChangePassword = mustChangePassword;
        this.permissions = permissions;
    }

    public String getUsername() { return username; }
    public String getStaffId() { return staffId; }
    public String getTrainerId() { return trainerId; }
    public String getFullName() { return fullName; }
    public String getPositionName() { return positionName; }
    public boolean isMustChangePassword() { return mustChangePassword; }

    public boolean hasAnyPermission() { return !permissions.isEmpty(); }
    public boolean canView(String screen) { return permissions.containsKey(screen); }
    public boolean isOwnOnly(String screen) { return permissions.get(screen) == AccessLevel.OWN; }
    public boolean canManage(String screen) { return permissions.get(screen) == AccessLevel.MANAGE; }
}
