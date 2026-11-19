package com.gymmanagement.dao;

import com.gymmanagement.model.AccessLevel;
import com.gymmanagement.model.UserSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAccountDao {
    // Tài khoản của một nhân viên: tên đăng nhập và còn đang dùng mật khẩu tạm hay không
    public record StaffAccount(String staffId, String username, boolean mustChangePassword) {}

    /** Chức vụ của tài khoản luôn lấy theo nhân viên/HLV sở hữu, nên đổi chức vụ là quyền đổi theo. */
    private static final String OWNER_JOIN = "FROM UserAccount a "
            + "LEFT JOIN Staff s ON s.StaffID = a.StaffID "
            + "LEFT JOIN Trainer t ON t.TrainerID = a.TrainerID "
            + "JOIN Position p ON p.PositionID = COALESCE(s.PositionID, 'HLV') ";

    /** Trả về phiên đăng nhập, hoặc null nếu sai tên đăng nhập/mật khẩu. */
    public UserSession login(String username, String password) {
        return Jdbc.queryOne("SELECT a.Username, a.PasswordHash, a.PasswordSalt, a.Iterations, a.MustChangePassword, a.StaffID, a.TrainerID, "
                        + "COALESCE(s.FullName, t.FullName), p.PositionID, p.PositionName " + OWNER_JOIN + "WHERE a.Username = ?",
                rs -> {
                    if (!PasswordHasher.matches(password, rs.getBytes(2), rs.getBytes(3), rs.getInt(4))) return null;
                    return new UserSession(rs.getString(1), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(10),
                            rs.getBoolean(5), loadPermissions(rs.getString(9)));
                }, username);
    }

    private Map<String, AccessLevel> loadPermissions(String positionId) {
        Map<String, AccessLevel> permissions = new HashMap<>();
        for (String[] row : Jdbc.query("SELECT ScreenCode, AccessLevel FROM PositionPermission WHERE PositionID = ?",
                rs -> new String[]{rs.getString(1), rs.getString(2)}, positionId)) {
            permissions.put(row[0], AccessLevel.valueOf(row[1]));
        }
        return permissions;
    }

    /** Trả về false nếu mật khẩu hiện tại không đúng. */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (login(username, currentPassword) == null) return false;
        byte[] salt = PasswordHasher.newSalt();
        Jdbc.update("UPDATE UserAccount SET PasswordHash=?, PasswordSalt=?, Iterations=?, MustChangePassword=0 WHERE Username=?",
                PasswordHasher.hash(newPassword, salt, PasswordHasher.ITERATIONS), salt, PasswordHasher.ITERATIONS, username);
        return true;
    }

    // Tài khoản của mọi nhân viên đã được cấp, khóa theo mã nhân viên
    public Map<String, StaffAccount> findStaffAccounts() {
        List<StaffAccount> rows = Jdbc.query("SELECT StaffID, Username, MustChangePassword FROM UserAccount WHERE StaffID IS NOT NULL",
                rs -> new StaffAccount(rs.getString(1), rs.getString(2), rs.getBoolean(3)));
        Map<String, StaffAccount> result = new HashMap<>();
        for (StaffAccount a : rows) result.put(a.staffId(), a);
        return result;
    }

    // Tên đăng nhập đã có người dùng chưa (database không phân biệt hoa thường)
    public boolean usernameExists(String username) {
        return Jdbc.queryOne("SELECT 1 FROM UserAccount WHERE Username = ?", rs -> rs.getInt(1), username) != null;
    }

    // Cấp tài khoản mới cho nhân viên; mustChange = bắt đổi mật khẩu ở lần đăng nhập đầu
    public void createStaffAccount(String staffId, String username, String password, boolean mustChange) {
        byte[] salt = PasswordHasher.newSalt();
        Jdbc.update("INSERT INTO UserAccount (Username, PasswordHash, PasswordSalt, Iterations, MustChangePassword, StaffID, TrainerID) "
                        + "VALUES (?,?,?,?,?,?,NULL)",
                username, PasswordHasher.hash(password, salt, PasswordHasher.ITERATIONS), salt, PasswordHasher.ITERATIONS, mustChange, staffId);
    }

    // Quản lý đặt lại mật khẩu cho nhân viên đã có tài khoản, không cần biết mật khẩu cũ
    public void resetStaffPassword(String staffId, String newPassword, boolean mustChange) {
        byte[] salt = PasswordHasher.newSalt();
        Jdbc.update("UPDATE UserAccount SET PasswordHash=?, PasswordSalt=?, Iterations=?, MustChangePassword=? WHERE StaffID=?",
                PasswordHasher.hash(newPassword, salt, PasswordHasher.ITERATIONS), salt, PasswordHasher.ITERATIONS, mustChange, staffId);
    }
    
}