package com.gymmanagement.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Trang chi tiết một huấn luyện viên: thông tin, thống kê buổi tập và bảng lịch tập PT của người đó.
 * Cũng dùng để xem lịch tập của tất cả HLV, khi đó phần thông tin được ẩn.
 */
public class TrainerDetailView extends JPanel {
    private final JButton btnBack = UITheme.createSecondaryButton("← Danh sách HLV");
    private final JLabel lblTitle = new JLabel(" ");
    private final JLabel lblPhone = infoValue();
    private final JLabel lblExperience = infoValue();
    private final JLabel lblSpecialties = infoValue();
    private final JLabel lblTotal = statValue();
    private final JLabel lblCompleted = statValue();
    private final JLabel lblUpcoming = statValue();
    private final JLabel lblCancelled = statValue();
    private final JPanel infoPanel = new JPanel(new BorderLayout(24, 0));
    private final JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
    private final PTSessionView sessionView = new PTSessionView();

    public TrainerDetailView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        JPanel headerCard = UITheme.createCardPanel();
        headerCard.setLayout(new BorderLayout(0, 14));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titleRow.add(btnBack);
        titleRow.add(lblTitle);

        JPanel details = new JPanel(new GridLayout(3, 1, 0, 4));
        details.setOpaque(false);
        details.add(infoRow("Số điện thoại:", lblPhone));
        details.add(infoRow("Kinh nghiệm:", lblExperience));
        details.add(infoRow("Chuyên môn:", lblSpecialties));

        statsPanel.setOpaque(false);
        statsPanel.add(statBox("Tổng buổi", lblTotal));
        statsPanel.add(statBox("Hoàn thành", lblCompleted));
        statsPanel.add(statBox("Sắp tới", lblUpcoming));
        statsPanel.add(statBox("Đã hủy", lblCancelled));

        infoPanel.setOpaque(false);
        infoPanel.add(details, BorderLayout.CENTER);
        infoPanel.add(statsPanel, BorderLayout.EAST);

        headerCard.add(titleRow, BorderLayout.NORTH);
        headerCard.add(infoPanel, BorderLayout.CENTER);

        // Bảng lịch tập dùng lại nguyên màn hình Lịch tập PT, bỏ lề vì đã nằm trong trang này
        sessionView.setBorder(BorderFactory.createEmptyBorder());

        add(headerCard, BorderLayout.NORTH);
        add(sessionView, BorderLayout.CENTER);
    }

    private static JLabel infoValue() {
        JLabel l = new JLabel("-");
        l.setFont(UITheme.FONT_REGULAR);
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private static JLabel statValue() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(UITheme.FONT_BIG_STAT.deriveFont(22f));
        l.setForeground(UITheme.PRIMARY);
        return l;
    }

    private static JPanel infoRow(String label, JLabel value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD.deriveFont(14f));
        lbl.setForeground(UITheme.TEXT_MUTED);
        row.add(lbl);
        row.add(value);
        return row;
    }

    private static JPanel statBox(String label, JLabel value) {
        JPanel box = new JPanel(new BorderLayout(0, 2));
        box.setBackground(UITheme.PRIMARY_LIGHT);
        box.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(8, 16, 8, 16)));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        box.add(value, BorderLayout.CENTER);
        box.add(lbl, BorderLayout.SOUTH);
        return box;
    }

    /** Hiện thông tin của một HLV. */
    public void showTrainer(String title, String phone, String experience, String specialties) {
        lblTitle.setText(title);
        lblPhone.setText(phone);
        lblExperience.setText(experience);
        lblSpecialties.setText(specialties);
        infoPanel.setVisible(true);
    }

    /** Chế độ xem lịch tập của tất cả HLV: chỉ còn tiêu đề và bảng. */
    public void showAllSessions(String title) {
        lblTitle.setText(title);
        infoPanel.setVisible(false);
    }

    public void setStats(int total, int completed, int upcoming, int cancelled) {
        lblTotal.setText(String.valueOf(total));
        lblCompleted.setText(String.valueOf(completed));
        lblUpcoming.setText(String.valueOf(upcoming));
        lblCancelled.setText(String.valueOf(cancelled));
    }

    /** Tài khoản không có quyền xem lịch tập thì chỉ thấy thông tin HLV. */
    public void setSessionsVisible(boolean visible) {
        sessionView.setVisible(visible);
        statsPanel.setVisible(visible);
    }

    public JButton getBtnBack() { return btnBack; }
    public PTSessionView getSessionView() { return sessionView; }
}
