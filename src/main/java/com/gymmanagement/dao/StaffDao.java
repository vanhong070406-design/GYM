package com.gymmanagement.dao;

import com.gymmanagement.model.Shift;
import com.gymmanagement.model.Staff;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class StaffDao {
    private static final String SELECT = "SELECT st.StaffID, st.FullName, st.Phone, st.Salary, st.ShiftID, st.HireDate, st.ManagerID, st.PositionID, "
            + "sh.ShiftName, sh.StartTime, sh.EndTime, p.PositionName FROM Staff st "
            + "JOIN Shift sh ON sh.ShiftID = st.ShiftID JOIN Position p ON p.PositionID = st.PositionID ";

    private static Staff map(ResultSet rs) throws SQLException {
        Staff s = new Staff(rs.getString(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getString(5),
                rs.getObject(6, LocalDate.class), rs.getString(7), rs.getString(8));
        s.setShift(new Shift(rs.getString(5), rs.getString(9), rs.getObject(10, LocalTime.class), rs.getObject(11, LocalTime.class)));
        s.setPositionName(rs.getString(12));
        return s;
    }

    public List<Staff> findAll() {
        return Jdbc.query(SELECT + "ORDER BY st.StaffID", StaffDao::map);
    }

    public List<Staff> findByPosition(String positionId) {
        return Jdbc.query(SELECT + "WHERE st.PositionID = ? ORDER BY st.StaffID", StaffDao::map, positionId);
    }

    /** Mã các nhân viên do managerId quản lý trực tiếp. */
    public List<String> findDirectReportIds(String managerId) {
        return Jdbc.query("SELECT StaffID FROM Staff WHERE ManagerID = ? ORDER BY StaffID", rs -> rs.getString(1), managerId);
    }

    public void insert(Staff x) {
        Jdbc.update("INSERT INTO Staff (StaffID, FullName, Phone, Salary, ShiftID, HireDate, ManagerID, PositionID) VALUES (?,?,?,?,?,?,?,?)",
                x.getStaffId(), x.getFullName(), x.getPhone(), x.getSalary(), x.getShiftId(), x.getHireDate(), x.getManagerId(), x.getPositionId());
    }

    public void update(Staff x) {
        Jdbc.update("UPDATE Staff SET FullName=?, Phone=?, Salary=?, ShiftID=?, HireDate=?, ManagerID=?, PositionID=? WHERE StaffID=?",
                x.getFullName(), x.getPhone(), x.getSalary(), x.getShiftId(), x.getHireDate(), x.getManagerId(), x.getPositionId(), x.getStaffId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Staff WHERE StaffID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Staff", "StaffID", "ST", 3);
    }
}
