package com.gymmanagement.dao;

import com.gymmanagement.model.Position;
import java.util.List;

public class PositionDao {
    // Các chức vụ gán được cho nhân viên; HLV quản lý ở bảng Trainer nên không có ở đây.
    public List<Position> findStaffPositions() {
        return Jdbc.query("SELECT PositionID, PositionName, CanLogin FROM Position WHERE PositionID NOT IN ('HLV' , 'QL') ORDER BY PositionName",
                rs -> new Position(rs.getString(1), rs.getString(2), rs.getBoolean(3)));
    }
}
