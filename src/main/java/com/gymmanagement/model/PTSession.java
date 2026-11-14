package com.gymmanagement.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho Lịch tập PT (PTSession)
 * trainerName, memberName: chỉ đọc, lấy từ JOIN để hiển thị, không lưu trong bảng này
 */
public class PTSession {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String trainerId;
    private String memberId;
    private String memberName;
    private String trainerName;

    public PTSession() {}

    public PTSession(String sessionId, LocalDateTime startTime, LocalDateTime endTime, String status, String trainerId, String memberId) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.trainerId = trainerId;
        this.memberId = memberId;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    @Override
    public String toString() {
        return sessionId + " (" + startTime + " - " + endTime + ")";
    }
}