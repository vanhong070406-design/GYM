package com.gymmanagement.model;

import java.time.LocalTime;

/**
 * Model đại diện cho Ca làm việc (Shift)
 * Các trường: ShiftID, ShiftName, StartTime, EndTime
 */
public class Shift {
    private final String shiftId;
    private final String shiftName;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Shift(String shiftId, String shiftName, LocalTime startTime, LocalTime endTime) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getShiftId() { return shiftId; }
    public String getShiftName() { return shiftName; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    @Override
    public String toString() {
        return shiftName + " (" + startTime + " - " + endTime + ")";
    }
}
