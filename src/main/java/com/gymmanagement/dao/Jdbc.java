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
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Khóa ngoại: missing khi thêm/sửa với mã không tồn tại,
     * inUse khi xóa/sửa bản ghi mà dữ liệu khác đang trỏ tới.
     */
    private record ForeignKeyMessage(String missing, String inUse) {}

    private static final Map<String, ForeignKeyMessage> FOREIGN_KEY_MESSAGES = Map.ofEntries(
            Map.entry("FK_Staff_Shift", new ForeignKeyMessage("Ca làm việc không tồn tại.", "Ca làm việc đang có nhân viên.")),
            Map.entry("FK_Staff_Position", new ForeignKeyMessage("Chức vụ không tồn tại.", "Chức vụ đang có nhân viên.")),
            Map.entry("FK_PositionPermission_Position", new ForeignKeyMessage("Chức vụ không tồn tại.", "Chức vụ đang có phân quyền.")),
            Map.entry("FK_Subscription_Member", new ForeignKeyMessage("Hội viên không tồn tại.", "Hội viên này đã có đăng ký gói tập.")),
            Map.entry("FK_Subscription_Package", new ForeignKeyMessage("Gói tập không tồn tại.", "Gói tập này đã có hội viên đăng ký.")),
            Map.entry("FK_Payment_Subscription", new ForeignKeyMessage("Đăng ký gói tập không tồn tại.", "Đăng ký gói tập này đã có thanh toán.")),
            Map.entry("FK_Payment_Staff", new ForeignKeyMessage("Nhân viên thu tiền không tồn tại.", "Nhân viên này đã lập hóa đơn thanh toán.")),
            Map.entry("FK_Checkin_Member", new ForeignKeyMessage("Hội viên không tồn tại.", "Hội viên này đã có lịch sử check-in.")),
            Map.entry("FK_Checkin_Staff", new ForeignKeyMessage("Nhân viên lễ tân không tồn tại.", "Nhân viên này đã ghi nhận check-in.")),
            Map.entry("FK_PTSession_Trainer", new ForeignKeyMessage("Huấn luyện viên không tồn tại.", "Huấn luyện viên này đã có lịch tập PT.")),
            Map.entry("FK_PTSession_Member", new ForeignKeyMessage("Hội viên không tồn tại.", "Hội viên này đã có lịch tập PT.")),
            Map.entry("FK_TrainerSpecialty_Trainer", new ForeignKeyMessage("Huấn luyện viên không tồn tại.", "Huấn luyện viên này đang có chuyên môn.")),
            Map.entry("FK_TrainerSpecialty_Specialty", new ForeignKeyMessage("Chuyên môn không tồn tại.", "Chuyên môn này đang được huấn luyện viên sử dụng.")),
            Map.entry("FK_Maintenance_Staff", new ForeignKeyMessage("Nhân viên kỹ thuật không tồn tại.", "Nhân viên này đang có phiếu bảo trì.")),
            Map.entry("FK_Maintenance_Equipment", new ForeignKeyMessage("Thiết bị không tồn tại.", "Thiết bị này đang có phiếu bảo trì.")),
            Map.entry("FK_UserAccount_Staff", new ForeignKeyMessage("Nhân viên không tồn tại.", "Nhân viên này đang có tài khoản.")),
            Map.entry("FK_UserAccount_Trainer", new ForeignKeyMessage("Huấn luyện viên không tồn tại.", "Huấn luyện viên này đang có tài khoản.")));

    /** Ràng buộc UNIQUE và CHECK; tên lấy đúng theo database/01_schema.sql. */
    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.ofEntries(
            Map.entry("UQ_Staff_Phone", "Số điện thoại này đã được nhân viên khác sử dụng."),
            Map.entry("UQ_Member_Phone", "Số điện thoại này đã được hội viên khác sử dụng."),
            Map.entry("UQ_Member_Email", "Email này đã được hội viên khác sử dụng."),
            Map.entry("UQ_Trainer_Phone", "Số điện thoại này đã được huấn luyện viên khác sử dụng."),
            Map.entry("UQ_GymPackage_Name", "Tên gói tập đã tồn tại."),
            Map.entry("UQ_Shift_Name", "Tên ca làm việc đã tồn tại."),
            Map.entry("UQ_Position_Name", "Tên chức vụ đã tồn tại."),
            Map.entry("UQ_Specialty_Name", "Tên chuyên môn đã tồn tại."),
            Map.entry("UQ_Subscription_Natural", "Hội viên đã đăng ký gói này với cùng ngày bắt đầu."),
            Map.entry("UQ_Checkin_Natural", "Hội viên đã check-in vào đúng thời điểm này."),
            Map.entry("UQ_PTSession_Trainer_Start", "Huấn luyện viên đã có buổi tập khác bắt đầu cùng giờ."),
            Map.entry("UQ_PTSession_Member_Start", "Hội viên đã có buổi tập khác bắt đầu cùng giờ."),
            Map.entry("UQ_UserAccount_Staff", "Nhân viên này đã có tài khoản."),
            Map.entry("UQ_UserAccount_Trainer", "Huấn luyện viên này đã có tài khoản."),
            Map.entry("CK_Staff_NotTrainerPosition", "Huấn luyện viên được quản lý ở màn hình Quản lý Huấn luyện viên, không chọn chức vụ này cho nhân viên."),
            Map.entry("CK_Staff_Salary", "Mức lương không được âm."),
            Map.entry("CK_GymPackage_Price", "Giá gói tập không được âm."),
            Map.entry("CK_GymPackage_Duration", "Thời hạn gói tập phải lớn hơn 0 ngày."),
            Map.entry("CK_Subscription_Dates", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu."),
            Map.entry("CK_Subscription_Price", "Giá đăng ký không được âm."),
            Map.entry("CK_Payment_Amount", "Số tiền thanh toán phải lớn hơn 0."),
            Map.entry("CK_Checkin_Times", "Giờ ra phải sau giờ vào."),
            Map.entry("CK_PTSession_Times", "Giờ kết thúc phải sau giờ bắt đầu."),
            Map.entry("CK_Trainer_Experience", "Số năm kinh nghiệm không được âm."),
            Map.entry("CK_Equipment_Value", "Giá trị thiết bị không được âm."),
            Map.entry("CK_Equipment_Status", "Trạng thái thiết bị chỉ được là Hoạt động tốt, Cần thay thế linh kiện hoặc Tới hạn bảo trì định kỳ. "
                    + "Nếu database được tạo từ bản cũ, hãy chạy lại script setup_database."));

    /** SQL Server ghi tên ràng buộc trong dấu nháy, ví dụ: constraint "FK_Staff_Shift" hoặc 'UQ_Staff_Phone'. */
    private static final Pattern CONSTRAINT_NAME = Pattern.compile("[\"']((?:PK|FK|UQ|CK)_\\w+)[\"']");

    private static DataAccessException translate(SQLException e) {
        String detail = e.getMessage() == null ? "" : e.getMessage();
        Matcher m = CONSTRAINT_NAME.matcher(detail);
        String constraint = m.find() ? m.group(1) : "";

        String message = switch (e.getErrorCode()) {
            case 2627, 2601 -> {
                if (constraint.startsWith("PK_")) yield "Mã này đã tồn tại, hãy dùng mã khác.";
                yield CONSTRAINT_MESSAGES.getOrDefault(constraint,
                        "Dữ liệu bị trùng: mã hoặc thông tin duy nhất (số điện thoại, email, tên...) đã tồn tại.");
            }
            case 547 -> {
                ForeignKeyMessage fk = FOREIGN_KEY_MESSAGES.get(constraint);
                // "REFERENCE constraint": xóa/sửa bản ghi đang được trỏ tới; "FOREIGN KEY constraint": mã liên kết không tồn tại
                boolean referenced = detail.contains("REFERENCE constraint");
                if (fk != null) yield "Không thể thực hiện: " + (referenced ? fk.inUse() : fk.missing());
                if (CONSTRAINT_MESSAGES.containsKey(constraint)) yield "Dữ liệu không hợp lệ: " + CONSTRAINT_MESSAGES.get(constraint);
                if (detail.contains("CHECK")) yield "Dữ liệu không hợp lệ (giá trị âm, hoặc thời gian kết thúc trước thời gian bắt đầu).";
                yield referenced
                        ? "Không thể thực hiện: bản ghi đang được dữ liệu khác sử dụng."
                        : "Không thể thực hiện: mã liên kết không tồn tại.";
            }
            // Trigger quy tắc chức vụ đăng nhập (THROW 5000x): thông báo đã viết sẵn bằng tiếng Việt
            case 50001, 50002, 50003 -> detail;
            case 18456 -> "Sai tài khoản kết nối database. Kiểm tra file db.properties.";
            case 4060 -> "Không mở được database GymManagementDB. Hãy chạy database\\setup_database.ps1 (Windows) hoặc database/setup_database.sh (macOS).";
            default -> "Lỗi cơ sở dữ liệu: " + detail;
        };
        return new DataAccessException(message, e);
    }
}
