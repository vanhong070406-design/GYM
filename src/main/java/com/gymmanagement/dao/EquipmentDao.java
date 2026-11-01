package com.gymmanagement.dao;

import com.gymmanagement.model.Equipment;
import java.time.LocalDate;
import java.util.List;

public class EquipmentDao {
    public List<Equipment> findAll() {
        return Jdbc.query("SELECT EquipmentID, EquipmentName, PurchaseValue, PurchaseDate, Status, AreaID FROM Equipment ORDER BY EquipmentID",
                rs -> new Equipment(rs.getString(1), rs.getString(2), rs.getBigDecimal(3), rs.getObject(4, LocalDate.class), rs.getString(5), rs.getString(6)));
    }

    public void insert(Equipment x) {
        Jdbc.update("INSERT INTO Equipment (EquipmentID, EquipmentName, PurchaseValue, PurchaseDate, Status, AreaID) VALUES (?,?,?,?,?,?)",
                x.getEquipmentId(), x.getEquipmentName(), x.getValue(), x.getPurchaseDate(), x.getStatus(), x.getAreaId());
    }

    public void update(Equipment x) {
        Jdbc.update("UPDATE Equipment SET EquipmentName=?, PurchaseValue=?, PurchaseDate=?, Status=?, AreaID=? WHERE EquipmentID=?",
                x.getEquipmentName(), x.getValue(), x.getPurchaseDate(), x.getStatus(), x.getAreaId(), x.getEquipmentId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Equipment WHERE EquipmentID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Equipment", "EquipmentID", "EQ", 2);
    }
}
