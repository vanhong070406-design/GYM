package com.gymmanagement.dao;

import com.gymmanagement.model.AccessLevel;
import com.gymmanagement.model.AccountOwner;
import com.gymmanagement.model.UserAccount;
import com.gymmanagement.model.UserSession;
import java.util.HashMap;
import java.util.List;
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

    public List<UserAccount> findAll() {
        return Jdbc.query("SELECT a.Username, COALESCE(a.StaffID, a.TrainerID), COALESCE(s.FullName, t.FullName), p.PositionName, a.MustChangePassword "
                        + OWNER_JOIN + "ORDER BY a.Username",
                rs -> new UserAccount(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5)));
    }

    public List<AccountOwner> findOwnersWithoutAccount() {
        return Jdbc.query("SELECT s.StaffID, NULL, s.FullName, p.PositionName FROM Staff s JOIN Position p ON p.PositionID = s.PositionID "
                        + "WHERE NOT EXISTS (SELECT 1 FROM UserAccount a WHERE a.StaffID = s.StaffID) "
                        + "UNION ALL SELECT NULL, t.TrainerID, t.FullName, p.PositionName FROM Trainer t JOIN Position p ON p.PositionID = 'HLV' "
                        + "WHERE NOT EXISTS (SELECT 1 FROM UserAccount a WHERE a.TrainerID = t.TrainerID) "
                        + "ORDER BY 1 DESC, 2",
                rs -> new AccountOwner(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    /** Tạo tài khoản với mật khẩu tạm; người dùng phải đổi ở lần đăng nhập đầu. */
    public void create(String username, String temporaryPassword, AccountOwner owner) {
        byte[] salt = PasswordHasher.newSalt();
        Jdbc.update("INSERT INTO UserAccount (Username, PasswordHash, PasswordSalt, Iterations, MustChangePassword, StaffID, TrainerID) VALUES (?,?,?,?,1,?,?)",
                username, PasswordHasher.hash(temporaryPassword, salt, PasswordHasher.ITERATIONS), salt, PasswordHasher.ITERATIONS,
                owner.getStaffId(), owner.getTrainerId());
    }

    public void resetPassword(String username, String temporaryPassword) {
        setPassword(username, temporaryPassword, true);
    }

    /** Trả về false nếu mật khẩu hiện tại không đúng. */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (login(username, currentPassword) == null) return false;
        setPassword(username, newPassword, false);
        return true;
    }

    private void setPassword(String username, String password, boolean mustChange) {
        byte[] salt = PasswordHasher.newSalt();
        Jdbc.update("UPDATE UserAccount SET PasswordHash=?, PasswordSalt=?, Iterations=?, MustChangePassword=? WHERE Username=?",
                PasswordHasher.hash(password, salt, PasswordHasher.ITERATIONS), salt, PasswordHasher.ITERATIONS, mustChange, username);
    }

    public void delete(String username) {
        Jdbc.update("DELETE FROM UserAccount WHERE Username=?", username);
    }
}
