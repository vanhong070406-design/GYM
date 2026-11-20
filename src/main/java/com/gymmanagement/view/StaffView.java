package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// Giao diện Quản lý nhân viên (Staff Management)
// Các trường: StaffID, FullName, Phone, Salary, Shift, HireDate, Position, Username, trạng thái mật khẩu.
// Cấp tài khoản / sửa / xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số nhân viên.
// Chỉ Quản lý và Lễ tân được cấp tài khoản: dòng của chức vụ khác có chìa khóa mờ và cột tài khoản ghi "Không áp dụng".
// Mật khẩu được băm một chiều nên cột Mật khẩu chỉ hiện trạng thái, không hiện được mật khẩu thật.

public class StaffView extends JPanel {
    public static final int ACTION_COLUMN = 9;
    // Chữ ở cột Tên đăng nhập / Mật khẩu khi nhân viên chưa có tài khoản
    public static final String NO_ACCOUNT = "Chưa cấp";
    // Chữ ở cột Tên đăng nhập / Mật khẩu khi chức vụ không được cấp tài khoản (chỉ Quản lý, Lễ tân có)
    public static final String NOT_APPLICABLE = "Không áp dụng";

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;
    // Nhân viên đang đăng nhập: không được tự xóa nên biểu tượng xóa ở dòng này bị làm mờ
    private String currentStaffId;
    // Nhân viên có chức vụ được đăng nhập: chỉ các dòng này bấm được biểu tượng chìa khóa
    private Set<String> loginStaffIds = Set.of();

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnAdd;

    public StaffView() {
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
        txtSearch.setToolTipText("Mã nhân viên, Tên nhân viên, SĐT hoặc Tên đăng nhập...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Thêm nhân viên");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số nhân viên và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách nhân viên", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã NV", "Họ và Tên", "Số Điện Thoại", "Lương CB",
                "Ca Làm Việc", "Ngày Tuyển", "Chức Vụ", "Tên Đăng Nhập", "Mật Khẩu", "Thao tác"
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
                return value == null ? null : value.toString();
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối
        // Cột mã ngắn nhường chỗ cho Họ tên, Ca làm việc và Tên đăng nhập
        int[] widths = {66, 150, 106, 100, 140, 94, 106, 118, 108};
        int[] minWidths = {60, 100, 96, 88, 90, 90, 76, 84, 84};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
        }
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.ACCOUNT, "Cấp tài khoản / đặt lại mật khẩu",
                Action.EDIT, "Chỉnh sửa nhân viên",
                Action.DELETE, "Xóa nhân viên"));
        actionColumn.setActions(Action.ACCOUNT, Action.EDIT, Action.DELETE);
        actionColumn.setDisabledWhen((row, action) ->
                        (action == Action.DELETE && isCurrentUserRow(row)) || (action == Action.ACCOUNT && !canLoginRow(row)),
                Map.of(Action.DELETE, "Không thể xóa tài khoản đang đăng nhập",
                        Action.ACCOUNT, "Chức vụ này không được cấp tài khoản (chỉ Quản lý và Lễ tân)"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    // row: chỉ số dòng trong tableModel
    public boolean isCurrentUserRow(int row) {
        return currentStaffId != null && currentStaffId.equalsIgnoreCase(String.valueOf(tableModel.getValueAt(row, 0)));
    }

    public void setCurrentStaffId(String staffId) {
        this.currentStaffId = staffId;
        table.repaint();
    }

    private boolean canLoginRow(int row) {
        return loginStaffIds.contains(String.valueOf(tableModel.getValueAt(row, 0)));
    }

    public void setLoginStaffIds(Set<String> staffIds) {
        this.loginStaffIds = Set.copyOf(staffIds);
        table.repaint();
    }

    // Tài khoản không có quyền quản lý: ẩn nút thêm và cột Thao tác
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.ACCOUNT, Action.EDIT, Action.DELETE);
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
