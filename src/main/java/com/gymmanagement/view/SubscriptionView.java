package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý đăng ký gói tập (Subscription Management)
 * Các trường: SubscriptionID, StartDate, EndDate, Status, MemberID, PackageID
 */
public class SubscriptionView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public SubscriptionView() {
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
        txtSearch.setToolTipText("Mã đăng ký, Mã HV, hoặc Mã Gói...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        cbFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Đang hoạt động", "Hết hạn", "Tạm dừng"});
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

        btnAdd = UITheme.createPrimaryButton("+ Đăng ký mới");
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
                "Mã Đăng Ký", "Mã Hội Viên", "Tên Hội Viên", 
                "Mã Gói Tập", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái"
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
