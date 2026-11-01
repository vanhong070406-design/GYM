package com.gymmanagement.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Kết nối SQL Server và các thao tác JDBC dùng chung.
 * Cấu hình đọc từ file db.properties ở thư mục chạy chương trình (thư mục gốc dự án).
 */
public final class Jdbc {
    private static final Path CONFIG = Path.of("db.properties");
    private static Properties config;

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TxWork {
        void run(Connection c) throws SQLException;
    }

    private Jdbc() {}

    private static synchronized Properties config() {
        if (config == null) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG)) {
                p.load(in);
            } catch (IOException e) {
                throw new DataAccessException("Không đọc được file cấu hình " + CONFIG.toAbsolutePath()
                        + ". Hãy chạy database\\setup_database.ps1.", e);
            }
            config = p;
        }
        return config;
    }

    private static Connection open() throws SQLException {
        Properties p = config();
        return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection c = open(); PreparedStatement ps = prepare(c, sql, params); ResultSet rs = ps.executeQuery()) {
            List<T> rows = new ArrayList<>();
            while (rs.next()) rows.add(mapper.map(rs));
            return rows;
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /** Trả về dòng đầu tiên, hoặc null nếu không có dòng nào. */
    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static int update(String sql, Object... params) {
        try (Connection c = open(); PreparedStatement ps = prepare(c, sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(c, sql, params)) {
            return ps.executeUpdate();
        }
    }

    public static void inTransaction(TxWork work) {
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /**
     * Sinh mã kế tiếp theo số lớn nhất đang có (MB007, PK06...), nên không trùng sau khi xóa.
     * table và column là hằng số trong DAO, không nhận từ người dùng.
     */
    public static String nextCode(String table, String column, String prefix, int width) {
        int max = queryOne(
                "SELECT COALESCE(MAX(TRY_CAST(SUBSTRING(" + column + ", ?, 20) AS INT)), 0) FROM " + table + " WHERE " + column + " LIKE ?",
                rs -> rs.getInt(1), prefix.length() + 1, prefix + "%");
        return prefix + String.format("%0" + width + "d", max + 1);
    }

    private static PreparedStatement prepare(Connection c, String sql, Object... params) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }

    private static DataAccessException translate(SQLException e) {
        String message = switch (e.getErrorCode()) {
            case 2627, 2601 -> "Dữ liệu bị trùng: mã hoặc thông tin duy nhất (số điện thoại, email, tên...) đã tồn tại.";
            case 547 -> e.getMessage() != null && e.getMessage().contains("CHECK")
                    ? "Dữ liệu không hợp lệ (giá trị âm, hoặc thời gian kết thúc trước thời gian bắt đầu)."
                    : "Không thể thực hiện: bản ghi đang được dữ liệu khác sử dụng, hoặc mã liên kết không tồn tại.";
            case 18456 -> "Sai tài khoản kết nối database. Kiểm tra file db.properties.";
            case 4060 -> "Không mở được database GymManagementDB. Hãy chạy database\\setup_database.ps1.";
            default -> "Lỗi cơ sở dữ liệu: " + e.getMessage();
        };
        return new DataAccessException(message, e);
    }
}
