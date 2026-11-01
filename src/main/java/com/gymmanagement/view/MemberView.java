package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý hội viên (Member Management)
 * Các trường: MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender
 */
public class MemberView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public MemberView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // 1. Action Toolbar Panel (Tìm kiếm, Thao tác CRUD)
        JPanel topPanel = UITheme.createCardPanel();

        // Nhánh trái: Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(18);
        txtSearch.setToolTipText("Nhập mã hội viên hoặc họ tên cần tìm...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
        btnRefresh = UITheme.createSecondaryButton("Làm mới");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Nhánh phải: Các nút Thêm, Sửa, Xóa
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("+ Thêm hội viên");
        btnEdit = UITheme.createSecondaryButton("Chỉnh sửa");
        btnDelete = UITheme.createDangerButton("Xóa");

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // 2. Bảng dữ liệu (Table)
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {
                "Mã Hội Viên", "Họ và Tên", "Email", "Số Điện Thoại", 
                "Ngày Tham Gia", "Địa Chỉ", "Ngày Sinh", "Giới Tính"
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

    // Getters
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnDelete() { return btnDelete; }
}
