package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý Check-in (Check-in Management)
 * Các trường: CheckinID, CheckinTime, CheckoutTime, MemberID, ReceptionistID
 */
public class CheckinView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnCheckin;
    private JButton btnCheckout;
    private JButton btnDelete;

    public CheckinView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new BorderLayout(16, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(16);
        txtSearch.setToolTipText("Mã check-in hoặc Mã hội viên...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
        btnRefresh = UITheme.createSecondaryButton("Làm mới");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnCheckin = UITheme.createPrimaryButton("+ Check-in vào");
        btnCheckout = UITheme.createSecondaryButton("Check-out ra");
        btnDelete = UITheme.createDangerButton("Xóa");

        actionPanel.add(btnCheckin);
        actionPanel.add(btnCheckout);
        actionPanel.add(btnDelete);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Bảng dữ liệu
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {
                "Mã Check-in", "Mã Hội Viên", "Tên Hội Viên", 
                "Giờ Vào (Check-in)", "Giờ Ra (Check-out)", "Mã Lễ Tân Trực"
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
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnCheckin() { return btnCheckin; }
    public JButton getBtnCheckout() { return btnCheckout; }
    public JButton getBtnDelete() { return btnDelete; }
}
