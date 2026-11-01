package com.gymmanagement.model;

/** Chức vụ. */
public class Position {
    public static final String MANAGER = "QL";
    public static final String RECEPTIONIST = "LT";
    public static final String TECHNICIAN = "KT";
    public static final String CLEANER = "TV";

    private final String positionId;
    private final String positionName;

    public Position(String positionId, String positionName) {
        this.positionId = positionId;
        this.positionName = positionName;
    }

    public String getPositionId() { return positionId; }
    public String getPositionName() { return positionName; }

    @Override
    public String toString() {
        return positionName;
    }
}
