package com.gymmanagement.dao;

import com.gymmanagement.model.PTSession;
import java.time.LocalDateTime;
import java.util.List;

public class PTSessionDao {
    public List<PTSession> findAll() {
        return Jdbc.query("SELECT s.SessionID, s.StartTime, s.EndTime, s.Status, s.TrainerID, s.MemberID, t.FullName, m.FullName "
                        + "FROM PTSession s JOIN Trainer t ON t.TrainerID = s.TrainerID JOIN Member m ON m.MemberID = s.MemberID ORDER BY s.SessionID",
                rs -> {
                    PTSession s = new PTSession(rs.getString(1), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                            rs.getString(4), rs.getString(5), rs.getString(6));
                    s.setTrainerName(rs.getString(7));
                    s.setMemberName(rs.getString(8));
                    return s;
                });
    }

    public void insert(PTSession x) {
        Jdbc.update("INSERT INTO PTSession (SessionID, StartTime, EndTime, Status, TrainerID, MemberID) VALUES (?,?,?,?,?,?)",
                x.getSessionId(), x.getStartTime(), x.getEndTime(), x.getStatus(), x.getTrainerId(), x.getMemberId());
    }

    public void update(PTSession x) {
        Jdbc.update("UPDATE PTSession SET StartTime=?, EndTime=?, Status=?, TrainerID=?, MemberID=? WHERE SessionID=?",
                x.getStartTime(), x.getEndTime(), x.getStatus(), x.getTrainerId(), x.getMemberId(), x.getSessionId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM PTSession WHERE SessionID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("PTSession", "SessionID", "SES", 3);
    }
}