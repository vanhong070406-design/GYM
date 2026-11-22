package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import com.gymmanagement.view.TableHeader.Note;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý đăng ký gói tập (Subscription Management)
 * Cột Còn lại và Thanh toán tô màu theo tình trạng; gia hạn / thu tiền / sửa / xóa bằng biểu tượng ở cột Thao tác.
 */
public class SubscriptionView extends JPanel {
    public static final int REMAINING_COLUMN = 5;
    public static final int PAYMENT_COLUMN = 6;
    public static final int ACTION_COLUMN = 7;

    public static final String FILTER_ALL = "Tất cả";
    public static final String FILTER_ACTIVE = "Đang hoạt động";
    public static final String FILTER_EXPIRING = "Sắp hết hạn (≤ 7 ngày)";
    public static final String FILTER_EXPIRED = "Hết hạn";
    public static final String FILTER_PAUSED = "Tạm dừng";
    public static final String FILTER_UNPAID = "Chưa thu đủ tiền";

    /** Giá trị một ô: chữ hiển thị, màu chữ (null = màu thường, không đậm) và tooltip (null = chính chữ đó). */
    public record Cell(String text, Color color, String tooltip) {
        @Override
        public String toString() { return text; }
    }

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnAdd;

    public SubscriptionView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút đăng ký mới bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(13);
        txtSearch.setToolTipText("Mã đăng ký, mã / tên hội viên, mã / tên gói...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        cbFilterStatus = new JComboBox<>(new String[]{
                FILTER_ALL, FILTER_ACTIVE, FILTER_EXPIRING, FILTER_EXPIRED, FILTER_PAUSED, FILTER_UNPAID});
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
        btnAdd = UITheme.createPrimaryButton("+ Đăng ký mới");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số đăng ký kèm thống kê nhanh và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách đăng ký", "Làm mới (xóa bộ lọc và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Mã ĐK", "Hội Viên", "Gói Tập", "Bắt Đầu", "Kết Thúc", "Còn Lại", "Thanh Toán", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
            // cửa sổ hẹp thì hiện thanh cuộn ngang để không bị mất cột
            @Override
            public boolean getScrollableTracksViewportWidth() { return UITheme.tracksViewportWidth(this); }

            @Override
            public String getToolTipText(MouseEvent e) {
                if (actionColumn.isAt(e.getPoint())) return actionColumn.tooltipAt(e.getPoint());
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                Object value = getValueAt(row, col);
                if (value instanceof Cell c && c.tooltip() != null) return c.tooltip();
                return value == null || value.toString().isEmpty() ? null : value.toString();
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối
        // Ngày, Còn lại và Thanh toán phải đọc trọn ("Quá hạn 90 ngày", "Còn nợ 1.350.000"); tên dài xem đủ qua tooltip
        int[] widths = {70, 135, 150, 100, 100, 140, 132};
        int[] minWidths = {66, 96, 95, 96, 96, 138, 128};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
        }
        styleColoredCells();

        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.RENEW, "Gia hạn (tạo đăng ký nối tiếp)",
                Action.PAY, "Thu tiền (lập hóa đơn)",
                Action.EDIT, "Chỉnh sửa đăng ký",
                Action.DELETE, "Xóa đăng ký"));
        actionColumn.setActions(Action.RENEW, Action.PAY, Action.EDIT, Action.DELETE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Ô kiểu {@link Cell} có màu thì chữ đậm và tô màu đó; ô khác giữ như renderer chung. */
    private void styleColoredCells() {
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) table.getColumnModel().getColumn(0).getCellRenderer();
        for (int col : new int[]{REMAINING_COLUMN, PAYMENT_COLUMN}) {
            table.getColumnModel().getColumn(col).setCellRenderer((t, value, selected, focus, row, c) -> {
                Component comp = base.getTableCellRendererComponent(t, value, selected, focus, row, c);
                if (value instanceof Cell cell && cell.color() != null) {
                    comp.setForeground(cell.color());
                    comp.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
                } else {
                    comp.setFont(UITheme.FONT_REGULAR);
                }
                return comp;
            });
        }
    }

    /** Tài khoản không có quyền quản lý: ẩn nút đăng ký và cột Thao tác. */
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.RENEW, Action.PAY, Action.EDIT, Action.DELETE);
        else actionColumn.setActions();
    }

    /** Số dòng đang hiện, kèm thống kê trên toàn bộ đăng ký (không phụ thuộc bộ lọc). */
    public void setSummary(int shown, int active, int expiring, int unpaid) {
        tableHeader.setCount(shown,
                active > 0 ? new Note(active + " đang hoạt động", UITheme.SUCCESS) : null,
                expiring > 0 ? new Note(expiring + " sắp hết hạn", UITheme.WARNING) : null,
                unpaid > 0 ? new Note(unpaid + " chưa thu đủ", UITheme.DANGER) : null);
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
