package com.gymmanagement.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý huấn luyện viên (Trainer Management)
 * Các trường: TrainerID, FullName, Phone, Experience, Specialty
 */
public class TrainerView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public TrainerView() {
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

        txtSearch = UITheme.createTextField(18);
        txtSearch.setToolTipText("Mã PT, Họ tên, hoặc Chuyên môn...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
        btnRefresh = UITheme.createSecondaryButton("Làm mới");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("+ Thêm HLV");
        btnEdit = UITheme.createSecondaryButton("Chỉnh sửa");
        btnDelete = UITheme.createDangerButton("Xóa");

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Bảng dữ liệu
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {"Mã HLV (PT)", "Họ và Tên", "Số Điện Thoại", "Kinh Nghiệm", "Chuyên Môn Giảng Dạy"};
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
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnDelete() { return btnDelete; }
}
