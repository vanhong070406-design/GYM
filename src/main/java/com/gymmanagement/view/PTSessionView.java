package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Giao diện Quản lý lịch tập PT (PT Session Management)
 * Các trường: SessionID, StartTime, EndTime, Status, TrainerID, AreaID.
 * Sửa/xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số buổi tập.
 */
public class PTSessionView extends JPanel {
    public static final int ACTION_COLUMN = 8;
    private static final int FIRST_TRAINER_COLUMN = 4;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableColumn[] trainerColumns;
    private boolean trainerColumnsShown = true;
    private ActionColumn actionColumn;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnAdd;
    private TableHeader tableHeader;

    public PTSessionView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút đặt lịch bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Mã lịch, Mã PT, Mã hội viên hoặc Tên...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        cbFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Đã đặt", "Đang diễn ra", "Hoàn thành", "Đã hủy"});
        cbFilterStatus.setFont(UITheme.FONT_REGULAR);
        cbFilterStatus.setBackground(Color.WHITE);

        btnSearch = UITheme.createPrimaryButton("Lọc / Tìm");

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cbFilterStatus);
        filterPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Đặt lịch PT");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số buổi tập và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Lịch tập", "Làm mới (xóa bộ lọc và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã Buổi Tập", "Bắt Đầu", "Kết Thúc",
                "Trạng Thái", "Mã HLV", "Tên HLV", "Mã Hội Viên", "Tên Hội Viên", "Thao tác"
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
        // Ngày giờ luôn hiện đủ; các cột mã nhường chỗ khi hiện thêm 2 cột HLV
        int[] widths = {80, 140, 140, 95, 66, 125, 85, 125};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(1).setMinWidth(140);
        table.getColumnModel().getColumn(2).setMinWidth(140);
        trainerColumns = new TableColumn[]{
                table.getColumnModel().getColumn(FIRST_TRAINER_COLUMN),
                table.getColumnModel().getColumn(FIRST_TRAINER_COLUMN + 1)
        };
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.EDIT, "Chỉnh sửa buổi tập",
                Action.DELETE, "Xóa buổi tập"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /**
     * Ẩn/hiện 2 cột Mã HLV và Tên HLV; ẩn khi đang xem lịch của một HLV vì cột nào cũng giống nhau.
     * Chỉ gỡ cột khỏi phần hiển thị, dữ liệu trong tableModel giữ nguyên chỉ số cột.
     * Dùng cờ riêng vì cột Thao tác cũng có thể đang bị ẩn, đếm số cột không còn đúng.
     */
    public void setTrainerColumnsVisible(boolean visible) {
        if (visible == trainerColumnsShown) return;
        TableColumnModel cm = table.getColumnModel();
        if (visible) {
            for (int i = 0; i < trainerColumns.length; i++) {
                cm.addColumn(trainerColumns[i]);
                cm.moveColumn(cm.getColumnCount() - 1, FIRST_TRAINER_COLUMN + i);
            }
        } else {
            for (TableColumn c : trainerColumns) cm.removeColumn(c);
        }
        trainerColumnsShown = visible;
    }

    /** Đặt quyền cho bảng lịch tập: có quyền quản lý thì đặt lịch, sửa, xóa; chỉ xem thì ẩn nút thêm và cột Thao tác. */
    public void setManageable(boolean canManage) {
        btnAdd.setVisible(canManage);
        if (canManage) actionColumn.setActions(Action.EDIT, Action.DELETE);
        else actionColumn.setActions();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
