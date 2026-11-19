package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Huấn luyện viên PT, gộp cả lịch tập PT.
 * Gồm 2 trang chuyển bằng CardLayout: danh sách HLV và trang chi tiết (thông tin + lịch tập của một HLV).
 * Xem chi tiết / sửa / xóa bằng biểu tượng ở cột Thao tác; nháy đúp vào dòng cũng mở trang chi tiết.
 */
public class TrainerView extends JPanel {
    public static final int ACTION_COLUMN = 5;
    private static final String PAGE_LIST = "LIST";
    private static final String PAGE_DETAIL = "DETAIL";

    private final CardLayout pages = new CardLayout();
    private final TrainerDetailView detailView = new TrainerDetailView();

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnAllSessions;
    private JButton btnAdd;
    private TableHeader tableHeader;

    public TrainerView() {
        setLayout(pages);
        setBackground(UITheme.BG_MAIN);
        add(createListPage(), PAGE_LIST);
        add(detailView, PAGE_DETAIL);
    }

    private JPanel createListPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(UITheme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Toolbar: tìm kiếm bên trái; lịch tập tất cả HLV và nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(18);
        txtSearch.setToolTipText("Mã HLV, Họ tên, hoặc Chuyên môn...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAllSessions = UITheme.createSecondaryButton("Lịch tập tất cả HLV");
        btnAdd = UITheme.createPrimaryButton("+ Thêm HLV");
        actionPanel.add(btnAllSessions);
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số HLV và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách huấn luyện viên", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Mã HLV", "Họ và Tên", "Số Điện Thoại", "Kinh Nghiệm", "Chuyên Môn Giảng Dạy", "Thao tác"};
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
        int[] widths = {80, 170, 120, 110, 260};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.VIEW, "Xem chi tiết và lịch tập",
                Action.EDIT, "Chỉnh sửa huấn luyện viên",
                Action.DELETE, "Xóa huấn luyện viên"));
        actionColumn.setActions(Action.VIEW, Action.EDIT, Action.DELETE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        page.add(topPanel, BorderLayout.NORTH);
        page.add(tablePanel, BorderLayout.CENTER);
        return page;
    }

    /** Quản lý: thêm, xem, sửa, xóa. Chỉ xem (Lễ tân): chỉ còn biểu tượng xem chi tiết. */
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.VIEW, Action.EDIT, Action.DELETE);
        else actionColumn.setActions(Action.VIEW);
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public void showList() { pages.show(this, PAGE_LIST); }
    public void showDetail() { pages.show(this, PAGE_DETAIL); }

    public TrainerDetailView getDetailView() { return detailView; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAllSessions() { return btnAllSessions; }
    public JButton getBtnAdd() { return btnAdd; }
}
