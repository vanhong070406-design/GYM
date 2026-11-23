package com.gymmanagement.dao;

import com.gymmanagement.model.Payment;
import com.gymmanagement.model.Subscription;
import java.time.LocalDate;
import java.util.List;

public class SubscriptionDao {
    /** Kèm tên hội viên, thông tin gói, giá chốt lúc đăng ký và tổng tiền đã thu của từng đăng ký. */
    public List<Subscription> findAll() {
        return Jdbc.query("SELECT s.SubscriptionID, s.StartDate, s.EndDate, s.Status, s.MemberID, s.PackageID, m.FullName, "
                        + "g.PackageName, s.Price, g.DurationDays, "
                        + "COALESCE((SELECT SUM(p.Amount) FROM Payment p WHERE p.SubscriptionID = s.SubscriptionID), 0) "
                        + "FROM Subscription s "
                        + "JOIN Member m ON m.MemberID = s.MemberID "
                        + "JOIN GymPackage g ON g.PackageID = s.PackageID "
                        + "ORDER BY s.SubscriptionID",
                rs -> {
                    Subscription s = new Subscription(rs.getString(1), rs.getObject(2, LocalDate.class), rs.getObject(3, LocalDate.class),
                            rs.getString(4), rs.getString(5), rs.getString(6));
                    s.setMemberName(rs.getString(7));
                    s.setPackageName(rs.getString(8));
                    s.setPrice(rs.getBigDecimal(9));
                    s.setDurationDays(rs.getInt(10));
                    s.setPaidAmount(rs.getBigDecimal(11));
                    return s;
                });
    }

    /** Mã các hóa đơn đã lập cho đăng ký này; còn hóa đơn thì không xóa được đăng ký. */
    public List<String> findPaymentIds(String subscriptionId) {
        return Jdbc.query("SELECT PaymentID FROM Payment WHERE SubscriptionID = ? ORDER BY PaymentID",
                rs -> rs.getString(1), subscriptionId);
    }

    /**
     * Thêm đăng ký và (nếu payment khác null) hóa đơn đi kèm trong cùng một giao dịch,
     * để không bao giờ có đăng ký mới đã thu tiền nhưng thiếu hóa đơn hoặc ngược lại.
     */
    public void insertWithPayment(Subscription x, Payment payment) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "INSERT INTO Subscription (SubscriptionID, MemberID, PackageID, Price, StartDate, EndDate, Status) VALUES (?,?,?,?,?,?,?)",
                    x.getSubscriptionId(), x.getMemberId(), x.getPackageId(), x.getPrice(), x.getStartDate(), x.getEndDate(), x.getStatus());
            if (payment != null) {
                Jdbc.update(c, "INSERT INTO Payment (PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID) VALUES (?,?,?,?,?,?)",
                        payment.getPaymentId(), payment.getPaymentDate(), payment.getPaymentMethod(), payment.getAmount(),
                        payment.getSubscriptionId(), payment.getReceptionistId());
            }
        });
    }

    /** Chuyển các đăng ký đã quá ngày kết thúc sang "Hết hạn"; đăng ký "Tạm dừng" giữ nguyên. */
    public void expireOverdue() {
        Jdbc.update("UPDATE Subscription SET Status = N'Hết hạn' "
                + "WHERE Status = N'Đang hoạt động' AND EndDate < CAST(GETDATE() AS DATE)");
    }

    public void insert(Subscription x) {
        Jdbc.update("INSERT INTO Subscription (SubscriptionID, MemberID, PackageID, Price, StartDate, EndDate, Status) VALUES (?,?,?,?,?,?,?)",
                x.getSubscriptionId(), x.getMemberId(), x.getPackageId(), x.getPrice(), x.getStartDate(), x.getEndDate(), x.getStatus());
    }

    public void update(Subscription x) {
        Jdbc.update("UPDATE Subscription SET MemberID=?, PackageID=?, Price=?, StartDate=?, EndDate=?, Status=? WHERE SubscriptionID=?",
                x.getMemberId(), x.getPackageId(), x.getPrice(), x.getStartDate(), x.getEndDate(), x.getStatus(), x.getSubscriptionId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Subscription WHERE SubscriptionID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Subscription", "SubscriptionID", "SUB", 3);
    }
}
