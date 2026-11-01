package com.gymmanagement.dao;

import com.gymmanagement.model.Area;
import java.util.List;

public class AreaDao {
    public List<Area> findAll() {
        return Jdbc.query("SELECT AreaID, AreaName, Floor, Description FROM Area ORDER BY AreaID",
                rs -> new Area(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    public void insert(Area x) {
        Jdbc.update("INSERT INTO Area (AreaID, AreaName, Floor, Description) VALUES (?,?,?,?)",
                x.getAreaId(), x.getAreaName(), x.getFloor(), x.getDescription());
    }

    public void update(Area x) {
        Jdbc.update("UPDATE Area SET AreaName=?, Floor=?, Description=? WHERE AreaID=?",
                x.getAreaName(), x.getFloor(), x.getDescription(), x.getAreaId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Area WHERE AreaID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Area", "AreaID", "AR", 2);
    }
}
