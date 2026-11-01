package com.gymmanagement.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Các truy vấn tổng hợp cho Dashboard. */
public class DashboardDao {
    public record Stats(int totalMembers, int activeMembers, int totalPackages, int todayCheckins, BigDecimal totalRevenue) {}

    public Stats loadStats() {
        return Jdbc.queryOne("SELECT "
                        + "(SELECT COUNT(*) FROM Member), "
                        + "(SELECT COUNT(DISTINCT MemberID) FROM Subscription WHERE Status = N'Đang hoạt động' AND EndDate >= CAST(GETDATE() AS DATE)), "
                        + "(SELECT COUNT(*) FROM GymPackage), "
                        + "(SELECT COUNT(*) FROM Checkin WHERE CAST(CheckinTime AS DATE) = CAST(GETDATE() AS DATE)), "
                        + "(SELECT COALESCE(SUM(Amount), 0) FROM Payment)",
                rs -> new Stats(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getBigDecimal(5)));
    }

    /** Doanh thu từng tháng trong N tháng gần nhất (tính cả tháng hiện tại, tháng không có thu = 0), theo thứ tự thời gian. */
    public Map<YearMonth, BigDecimal> loadMonthlyRevenue(int months) {
        YearMonth current = YearMonth.now();
        Map<YearMonth, BigDecimal> result = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) result.put(current.minusMonths(i), BigDecimal.ZERO);

        LocalDate from = current.minusMonths(months - 1L).atDay(1);
        List<Object[]> rows = Jdbc.query("SELECT YEAR(PaymentDate), MONTH(PaymentDate), SUM(Amount) FROM Payment "
                        + "WHERE PaymentDate >= ? GROUP BY YEAR(PaymentDate), MONTH(PaymentDate)",
                rs -> new Object[]{YearMonth.of(rs.getInt(1), rs.getInt(2)), rs.getBigDecimal(3)}, from);
        for (Object[] row : rows) result.replace((YearMonth) row[0], (BigDecimal) row[1]);
        return result;
    }
}
