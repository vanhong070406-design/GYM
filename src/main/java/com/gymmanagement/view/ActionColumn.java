package com.gymmanagement.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;


// Cột Thao tác dạng biểu tượng ở cuối bảng: xem / sửa / check-out / xóa / cấp tài khoản.
// Các biểu tượng chia đều bề ngang ô; tập biểu tượng đổi theo quyền bằng {@link #setActions},
// không còn biểu tượng nào thì cột bị ẩn. Biểu tượng có thể bị làm mờ theo từng dòng bằng {@link #setDisabledWhen}.

public final class ActionColumn {
    public enum Action { VIEW, RENEW, PAY, ACCOUNT, EDIT, CHECKOUT, DELETE }

    private static final Color DISABLED = UITheme.BORDER;

    private final JTable table;
    private final TableColumn column;
    private final int modelIndex;
    private final Map<Action, JLabel> labels = new EnumMap<>(Action.class);
    private final Map<Action, Icon> enabledIcons = new EnumMap<>(Action.class);
    private final Map<Action, Icon> disabledIcons = new EnumMap<>(Action.class);
    private final Map<Action, String> tooltips = new EnumMap<>(Action.class);
    private final Map<Action, String> disabledTooltips = new EnumMap<>(Action.class);
    private final JPanel cell = new JPanel();
    private List<Action> actions = List.of();
    private BiPredicate<Integer, Action> disabledWhen = (row, action) -> false;
    private boolean shown = true;

    // tooltips: chữ hiện khi rê chuột lên từng biểu tượng, ví dụ VIEW → "Xem chi tiết HLV". 
    public ActionColumn(JTable table, int modelIndex, Map<Action, String> tooltips) {
        this.table = table;
        this.modelIndex = modelIndex;
        this.column = table.getColumnModel().getColumn(modelIndex);
        this.tooltips.putAll(tooltips);

        register(Action.VIEW, Icons.eye(UITheme.TEXT_MUTED), Icons.eye(DISABLED));
        register(Action.RENEW, Icons.calendarPlus(UITheme.PRIMARY_HOVER), Icons.calendarPlus(DISABLED));
        register(Action.PAY, Icons.cash(UITheme.SUCCESS), Icons.cash(DISABLED));
        register(Action.ACCOUNT, Icons.key(UITheme.WARNING), Icons.key(DISABLED));
        register(Action.EDIT, Icons.edit(UITheme.PRIMARY_HOVER), Icons.edit(DISABLED));
        register(Action.CHECKOUT, Icons.logout(UITheme.SUCCESS), Icons.logout(DISABLED));
        register(Action.DELETE, Icons.trash(UITheme.DANGER), Icons.trash(DISABLED));

        DefaultTableCellRenderer base = (DefaultTableCellRenderer) column.getCellRenderer();
        column.setCellRenderer((t, value, selected, focus, row, c) -> {
            // Lấy màu nền xen kẽ / màu chọn từ renderer chung để cột này đồng bộ với các cột khác
            Component bg = base.getTableCellRendererComponent(t, "", selected, false, row, c);
            cell.setBackground(bg.getBackground());
            for (Action a : actions) {
                labels.get(a).setIcon(isDisabled(row, a) ? disabledIcons.get(a) : enabledIcons.get(a));
            }
            return cell;
        });
        column.setResizable(false);
        setActions(Action.EDIT, Action.DELETE);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean clickable = actionAt(e.getPoint()) != null;
                table.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void register(Action action, Icon enabled, Icon disabled) {
        JLabel l = new JLabel(enabled);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        labels.put(action, l);
        enabledIcons.put(action, enabled);
        disabledIcons.put(action, disabled);
    }

    // Đặt các biểu tượng hiện trong cột, theo thứ tự trái sang phải; không truyền gì thì ẩn cột. 
    public void setActions(Action... list) {
        actions = List.of(list);
        cell.removeAll();
        cell.setLayout(new GridLayout(1, Math.max(1, actions.size())));
        for (Action a : actions) cell.add(labels.get(a));

        // Đủ chỗ cho tiêu đề "Thao tác" khi chỉ còn một biểu tượng
        int width = Math.max(84, (actions.size() >= 4 ? 30 : 34) * actions.size() + 22);
        // Mở giới hạn trên trước: setMinWidth bị chặn bởi maxWidth cũ nên cột sẽ không rộng ra được
        column.setMaxWidth(Integer.MAX_VALUE);
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setPreferredWidth(width);

        boolean show = !actions.isEmpty();
        if (show != shown) {
            if (show) table.getColumnModel().addColumn(column);
            else table.getColumnModel().removeColumn(column);
            shown = show;
        }
        table.repaint();
    }

    
    // Làm mờ biểu tượng ở những dòng thỏa điều kiện (row là chỉ số dòng trong tableModel); bấm vào không có tác dụng.
    // reasons: tooltip giải thích vì sao biểu tượng bị mờ, theo từng loại.
    
    public void setDisabledWhen(BiPredicate<Integer, Action> disabledWhen, Map<Action, String> reasons) {
        this.disabledWhen = disabledWhen;
        disabledTooltips.clear();
        disabledTooltips.putAll(reasons);
        table.repaint();
    }

    private boolean isDisabled(int viewRow, Action action) {
        return disabledWhen.test(table.convertRowIndexToModel(viewRow), action);
    }

    // Điểm nằm trên ô của cột Thao tác (có dữ liệu) hay không. 
    public boolean isAt(Point p) {
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);
        return shown && row >= 0 && col >= 0 && table.convertColumnIndexToModel(col) == modelIndex;
    }

    // Biểu tượng nằm dưới điểm p, kể cả khi đang mờ; null nếu p không nằm trong cột Thao tác. 
    private Action iconAt(Point p) {
        if (!isAt(p)) return null;
        Rectangle r = table.getCellRect(table.rowAtPoint(p), table.columnAtPoint(p), false);
        int index = (p.x - r.x) * actions.size() / Math.max(1, r.width);
        return actions.get(Math.min(Math.max(index, 0), actions.size() - 1));
    }

    // Hành động bấm được tại điểm p; null nếu không nằm trên biểu tượng nào hoặc biểu tượng đang mờ. 
    public Action actionAt(Point p) {
        Action a = iconAt(p);
        return a == null || isDisabled(table.rowAtPoint(p), a) ? null : a;
    }

    public String tooltipAt(Point p) {
        Action a = iconAt(p);
        if (a == null) return null;
        return isDisabled(table.rowAtPoint(p), a) ? disabledTooltips.getOrDefault(a, tooltips.get(a)) : tooltips.get(a);
    }

    public List<Action> getActions() { return new ArrayList<>(actions); }
}