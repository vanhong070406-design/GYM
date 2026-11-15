package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Giao diện Quản lý Check-in / Check-out
 * Các trường: CheckinID, MemberID, MemberName, CheckinTime, CheckoutTime, ReceptionistID.
 * Check-out và xóa bằng biểu tượng ở cột Thao tác; lượt chưa ra hiện "Đang tập" ở cột Giờ ra.
 */
public class CheckinView extends JPanel {
    public static final int CHECKOUT_COLUMN = 4;
    public static final int ACTION_COLUMN = 6;
    /** Chữ hiện ở cột Giờ ra khi hội viên chưa check-out. */
    public static final String STILL_INSIDE = "Đang tập";

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnCheckin;

    public CheckinView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: tìm kiếm bên trái, nút check-in bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(22);
        txtSearch.setToolTipText("Mã check-in, Mã hội viên hoặc Tên hội viên...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnCheckin = UITheme.createPrimaryButton("+ Check-in vào");
        actionPanel.add(btnCheckin);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số lượt, số người đang tập và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Lịch sử check-in", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã Check-in", "Mã Hội Viên", "Tên Hội Viên",
                "Giờ Vào", "Giờ Ra", "Lễ Tân Trực", "Thao tác"
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
        int[] widths = {100, 100, 180, 140, 140, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        styleCheckoutColumn();

        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.CHECKOUT, "Check-out ngay (ghi giờ ra là bây giờ)",
                Action.DELETE, "Xóa lượt check-in"));
        actionColumn.setActions(Action.CHECKOUT, Action.DELETE);
        actionColumn.setDisabledWhen((row, action) -> action == Action.CHECKOUT && !isInside(row),
                Map.of(Action.CHECKOUT, "Lượt này đã check-out"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Ô Giờ ra của lượt chưa check-out hiện "Đang tập" chữ đậm màu xanh lá. */
    private void styleCheckoutColumn() {
        TableColumn col = table.getColumnModel().getColumn(CHECKOUT_COLUMN);
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) col.getCellRenderer();
        col.setCellRenderer((t, value, selected, focus, row, c) -> {
            Component comp = base.getTableCellRendererComponent(t, value, selected, focus, row, c);
            if (STILL_INSIDE.equals(value)) {
                comp.setForeground(UITheme.SUCCESS);
                comp.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
            } else {
                comp.setFont(UITheme.FONT_REGULAR);
            }
            return comp;
        });
    }

    /** row: chỉ số dòng trong tableModel; true nếu hội viên chưa check-out. */
    public boolean isInside(int row) {
        return STILL_INSIDE.equals(tableModel.getValueAt(row, CHECKOUT_COLUMN));
    }

    /** Tài khoản không có quyền quản lý: ẩn nút check-in và cột Thao tác. */
    public void setManageable(boolean manageable) {
        btnCheckin.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.CHECKOUT, Action.DELETE);
        else actionColumn.setActions();
    }

    /** Số lượt đang hiện, kèm số người chưa check-out (nếu có). */
    public void setCount(int shown, int inside) {
        tableHeader.setCount(shown, inside > 0 ? inside + " đang tập" : null);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnCheckin() { return btnCheckin; }
}
