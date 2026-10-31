package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý lịch tập PT (PT Session Management)
 * Các trường: SessionID, StartTime, EndTime, Status, TrainerID, AreaID
 */
public class PTSessionView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public PTSessionView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new BorderLayout(16, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Mã lịch, Mã PT, hoặc Mã Khu vực...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        cbFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Đã đặt", "Đang diễn ra", "Hoàn thành", "Đã hủy"});
        cbFilterStatus.setFont(UITheme.FONT_REGULAR);
        cbFilterStatus.setBackground(Color.WHITE);

        btnSearch = UITheme.createPrimaryButton("Lọc / Tìm");
        btnRefresh = UITheme.createSecondaryButton("Làm mới");

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cbFilterStatus);
        filterPanel.add(btnSearch);
        filterPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("+ Đặt lịch PT");
        btnEdit = UITheme.createSecondaryButton("Chỉnh sửa");
        btnDelete = UITheme.createDangerButton("Xóa");

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Bảng dữ liệu
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {
                "Mã Buổi Tập", "Thời Gian Bắt Đầu", "Thời Gian Kết Thúc", 
                "Trạng Thái", "Mã HLV (PT)", "Tên HLV", "Mã Khu Vực Tập"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnDelete() { return btnDelete; }
}
