package com.gymmanagement.model;

/** Một dòng trên màn hình Tài khoản. */
public class UserAccount {
    private final String username;
    private final String ownerId;
    private final String ownerName;
    private final String positionName;
    private final boolean mustChangePassword;

    public UserAccount(String username, String ownerId, String ownerName, String positionName, boolean mustChangePassword) {
        this.username = username;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.positionName = positionName;
        this.mustChangePassword = mustChangePassword;
    }

    public String getUsername() { return username; }
    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getPositionName() { return positionName; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
