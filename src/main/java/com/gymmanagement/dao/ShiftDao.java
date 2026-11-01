package com.gymmanagement.dao;

import com.gymmanagement.model.Shift;
import java.time.LocalTime;
import java.util.List;

public class ShiftDao {
    public List<Shift> findAll() {
        return Jdbc.query("SELECT ShiftID, ShiftName, StartTime, EndTime FROM Shift ORDER BY StartTime",
                rs -> new Shift(rs.getString(1), rs.getString(2), rs.getObject(3, LocalTime.class), rs.getObject(4, LocalTime.class)));
    }
}
