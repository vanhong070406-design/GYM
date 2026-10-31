package com.gymmanagement.model;

/**
 * Model đại diện cho Lịch tập PT (PTSession)
 * Các trường: SessionID, StartTime, EndTime, Status, TrainerID, AreaID
 */
public class PTSession {
    private String sessionId;
    private String startTime;
    private String endTime;
    private String status; // "Đã đặt", "Đang diễn ra", "Hoàn thành", "Đã hủy"
    private String trainerId;
    private String areaId;

    public PTSession() {}

    public PTSession(String sessionId, String startTime, String endTime, 
                     String status, String trainerId, String areaId) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.trainerId = trainerId;
        this.areaId = areaId;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    @Override
    public String toString() {
        return sessionId + " (" + startTime + " - " + endTime + ")";
    }
}
