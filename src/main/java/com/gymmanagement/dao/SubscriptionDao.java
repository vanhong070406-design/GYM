package com.gymmanagement.dao;

import com.gymmanagement.model.Subscription;
import java.time.LocalDate;
import java.util.List;

public class SubscriptionDao {
    public List<Subscription> findAll() {
        return Jdbc.query("SELECT s.SubscriptionID, s.StartDate, s.EndDate, s.Status, s.MemberID, s.PackageID, m.FullName "
                        + "FROM Subscription s JOIN Member m ON m.MemberID = s.MemberID ORDER BY s.SubscriptionID",
                rs -> {
                    Subscription s = new Subscription(rs.getString(1), rs.getObject(2, LocalDate.class), rs.getObject(3, LocalDate.class),
                            rs.getString(4), rs.getString(5), rs.getString(6));
                    s.setMemberName(rs.getString(7));
                    return s;
                });
    }

    /** Chuyển các đăng ký đã quá ngày kết thúc sang "Hết hạn"; đăng ký "Tạm dừng" giữ nguyên. */
    public void expireOverdue() {
        Jdbc.update("UPDATE Subscription SET Status = N'Hết hạn' "
                + "WHERE Status = N'Đang hoạt động' AND EndDate < CAST(GETDATE() AS DATE)");
    }

    public void insert(Subscription x) {
        Jdbc.update("INSERT INTO Subscription (SubscriptionID, MemberID, PackageID, StartDate, EndDate, Status) VALUES (?,?,?,?,?,?)",
                x.getSubscriptionId(), x.getMemberId(), x.getPackageId(), x.getStartDate(), x.getEndDate(), x.getStatus());
    }

    public void update(Subscription x) {
        Jdbc.update("UPDATE Subscription SET MemberID=?, PackageID=?, StartDate=?, EndDate=?, Status=? WHERE SubscriptionID=?",
                x.getMemberId(), x.getPackageId(), x.getStartDate(), x.getEndDate(), x.getStatus(), x.getSubscriptionId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Subscription WHERE SubscriptionID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Subscription", "SubscriptionID", "SUB", 3);
    }
}
