package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý thanh toán (Payment Management)
 * Các trường: PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID.
 * Hóa đơn không sửa được, chỉ xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số hóa đơn.
 */
public class PaymentView extends JPanel {
    public static final int ACTION_COLUMN = 6;

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterMethod;
    private JButton btnSearch;
    private JButton btnAdd;

    public PaymentView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút lập hóa đơn bên phải
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

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblMethod);
        filterPanel.add(cbFilterMethod);
        filterPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Lập hóa đơn");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số hóa đơn và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách hóa đơn", "Làm mới (xóa bộ lọc và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã Hóa Đơn", "Ngày Thanh Toán", "Phương Thức",
                "Số Tiền (VNĐ)", "Mã Đăng Ký", "Lễ Tân Thu", "Thao tác"
        };
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
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(Action.DELETE, "Xóa hóa đơn"));
        actionColumn.setActions(Action.DELETE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Tài khoản không có quyền quản lý: ẩn nút lập hóa đơn và cột Thao tác. */
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.DELETE);
        else actionColumn.setActions();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterMethod() { return cbFilterMethod; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
