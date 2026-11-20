package com.gymmanagement.view;

import com.gymmanagement.model.MaintenanceAssignment;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Giao diện Quản lý bảo trì thiết bị (Maintenance Assignment), cùng kiểu với màn Thiết bị:
 * đổi trạng thái phiếu ngay trong bảng bằng combobox; sửa/xóa bằng biểu tượng ở cột Thao tác.
 * Phiếu đã hoàn thành bị khóa trạng thái, vì mở lại phiếu không trả thiết bị về tình trạng cũ.
 */
public class MaintenanceView extends JPanel {
    public static final int DATE_COLUMN = 0;
    public static final int STATUS_COLUMN = 4;
    public static final int ACTION_COLUMN = 5;
    public static final String FILTER_ALL = "Tất cả";

    /** Biểu tượng được bấm trong cột Thao tác. */
    public enum Action { EDIT, DELETE }

    private JTable table;
    private DefaultTableModel tableModel;
    private TableColumn actionColumn;
    private JPanel actionCell;
    private JLabel editIcon;
    private JLabel deleteIcon;
    /** Có quyền quản lý: đổi trạng thái, sửa và xóa phiếu. */
    private boolean manageable = true;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JLabel lblCount;

    public MaintenanceView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút phân công bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(11);
        txtSearch.setToolTipText("Mã/tên nhân viên, mã/tên thiết bị hoặc nội dung...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        List<String> filterItems = new ArrayList<>();
        filterItems.add(FILTER_ALL);
        filterItems.addAll(MaintenanceAssignment.STATUSES);
        cbFilterStatus = new JComboBox<>(filterItems.toArray(new String[0]));
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
        btnAdd = UITheme.createPrimaryButton("+ Phân công bảo trì");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số phiếu và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tablePanel.add(createTableHeader(), BorderLayout.NORTH);

        String[] columns = {"Ngày Phân Công", "Thiết Bị", "Nhân Viên Kỹ Thuật", "Nội Dung Bảo Trì", "Trạng Thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == STATUS_COLUMN && isStatusEditable(row);
            }
        };

        table = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                int modelCol = convertColumnIndexToModel(col);
                if (modelCol == ACTION_COLUMN) return actionAt(e.getPoint()) == Action.EDIT ? "Sửa nội dung phiếu" : "Xóa phiếu bảo trì";
                if (modelCol == STATUS_COLUMN) {
                    if (isStatusEditable(row)) return "Bấm để đổi trạng thái";
                    if (MaintenanceAssignment.STATUS_DONE.equals(tableModel.getValueAt(row, STATUS_COLUMN))) {
                        return "Phiếu đã hoàn thành, không đổi trạng thái được nữa";
                    }
                }
                if (modelCol == 3) return String.valueOf(tableModel.getValueAt(row, 3)); // nội dung dài bị cắt thì xem đủ ở đây
                return null;
            }
        };
        UITheme.styleTable(table);
        table.setRowHeight(46); // cột Thiết bị và Nhân viên có hai dòng: tên và mã
        table.setSurrendersFocusOnKeystroke(true);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác và Trạng thái phải giữ đúng vị trí
        table.getColumnModel().getColumn(DATE_COLUMN).setPreferredWidth(150);
        table.getColumnModel().getColumn(DATE_COLUMN).setMinWidth(150); // đủ chỗ cho tiêu đề "Ngày Phân Công"
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        styleStatusColumn();
        styleActionColumn();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        lblCount = new JLabel("Danh sách phiếu bảo trì");
        lblCount.setFont(UITheme.FONT_HEADER);
        lblCount.setForeground(UITheme.TEXT_DARK);

        btnRefresh = new JButton(Icons.refresh(UITheme.TEXT_MUTED));
        UITheme.styleButton(btnRefresh, UITheme.TEXT_MUTED, Color.WHITE, new Color(241, 245, 249), UITheme.BORDER, UITheme.BORDER);
        btnRefresh.setPreferredSize(new Dimension(34, 30));
        btnRefresh.setToolTipText("Làm mới (xóa bộ lọc và tải lại)");
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(lblCount, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        return header;
    }

    private boolean isStatusEditable(int row) {
        return manageable && !MaintenanceAssignment.STATUS_DONE.equals(tableModel.getValueAt(row, STATUS_COLUMN));
    }

    /**
     * Ô trạng thái: chữ màu theo tiến độ, có mũi tên ▾ khi đổi được, ổ khóa khi đã hoàn thành.
     * Dùng nhãn riêng thay vì renderer chung của bảng để biểu tượng ổ khóa không lan sang cột khác.
     */
    private void styleStatusColumn() {
        TableColumn col = table.getColumnModel().getColumn(STATUS_COLUMN);
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) col.getCellRenderer();
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setIconTextGap(6);
        Icon lock = Icons.lock(UITheme.TEXT_MUTED);
        col.setCellRenderer((t, value, selected, focus, row, c) -> {
            JComponent bg = (JComponent) base.getTableCellRendererComponent(t, "", selected, focus, row, c);
            String status = String.valueOf(value);
            boolean done = MaintenanceAssignment.STATUS_DONE.equals(status);
            label.setBackground(bg.getBackground());
            label.setBorder(bg.getBorder());
            label.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
            label.setForeground(statusColor(status));
            label.setText(isStatusEditable(row) ? status + "   ▾" : status);
            label.setIcon(done ? lock : null);
            return label;
        });

        JComboBox<String> editor = new JComboBox<>(MaintenanceAssignment.STATUSES.toArray(new String[0]));
        editor.setFont(UITheme.FONT_REGULAR);
        editor.setBackground(Color.WHITE);
        DefaultCellEditor cellEditor = new DefaultCellEditor(editor);
        cellEditor.setClickCountToStart(1);
        col.setCellEditor(cellEditor);
        col.setPreferredWidth(170);
    }

    private static Color statusColor(String status) {
        if (MaintenanceAssignment.STATUS_DONE.equals(status)) return UITheme.SUCCESS;
        if (MaintenanceAssignment.STATUS_WAITING_PARTS.equals(status)) return UITheme.DANGER;
        if (MaintenanceAssignment.STATUS_IN_PROGRESS.equals(status)) return UITheme.WARNING;
        return UITheme.TEXT_DARK;
    }

    /** Cột Thao tác: bút chì (nửa trái) để sửa, thùng rác (nửa phải) để xóa; không có quyền xóa thì chỉ còn bút chì. */
    private void styleActionColumn() {
        actionColumn = table.getColumnModel().getColumn(ACTION_COLUMN);
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) actionColumn.getCellRenderer();
        editIcon = new JLabel(Icons.edit(UITheme.PRIMARY_HOVER));
        deleteIcon = new JLabel(Icons.trash(UITheme.DANGER));
        editIcon.setHorizontalAlignment(SwingConstants.CENTER);
        deleteIcon.setHorizontalAlignment(SwingConstants.CENTER);
        actionCell = new JPanel(new GridLayout(1, 2));
        actionCell.add(editIcon);
        actionCell.add(deleteIcon);
        actionColumn.setCellRenderer((t, value, selected, focus, row, c) -> {
            // Lấy màu nền xen kẽ / màu chọn từ renderer chung để cột này đồng bộ với các cột khác
            Component bg = base.getTableCellRendererComponent(t, "", selected, false, row, c);
            actionCell.setBackground(bg.getBackground());
            return actionCell;
        });
        actionColumn.setPreferredWidth(90);
        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                boolean clickable = row >= 0 && col >= 0 && (table.convertColumnIndexToModel(col) == ACTION_COLUMN
                        || (table.convertColumnIndexToModel(col) == STATUS_COLUMN && isStatusEditable(row)));
                table.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /** Biểu tượng nằm dưới điểm bấm trong cột Thao tác. */
    public Action actionAt(Point p) {
        Rectangle cell = table.getCellRect(table.rowAtPoint(p), table.columnAtPoint(p), false);
        return p.x < cell.x + cell.width / 2 ? Action.EDIT : Action.DELETE;
    }

    /**
     * Đặt quyền cho màn hình: có quyền quản lý thì đổi trạng thái, sửa, xóa được.
     * Chỉ xem thì ẩn cột Thao tác, ô trạng thái chỉ hiện chữ.
     */
    public void setManageable(boolean manageable) {
        this.manageable = manageable;
        boolean shown = table.getColumnModel().getColumnCount() == tableModel.getColumnCount();
        if (!manageable && shown) table.getColumnModel().removeColumn(actionColumn);
        if (manageable && !shown) table.getColumnModel().addColumn(actionColumn);
        table.repaint();
    }

    public void setCount(int shown) {
        lblCount.setText("Danh sách phiếu bảo trì · " + shown);
    }

    /** Ô hai dòng: tên ở trên, mã nhỏ màu xám ở dưới. */
    public static String twoLine(String name, String code) {
        // nobr: tên dài bị cắt bớt thay vì xuống dòng và đẩy mã ra khỏi ô
        return "<html><nobr>" + escape(name) + "</nobr><br><nobr><span style='color:#64748B;font-size:10px'>"
                + escape(code) + "</span></nobr></html>";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnAdd() { return btnAdd; }
}
