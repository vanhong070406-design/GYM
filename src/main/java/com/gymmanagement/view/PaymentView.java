package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý thanh toán (Payment Management)
 * Các trường: PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID
 */
public class PaymentView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterMethod;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnDelete;

    public PaymentView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Mã thanh toán hoặc Mã đăng ký...");

        JLabel lblMethod = new JLabel("Phương thức:");
        lblMethod.setFont(UITheme.FONT_BOLD);
        lblMethod.setForeground(UITheme.TEXT_DARK);

        cbFilterMethod = new JComboBox<>(new String[]{"Tất cả", "Chuyển khoản", "Tiền mặt", "Thẻ tín dụng"});
        cbFilterMethod.setFont(UITheme.FONT_REGULAR);
        cbFilterMethod.setBackground(Color.WHITE);

        btnSearch = UITheme.createPrimaryButton("Lọc dữ liệu");
        btnRefresh = UITheme.createSecondaryButton("Làm mới");

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblMethod);
        filterPanel.add(cbFilterMethod);
        filterPanel.add(btnSearch);
        filterPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("+ Lập hóa đơn");
        btnDelete = UITheme.createDangerButton("Xóa");

        actionPanel.add(btnAdd);
        actionPanel.add(btnDelete);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {
                "Mã Hóa Đơn", "Ngày Thanh Toán", "Phương Thức", 
                "Số Tiền (VNĐ)", "Mã Đăng Ký (SubID)", "Mã Lễ Tân Thu"
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
    public JComboBox<String> getCbFilterMethod() { return cbFilterMethod; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnDelete() { return btnDelete; }
}
