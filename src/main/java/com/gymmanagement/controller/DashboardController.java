package com.gymmanagement.controller;

import com.gymmanagement.dao.CheckinDao;
import com.gymmanagement.dao.DashboardDao;
import com.gymmanagement.model.Checkin;
import com.gymmanagement.view.DashboardView;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Controller xử lý dữ liệu và logic cho Dashboard
 */
public class DashboardController {
    private static final int RECENT_CHECKINS = 6;
    private static final int CHART_MONTHS = 6;

    private final DashboardView view;
    private final DashboardDao dashboardDao = new DashboardDao();
    private final CheckinDao checkinDao = new CheckinDao();

    public DashboardController(DashboardView view) {
        this.view = view;
        loadData();
    }

    public void loadData() {
        DbAction.run(view, () -> {
            DashboardDao.Stats s = dashboardDao.loadStats();
            view.setStats(s.totalMembers(), s.activeMembers(), s.totalPackages(), s.todayCheckins(), s.totalRevenue().doubleValue());
            view.setMonthlyRevenue(dashboardDao.loadMonthlyRevenue(CHART_MONTHS));

            // Nạp bảng check-in gần nhất
            List<Checkin> recent = checkinDao.findRecent(RECENT_CHECKINS);
            DefaultTableModel model = view.getCheckinTableModel();
            model.setRowCount(0);
            for (Checkin c : recent) {
                model.addRow(new Object[]{
                        c.getCheckinId(),
                        c.getMemberId(),
                        Formats.text(c.getCheckinTime()),
                        c.getCheckoutTime() == null ? Formats.NO_CHECKOUT : Formats.text(c.getCheckoutTime())
                });
            }
        });
    }
}
