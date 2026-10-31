package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Dashboard (Tổng quan phòng gym)
 * Hiển thị các thẻ chỉ số (KPIs), biểu đồ doanh thu minh họa và bảng hoạt động gần nhất
 */
public class DashboardView extends JPanel {
    private JLabel lblTotalMembers;
    private JLabel lblActiveMembers;
    private JLabel lblTotalPackages;
    private JLabel lblTodayCheckins;
    private JLabel lblTotalRevenue;

    private JTable tblRecentCheckins;
    private DefaultTableModel checkinTableModel;
    private RevenueChartPanel revenueChart;

    public DashboardView() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // 1. Thẻ chỉ số tổng quan (Top Metric Cards)
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 16, 0));
        statsPanel.setOpaque(false);

        lblTotalMembers = new JLabel("0");
        lblActiveMembers = new JLabel("0");
        lblTotalPackages = new JLabel("0");
        lblTodayCheckins = new JLabel("0");
        lblTotalRevenue = new JLabel("0 đ");

        statsPanel.add(createStatCard("Tổng hội viên", lblTotalMembers, "Hội viên đã đăng ký", UITheme.PRIMARY));
        statsPanel.add(createStatCard("Đang hoạt động", lblActiveMembers, "Thẻ còn hạn", UITheme.SUCCESS));
        statsPanel.add(createStatCard("Gói tập", lblTotalPackages, "Các gói đang mở", UITheme.PRIMARY_HOVER));
        statsPanel.add(createStatCard("Check-in hôm nay", lblTodayCheckins, "Lượt vào phòng", UITheme.WARNING));
        statsPanel.add(createStatCard("Tổng doanh thu", lblTotalRevenue, "Doanh thu tích lũy", UITheme.PRIMARY));

        // 2. Nội dung trung tâm: Biểu đồ doanh thu + Bảng hoạt động gần đây
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        // Panel biểu đồ
        JPanel chartCard = UITheme.createCardPanel();
        chartCard.setLayout(new BorderLayout(10, 10));
        JLabel lblChartTitle = new JLabel("Doanh thu 6 tháng gần nhất (Triệu VNĐ)");
        lblChartTitle.setFont(UITheme.FONT_HEADER);
        lblChartTitle.setForeground(UITheme.TEXT_DARK);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        revenueChart = new RevenueChartPanel();
        chartCard.add(revenueChart, BorderLayout.CENTER);

        // Panel bảng check-in gần đây
        JPanel recentCard = UITheme.createCardPanel();
        recentCard.setLayout(new BorderLayout(10, 10));
        JLabel lblRecentTitle = new JLabel("Lịch sử ra vào mới nhất");
        lblRecentTitle.setFont(UITheme.FONT_HEADER);
        lblRecentTitle.setForeground(UITheme.TEXT_DARK);
        recentCard.add(lblRecentTitle, BorderLayout.NORTH);

        String[] cols = {"Mã Check-in", "Mã Hội Viên", "Thời Gian Vào", "Thời Gian Ra"};
        checkinTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRecentCheckins = new JTable(checkinTableModel);
        UITheme.styleTable(tblRecentCheckins);
        JScrollPane scrollTable = new JScrollPane(tblRecentCheckins);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(scrollTable, BorderLayout.CENTER);

        contentPanel.add(chartCard);
        contentPanel.add(recentCard);

        add(statsPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_BIG_STAT);
        valueLabel.setForeground(accentColor);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblSub);

        return card;
    }

    public void setStats(int totalMembers, int activeMembers, int totalPackages, int todayCheckins, double totalRevenue) {
        lblTotalMembers.setText(String.valueOf(totalMembers));
        lblActiveMembers.setText(String.valueOf(activeMembers));
        lblTotalPackages.setText(String.valueOf(totalPackages));
        lblTodayCheckins.setText(String.valueOf(todayCheckins));
        lblTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
    }

    public DefaultTableModel getCheckinTableModel() {
        return checkinTableModel;
    }

    /**
     * Panel vẽ biểu đồ cột trực quan đơn giản, tinh tế bằng Graphics2D
     */
    public static class RevenueChartPanel extends JPanel {
        private final String[] months = {"T12", "T1", "T2", "T3", "T4", "T5"};
        private final double[] values = {18.5, 24.0, 16.5, 29.8, 35.2, 42.0}; // Triệu VNĐ

        public RevenueChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(350, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 35;
            int bottomPadding = 30;

            int chartHeight = height - padding - bottomPadding;
            int chartWidth = width - 2 * padding;

            double maxVal = 50.0;

            // Đường kẻ ngang mờ
            g2.setColor(new Color(241, 245, 249));
            for (int i = 0; i <= 5; i++) {
                int y = padding + (int) (chartHeight * (1.0 - (double) i / 5));
                g2.drawLine(padding, y, width - padding, y);
            }

            int barWidth = Math.max(16, (chartWidth / months.length) - 20);

            for (int i = 0; i < months.length; i++) {
                int barHeight = (int) ((values[i] / maxVal) * chartHeight);
                int x = padding + (i * (chartWidth / months.length)) + ((chartWidth / months.length - barWidth) / 2);
                int y = padding + chartHeight - barHeight;

                // Cột thanh bar
                g2.setColor(i == months.length - 1 ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY);
                g2.fillRect(x, y, barWidth, barHeight);

                // Nhãn tháng phía dưới
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                int labelX = x + (barWidth - fm.stringWidth(months[i])) / 2;
                g2.drawString(months[i], labelX, height - 10);

                // Giá trị trên đầu cột
                String valStr = String.format("%.1f", values[i]);
                int valX = x + (barWidth - fm.stringWidth(valStr)) / 2;
                g2.drawString(valStr, valX, y - 5);
            }
        }
    }
}
