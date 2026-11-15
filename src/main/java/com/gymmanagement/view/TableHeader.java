package com.gymmanagement.view;

import java.awt.*;
import javax.swing.*;

/**
 * Đầu bảng dùng chung: tiêu đề kèm số dòng đang hiện bên trái, nút làm mới dạng biểu tượng bên phải.
 */
public class TableHeader extends JPanel {
    private final String title;
    private final JLabel lblTitle;
    private final JButton btnRefresh;

    public TableHeader(String title, String refreshTooltip) {
        super(new BorderLayout());
        setOpaque(false);
        this.title = title;

        lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        btnRefresh = new JButton(Icons.refresh(UITheme.TEXT_MUTED));
        UITheme.styleButton(btnRefresh, UITheme.TEXT_MUTED, Color.WHITE, new Color(241, 245, 249), UITheme.BORDER, UITheme.BORDER);
        btnRefresh.setPreferredSize(new Dimension(34, 30));
        btnRefresh.setToolTipText(refreshTooltip);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(lblTitle, BorderLayout.WEST);
        add(btnRefresh, BorderLayout.EAST);
    }

    /** "Tiêu đề · n". */
    public void setCount(int shown) {
        lblTitle.setText(title + " · " + shown);
    }

    /** "Tiêu đề · n · ghi chú", ghi chú chữ thường màu xanh lá; note rỗng thì như {@link #setCount(int)}. */
    public void setCount(int shown, String note) {
        if (note == null || note.isEmpty()) {
            setCount(shown);
            return;
        }
        setCount(shown, new Note(note, UITheme.SUCCESS));
    }

    /** Một ghi chú nhỏ sau số lượng, mỗi ghi chú một màu. */
    public record Note(String text, Color color) {}

    /** "Tiêu đề · n · ghi chú 1 · ghi chú 2…"; ghi chú rỗng bị bỏ qua. */
    public void setCount(int shown, Note... notes) {
        StringBuilder html = new StringBuilder("<html>").append(title).append(" · ").append(shown);
        boolean any = false;
        for (Note n : notes) {
            if (n == null || n.text() == null || n.text().isEmpty()) continue;
            html.append(" <span style='font-weight:normal;color:").append(hex(n.color())).append("'>· ")
                    .append(n.text()).append("</span>");
            any = true;
        }
        if (!any) {
            setCount(shown);
            return;
        }
        lblTitle.setText(html.append("</html>").toString());
    }

    private static String hex(Color c) {
        return String.format("#%06x", c.getRGB() & 0xFFFFFF);
    }

    public JButton getBtnRefresh() { return btnRefresh; }
}
