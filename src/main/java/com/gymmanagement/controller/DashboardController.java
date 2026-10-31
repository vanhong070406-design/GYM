package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Checkin;
import com.gymmanagement.model.Payment;
import com.gymmanagement.model.Subscription;
import com.gymmanagement.view.DashboardView;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Controller xử lý dữ liệu và logic cho Dashboard
 */
public class DashboardController {
    private final DashboardView view;
    private final SampleData data;

    public DashboardController(DashboardView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        loadData();
    }

    public void loadData() {
        int totalMembers = data.getMembers().size();

        // Đếm số hội viên có gói tập đang hoạt động
        int activeMembers = 0;
        for (Subscription sub : data.getSubscriptions()) {
            if ("Đang hoạt động".equalsIgnoreCase(sub.getStatus())) {
                activeMembers++;
            }
        }

        int totalPackages = data.getPackages().size();
        int todayCheckins = data.getCheckins().size();

        // Tính tổng doanh thu
        double totalRevenue = 0;
        for (Payment p : data.getPayments()) {
            totalRevenue += p.getAmount();
        }

        view.setStats(totalMembers, activeMembers, totalPackages, todayCheckins, totalRevenue);

        // Nạp bảng check-in gần nhất
        DefaultTableModel model = view.getCheckinTableModel();
        model.setRowCount(0);
        List<Checkin> checkins = data.getCheckins();
        int start = Math.max(0, checkins.size() - 6);
        for (int i = checkins.size() - 1; i >= start; i--) {
            Checkin c = checkins.get(i);
            model.addRow(new Object[]{
                    c.getCheckinId(),
                    c.getMemberId(),
                    c.getCheckinTime(),
                    c.getCheckoutTime()
            });
        }
    }
}
