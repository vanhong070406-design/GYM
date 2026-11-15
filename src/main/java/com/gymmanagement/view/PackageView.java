package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý gói tập (Package Management)
 * Các trường: PackageID, PackageName, Price, Duration, Description.
 * Sửa/xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số gói tập.
 */
public class PackageView extends JPanel {
    public static final int ACTION_COLUMN = 5;

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnAdd;

    public PackageView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: tìm kiếm bên trái, nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(22);
        txtSearch.setToolTipText("Nhập mã hoặc tên gói tập...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Thêm gói tập");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số gói tập và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách gói tập", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Mã Gói", "Tên Gói Tập", "Giá Tiền (VNĐ)", "Số Ngày", "Mô Tả Quyền Lợi", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                if (actionColumn.isAt(e.getPoint())) return actionColumn.tooltipAt(e.getPoint());
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                // Ô bị cắt chữ vẫn xem được đầy đủ khi rê chuột
                Object value = getValueAt(row, col);
                return value == null || value.toString().isEmpty() ? null : value.toString();
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối
        // Mô tả quyền lợi dài nên được nhiều chỗ nhất
        int[] widths = {80, 220, 130, 90, 340};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.EDIT, "Chỉnh sửa gói tập",
                Action.DELETE, "Xóa gói tập"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Chỉ xem (Lễ tân): ẩn nút thêm và cột Thao tác. */
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.EDIT, Action.DELETE);
        else actionColumn.setActions();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
