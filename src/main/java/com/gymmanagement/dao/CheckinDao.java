package com.gymmanagement.dao;

import com.gymmanagement.model.Checkin;
import java.time.LocalDateTime;
import java.util.List;

public class CheckinDao {
    private static final String COLUMNS = " c.CheckinID, c.CheckinTime, c.CheckoutTime, c.MemberID, c.ReceptionistID, m.FullName "
            + "FROM Checkin c JOIN Member m ON m.MemberID = c.MemberID ";

    private static List<Checkin> read(String sql) {
        return Jdbc.query(sql, rs -> {
            Checkin c = new Checkin(rs.getString(1), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                    rs.getString(4), rs.getString(5));
            c.setMemberName(rs.getString(6));
            return c;
        });
    }

    public List<Checkin> findAll() {
        return read("SELECT" + COLUMNS + "ORDER BY c.CheckinID");
    }

    public List<Checkin> findRecent(int limit) {
        return read("SELECT TOP (" + limit + ")" + COLUMNS + "ORDER BY c.CheckinTime DESC");
    }

    public void insert(Checkin c) {
        Jdbc.update("INSERT INTO Checkin (CheckinID, CheckinTime, CheckoutTime, MemberID, ReceptionistID) VALUES (?,?,?,?,?)",
                c.getCheckinId(), c.getCheckinTime(), c.getCheckoutTime(), c.getMemberId(), c.getReceptionistId());
    }

    public void checkout(String id, LocalDateTime time) {
        Jdbc.update("UPDATE Checkin SET CheckoutTime=? WHERE CheckinID=?", time, id);
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Checkin WHERE CheckinID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Checkin", "CheckinID", "CK", 3);
    }
}
