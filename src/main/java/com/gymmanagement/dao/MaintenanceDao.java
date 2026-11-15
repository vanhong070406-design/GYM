package com.gymmanagement.dao;

import com.gymmanagement.model.Equipment;
import com.gymmanagement.model.MaintenanceAssignment;
import java.sql.Connection;
import java.sql.SQLException;
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

    /** Trạng thái phiếu đã xong; mọi trạng thái khác (Đang bảo trì, Chờ linh kiện) là phiếu đang mở. */
    public static final String STATUS_DONE = MaintenanceAssignment.STATUS_DONE;

    /**
     * Lưu phân công, hoàn thành ngay thì cập nhật thiết bị trong cùng giao dịch.
     * Phiếu đang mở không đổi trạng thái thiết bị: máy vẫn giữ "Cần thay thế linh kiện" / "Tới hạn bảo trì định kỳ".
     */
    public void insert(MaintenanceAssignment m) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "INSERT INTO MaintenanceAssignment (StaffID, EquipmentID, MaintenanceDate, Note, Status) VALUES (?,?,?,?,?)",
                    m.getStaffId(), m.getEquipmentId(), m.getMaintenanceDate(), m.getNote(), m.getStatus());
            if (STATUS_DONE.equals(m.getStatus())) markGoodIfNoOpenTicket(c, m.getEquipmentId());
        });
    }

    /** Cập nhật nội dung và trạng thái phiếu; phiếu vừa chuyển sang hoàn thành thì cập nhật thiết bị. */
    public void update(MaintenanceAssignment m, String previousStatus) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "UPDATE MaintenanceAssignment SET Note=?, Status=? WHERE StaffID=? AND EquipmentID=? AND MaintenanceDate=?",
                    m.getNote(), m.getStatus(), m.getStaffId(), m.getEquipmentId(), m.getMaintenanceDate());
            if (STATUS_DONE.equals(m.getStatus()) && !STATUS_DONE.equals(previousStatus)) {
                markGoodIfNoOpenTicket(c, m.getEquipmentId());
            }
        });
    }

    /** Thiết bị về "Hoạt động tốt" khi không còn phiếu bảo trì nào đang mở. */
    private static void markGoodIfNoOpenTicket(Connection c, String equipmentId) throws SQLException {
        Jdbc.update(c, "UPDATE Equipment SET Status = ? WHERE EquipmentID = ? AND NOT EXISTS "
                        + "(SELECT 1 FROM MaintenanceAssignment WHERE EquipmentID = ? AND Status <> ?)",
                Equipment.STATUS_GOOD, equipmentId, equipmentId, STATUS_DONE);
    }

    public void delete(String staffId, String equipmentId, LocalDate date) {
        Jdbc.update("DELETE FROM MaintenanceAssignment WHERE StaffID=? AND EquipmentID=? AND MaintenanceDate=?", staffId, equipmentId, date);
    }
}
