package com.gymmanagement.dao;

import com.gymmanagement.model.MaintenanceAssignment;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceDao {
    public List<MaintenanceAssignment> findAll() {
        return Jdbc.query("SELECT m.StaffID, m.EquipmentID, m.MaintenanceDate, m.Note, m.Status, s.FullName, e.EquipmentName "
                        + "FROM MaintenanceAssignment m JOIN Staff s ON s.StaffID = m.StaffID "
                        + "JOIN Equipment e ON e.EquipmentID = m.EquipmentID ORDER BY m.MaintenanceDate DESC, m.StaffID, m.EquipmentID",
                rs -> {
                    MaintenanceAssignment m = new MaintenanceAssignment(rs.getString(1), rs.getString(2),
                            rs.getObject(3, LocalDate.class), rs.getString(4), rs.getString(5));
                    m.setStaffName(rs.getString(6));
                    m.setEquipmentName(rs.getString(7));
                    return m;
                });
    }

    /** Lưu phân công; nếu phiếu ở trạng thái "Đang bảo trì" thì cập nhật luôn trạng thái thiết bị trong cùng giao dịch. */
    public void insert(MaintenanceAssignment m) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "INSERT INTO MaintenanceAssignment (StaffID, EquipmentID, MaintenanceDate, Note, Status) VALUES (?,?,?,?,?)",
                    m.getStaffId(), m.getEquipmentId(), m.getMaintenanceDate(), m.getNote(), m.getStatus());
            if ("Đang bảo trì".equals(m.getStatus())) {
                Jdbc.update(c, "UPDATE Equipment SET Status = N'Đang bảo trì' WHERE EquipmentID = ?", m.getEquipmentId());
            }
        });
    }

    /**
     * Cập nhật nội dung và trạng thái phiếu. Khi phiếu vừa chuyển sang hoàn thành và thiết bị không còn phiếu
     * nào đang mở thì thiết bị trở lại "Tốt"; khi phiếu vừa chuyển sang đang bảo trì thì thiết bị cũng vậy.
     */
    public void update(MaintenanceAssignment m, String previousStatus) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "UPDATE MaintenanceAssignment SET Note=?, Status=? WHERE StaffID=? AND EquipmentID=? AND MaintenanceDate=?",
                    m.getNote(), m.getStatus(), m.getStaffId(), m.getEquipmentId(), m.getMaintenanceDate());
            if (m.getStatus().equals(previousStatus)) return;
            if ("Đang bảo trì".equals(m.getStatus())) {
                Jdbc.update(c, "UPDATE Equipment SET Status = N'Đang bảo trì' WHERE EquipmentID = ?", m.getEquipmentId());
            } else if ("Đã hoàn thành".equals(m.getStatus())) {
                Jdbc.update(c, "UPDATE Equipment SET Status = N'Tốt' WHERE EquipmentID = ? AND NOT EXISTS "
                        + "(SELECT 1 FROM MaintenanceAssignment WHERE EquipmentID = ? AND Status <> N'Đã hoàn thành')",
                        m.getEquipmentId(), m.getEquipmentId());
            }
        });
    }

    public void delete(String staffId, String equipmentId, LocalDate date) {
        Jdbc.update("DELETE FROM MaintenanceAssignment WHERE StaffID=? AND EquipmentID=? AND MaintenanceDate=?", staffId, equipmentId, date);
    }
}
