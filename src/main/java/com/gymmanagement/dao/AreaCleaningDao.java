package com.gymmanagement.dao;

import com.gymmanagement.model.AreaCleaning;
import java.time.LocalDate;
import java.util.List;

public class AreaCleaningDao {
    public List<AreaCleaning> findAll() {
        return Jdbc.query("SELECT ac.StaffID, ac.AreaID, ac.CleaningDate, ac.ShiftID, ac.Status, st.FullName, a.AreaName, sh.ShiftName "
                        + "FROM AreaCleaning ac JOIN Staff st ON st.StaffID = ac.StaffID JOIN Area a ON a.AreaID = ac.AreaID "
                        + "JOIN Shift sh ON sh.ShiftID = ac.ShiftID ORDER BY ac.CleaningDate DESC, ac.AreaID, sh.StartTime",
                rs -> {
                    AreaCleaning ac = new AreaCleaning(rs.getString(1), rs.getString(2), rs.getObject(3, LocalDate.class), rs.getString(4), rs.getString(5));
                    ac.setStaffName(rs.getString(6));
                    ac.setAreaName(rs.getString(7));
                    ac.setShiftName(rs.getString(8));
                    return ac;
                });
    }

    public void insert(AreaCleaning ac) {
        Jdbc.update("INSERT INTO AreaCleaning (AreaID, CleaningDate, ShiftID, StaffID, Status) VALUES (?,?,?,?,?)",
                ac.getAreaId(), ac.getCleaningDate(), ac.getShiftId(), ac.getStaffId(), ac.getStatus());
    }

    public void update(AreaCleaning ac) {
        Jdbc.update("UPDATE AreaCleaning SET StaffID=?, Status=? WHERE AreaID=? AND CleaningDate=? AND ShiftID=?",
                ac.getStaffId(), ac.getStatus(), ac.getAreaId(), ac.getCleaningDate(), ac.getShiftId());
    }

    public void delete(String areaId, LocalDate date, String shiftId) {
        Jdbc.update("DELETE FROM AreaCleaning WHERE AreaID=? AND CleaningDate=? AND ShiftID=?", areaId, date, shiftId);
    }
}
