package com.gymmanagement.dao;

import com.gymmanagement.model.Equipment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EquipmentDao {
    private static final String SELECT = "SELECT EquipmentID, EquipmentName, PurchaseValue, PurchaseDate, Status FROM Equipment ";

    private static Equipment map(ResultSet rs) throws SQLException {
        return new Equipment(rs.getString(1), rs.getString(2), rs.getBigDecimal(3), rs.getObject(4, LocalDate.class), rs.getString(5));
    }

    public List<Equipment> findAll() {
        return Jdbc.query(SELECT + "ORDER BY EquipmentID", EquipmentDao::map);
    }

    /**
     * Thiết bị có thể phân công bảo trì: đang cần thay linh kiện hoặc tới hạn bảo trì,
     * và chưa có phiếu bảo trì nào đang mở (để một máy không bị giao 2 lần).
     */
    public List<Equipment> findNeedingMaintenance() {
        return Jdbc.query(SELECT + "WHERE Status IN (?, ?) AND NOT EXISTS (SELECT 1 FROM MaintenanceAssignment m "
                        + "WHERE m.EquipmentID = Equipment.EquipmentID AND m.Status <> ?) ORDER BY EquipmentID",
                EquipmentDao::map, Equipment.STATUS_NEEDS_PARTS, Equipment.STATUS_MAINTENANCE_DUE, MaintenanceDao.STATUS_DONE);
    }

    /** Đổi riêng trạng thái, dùng cho combobox trong bảng. */
    public void updateStatus(String id, String status) {
        Jdbc.update("UPDATE Equipment SET Status=? WHERE EquipmentID=?", status, id);
    }

    public void insert(Equipment x) {
        Jdbc.update("INSERT INTO Equipment (EquipmentID, EquipmentName, PurchaseValue, PurchaseDate, Status) VALUES (?,?,?,?,?)",
                x.getEquipmentId(), x.getEquipmentName(), x.getValue(), x.getPurchaseDate(), x.getStatus());
    }

    public void update(Equipment x) {
        Jdbc.update("UPDATE Equipment SET EquipmentName=?, PurchaseValue=?, PurchaseDate=?, Status=? WHERE EquipmentID=?",
                x.getEquipmentName(), x.getValue(), x.getPurchaseDate(), x.getStatus(), x.getEquipmentId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Equipment WHERE EquipmentID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Equipment", "EquipmentID", "EQ", 2);
    }
}