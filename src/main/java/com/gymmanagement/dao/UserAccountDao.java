package com.gymmanagement.dao;

import com.gymmanagement.model.AccessLevel;
import com.gymmanagement.model.UserSession;
import java.util.HashMap;
import java.util.Map;

public class UserAccountDao {
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
}