package com.gymmanagement.view;

import com.gymmanagement.model.Equipment;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Giao diện Quản lý thiết bị (Equipment Management)
 * Các trường: EquipmentID, EquipmentName, Value, PurchaseDate, Status.
 * Trạng thái đổi ngay trong bảng bằng combobox; sửa/xóa bằng biểu tượng ở cột Thao tác.
 */
public class EquipmentView extends JPanel {
    public static final int STATUS_COLUMN = 4;
    public static final int ACTION_COLUMN = 5;
    private static final String FILTER_ALL = "Tất cả";

    private JTable table;
    private DefaultTableModel tableModel;
    private TableColumn actionColumn;
    /** Có quyền quản lý thì mới đổi được trạng thái và thấy cột Thao tác. */
    private boolean manageable = true;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnAdd;
    private TableHeader tableHeader;

    public EquipmentView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(11); // vừa đủ để thanh công cụ nằm trên một hàng ở cửa sổ mặc định
        txtSearch.setToolTipText("Mã thiết bị hoặc Tên máy...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        List<String> filterItems = new ArrayList<>();
        filterItems.add(FILTER_ALL);
        filterItems.addAll(Equipment.STATUSES);
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
        btnAdd = UITheme.createPrimaryButton("+ Thêm thiết bị");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số thiết bị và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách thiết bị", "Làm mới (xóa bộ lọc và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Mã Thiết Bị", "Tên Thiết Bị", "Giá Trị (VNĐ)", "Ngày Mua", "Trạng Thái Hoạt Động", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return manageable && column == STATUS_COLUMN;
            }
        };

        table = new JTable(tableModel) {
            // cửa sổ hẹp thì hiện thanh cuộn ngang để không bị mất cột
            @Override
            public boolean getScrollableTracksViewportWidth() { return UITheme.tracksViewportWidth(this); }

            @Override
            public String getToolTipText(MouseEvent e) {
                int col = columnAtPoint(e.getPoint());
                if (rowAtPoint(e.getPoint()) < 0) return null;
                if (col == ACTION_COLUMN) return isEditHalf(e.getPoint()) ? "Chỉnh sửa thiết bị" : "Xóa thiết bị";
                if (col == STATUS_COLUMN && manageable) return "Bấm để đổi trạng thái";
                return null;
            }
        };
        UITheme.styleTable(table);
        table.setSurrendersFocusOnKeystroke(true);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác và Trạng thái phải giữ đúng vị trí
        styleStatusColumn();
        styleActionColumn();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Ô trạng thái: chữ màu theo mức độ, có mũi tên ▾ khi đổi được; bấm một lần là mở combobox. */
    private void styleStatusColumn() {
        TableColumn col = table.getColumnModel().getColumn(STATUS_COLUMN);
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) col.getCellRenderer();
        col.setCellRenderer((t, value, selected, focus, row, c) -> {
            JLabel l = (JLabel) base.getTableCellRendererComponent(t, value, selected, focus, row, c);
            l.setText(manageable ? value + "   ▾" : String.valueOf(value));
            l.setForeground(statusColor(String.valueOf(value)));
            l.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
            return l;
        });

        JComboBox<String> editor = new JComboBox<>(Equipment.STATUSES.toArray(new String[0]));
        editor.setFont(UITheme.FONT_REGULAR);
        editor.setBackground(Color.WHITE);
        DefaultCellEditor cellEditor = new DefaultCellEditor(editor);
        cellEditor.setClickCountToStart(1);
        col.setCellEditor(cellEditor);
        col.setPreferredWidth(200);
    }

    private static Color statusColor(String status) {
        if (Equipment.STATUS_GOOD.equals(status)) return UITheme.SUCCESS;
        if (Equipment.STATUS_NEEDS_PARTS.equals(status)) return UITheme.DANGER;
        if (Equipment.STATUS_MAINTENANCE_DUE.equals(status)) return UITheme.WARNING;
        return UITheme.TEXT_DARK;
    }

    /** Cột Thao tác: bút chì (nửa trái) để sửa, thùng rác (nửa phải) để xóa. */
    private void styleActionColumn() {
        actionColumn = table.getColumnModel().getColumn(ACTION_COLUMN);
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) actionColumn.getCellRenderer();
        JLabel editIcon = new JLabel(Icons.edit(UITheme.PRIMARY_HOVER));
        JLabel deleteIcon = new JLabel(Icons.trash(UITheme.DANGER));
        JPanel cell = new JPanel(new GridLayout(1, 2));
        editIcon.setHorizontalAlignment(SwingConstants.CENTER);
        deleteIcon.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(editIcon);
        cell.add(deleteIcon);
        TableCellRenderer renderer = (t, value, selected, focus, row, c) -> {
            // Lấy màu nền xen kẽ / màu chọn từ renderer chung để cột này đồng bộ với các cột khác
            Component bg = base.getTableCellRendererComponent(t, "", selected, false, row, c);
            cell.setBackground(bg.getBackground());
            return cell;
        };
        actionColumn.setCellRenderer(renderer);
        actionColumn.setPreferredWidth(90);
        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                boolean clickable = table.rowAtPoint(e.getPoint()) >= 0
                        && (col == ACTION_COLUMN || (col == STATUS_COLUMN && manageable));
                table.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /** Điểm bấm trong cột Thao tác rơi vào nửa trái (biểu tượng sửa) hay không. */
    public boolean isEditHalf(Point p) {
        Rectangle cell = table.getCellRect(table.rowAtPoint(p), table.columnAtPoint(p), false);
        return p.x < cell.x + cell.width / 2;
    }

    /** Tài khoản chỉ xem: ẩn cột Thao tác, ô trạng thái chỉ hiện chữ. */
    public void setManageable(boolean manageable) {
        this.manageable = manageable;
        boolean shown = table.getColumnModel().getColumnCount() == tableModel.getColumnCount();
        if (!manageable && shown) table.getColumnModel().removeColumn(actionColumn);
        if (manageable && !shown) table.getColumnModel().addColumn(actionColumn);
        table.repaint();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
