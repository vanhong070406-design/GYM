package com.gymmanagement.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Lớp tiện ích cung cấp giao diện chuẩn, tối giản, chuyên nghiệp và đồng bộ
 */
public class UITheme {
    // Bảng màu chuẩn
    public static final Color PRIMARY = new Color(30, 58, 138);       // Deep Navy (#1E3A8A)
    public static final Color PRIMARY_HOVER = new Color(37, 99, 235); // Accent Blue (#2563EB)
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255); // Blue-50 (#EFF6FF)
    
    public static final Color BG_MAIN = new Color(248, 250, 252);     // Slate-50 (#F8FAFC)
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(0, 59, 92);     // Slate-900 (#0F172A)
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);  // Slate-800 (#1E293B)
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235); // Blue-600
    
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate-900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate-500
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);  // Slate-100
    
    public static final Color BORDER = new Color(226, 232, 240);      // Slate-200
    public static final Color SUCCESS = new Color(21, 128, 61);       // Green-700
    public static final Color DANGER = new Color(220, 38, 38);        // Red-600
    public static final Color WARNING = new Color(180, 83, 9);        // Amber-700

    public static final Color SELECTION = new Color(191, 219, 254);   // Blue-200, nền vùng được chọn
    public static final Color SIDEBAR_TEXT_MUTED = new Color(148, 163, 184); // Slate-400
    private static final Color DISABLED_BG = BORDER;
    private static final Color DISABLED_TEXT = new Color(71, 85, 105); // Slate-600
    private static final int BUTTON_ARC = 10;

    // Phông chữ chuẩn (Segoe UI / Arial)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BIG_STAT = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Áp dụng màu chung cho ô nhập, combo và danh sách. Gọi sau khi đã đặt Look and Feel.
     */
    public static void installDefaults() {
        for (String key : new String[]{"TextField", "PasswordField", "TextArea", "FormattedTextField", "ComboBox", "List"}) {
            UIManager.put(key + ".foreground", TEXT_DARK);
            UIManager.put(key + ".selectionBackground", SELECTION);
            UIManager.put(key + ".selectionForeground", TEXT_DARK);
        }
        UIManager.put("ComboBox.disabledForeground", DISABLED_TEXT);
        UIManager.put("TextField.inactiveForeground", TEXT_MUTED);
        UIManager.put("TextField.disabledForeground", TEXT_MUTED);
        UIManager.put("PasswordField.inactiveForeground", TEXT_MUTED);
        UIManager.put("PasswordField.disabledForeground", TEXT_MUTED);
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.okButtonText", "Đồng ý");
    }

    /**
     * Tự vẽ nút theo màu của theme. Giao diện Windows mặc định bỏ qua setBackground của JButton,
     * làm chữ trắng nằm trên nền trắng, nên màu nền không được phó mặc cho hệ điều hành.
     */
    private static final class ThemeButtonUI extends BasicButtonUI {
        private final Color normal;
        private final Color hover;
        private final Color pressed;
        private final Color outline;

        ThemeButtonUI(Color normal, Color hover, Color pressed, Color outline) {
            this.normal = normal;
            this.hover = hover;
            this.pressed = pressed;
            this.outline = outline;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Color fill = !b.isEnabled() ? DISABLED_BG
                    : m.isArmed() && m.isPressed() ? pressed
                    : m.isRollover() ? hover : normal;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), BUTTON_ARC, BUTTON_ARC);
            if (outline != null && b.isEnabled()) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, BUTTON_ARC, BUTTON_ARC);
            }
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            FontMetrics fm = b.getFontMetrics(b.getFont());
            g.setFont(b.getFont());
            g.setColor(b.isEnabled() ? b.getForeground() : DISABLED_TEXT);
            BasicGraphicsUtils.drawStringUnderlineCharAt(b, (Graphics2D) g, text, -1,
                    textRect.x, textRect.y + fm.getAscent());
        }

        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            // Màu nhấn đã được vẽ trong paint()
        }
    }

    /**
     * Gán màu cho một nút ở các trạng thái thường, rê chuột và đang nhấn.
     */
    public static void styleButton(AbstractButton btn, Color text, Color normal, Color hover, Color pressed, Color outline) {
        btn.setUI(new ThemeButtonUI(normal, hover, pressed, outline));
        btn.setForeground(text);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static JButton createButton(String text, Color fg, Color normal, Color hover, Color pressed, Color outline) {
        JButton btn = new JButton(text);
        styleButton(btn, fg, normal, hover, pressed, outline);
        btn.setFont(FONT_BOLD);
        btn.setBorder(new EmptyBorder(9, 17, 9, 17));
        return btn;
    }

    /**
     * Tạo nút bấm chính (Primary Button)
     */
    public static JButton createPrimaryButton(String text) {
        return createButton(text, Color.WHITE, PRIMARY, PRIMARY_HOVER, new Color(23, 37, 84), null);
    }

    /**
     * Tạo nút bấm phụ (Secondary Button)
     */
    public static JButton createSecondaryButton(String text) {
        return createButton(text, TEXT_DARK, Color.WHITE, new Color(241, 245, 249), BORDER, new Color(148, 163, 184));
    }

    /**
     * Tạo nút bấm xóa/nguy hiểm (Danger Button)
     */
    public static JButton createDangerButton(String text) {
        return createButton(text, Color.WHITE, DANGER, new Color(185, 28, 28), new Color(153, 27, 27), null);
    }

    /**
     * Đặt màu cho mục menu trên sidebar theo trạng thái đang chọn.
     */
    public static void styleSidebarButton(AbstractButton btn, boolean active) {
        if (active) {
            styleButton(btn, Color.WHITE, SIDEBAR_ACTIVE, SIDEBAR_ACTIVE, SIDEBAR_ACTIVE, null);
            btn.setFont(FONT_BOLD);
        } else {
            styleButton(btn, TEXT_LIGHT, BG_SIDEBAR, SIDEBAR_HOVER, new Color(51, 65, 85), null);
            btn.setFont(FONT_REGULAR);
        }
    }

    /**
     * Định dạng TextField chuẩn
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(TEXT_DARK);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * Định dạng ô mật khẩu giống các ô nhập khác
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_DARK);
        pf.setCaretColor(TEXT_DARK);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    /**
     * Xếp nhóm lọc và nhóm thao tác trên một hàng; khi cửa sổ hẹp không đủ chỗ thì
     * tách thành hai hàng để hai nhóm không đè lên nhau.
     */
    public static void arrangeToolbar(JPanel toolbar, JPanel filters, JPanel actions) {
        toolbar.setLayout(new BorderLayout(16, 10));
        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        toolbar.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Insets in = toolbar.getInsets();
                int available = toolbar.getWidth() - in.left - in.right;
                boolean twoRows = filters.getPreferredSize().width + 16 + actions.getPreferredSize().width > available;
                BorderLayout layout = (BorderLayout) toolbar.getLayout();
                Object wanted = twoRows ? BorderLayout.NORTH : BorderLayout.WEST;
                if (wanted.equals(layout.getConstraints(filters))) {
                    return;
                }
                toolbar.add(filters, wanted);
                toolbar.add(actions, twoRows ? BorderLayout.SOUTH : BorderLayout.EAST);
                toolbar.revalidate();
            }
        });
    }

    /**
     * Áp dụng phong cách chuẩn cho JTable
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Center / Left align cell renderer with padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_DARK);
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Tạo Card Panel với nền trắng và viền xám nhẹ
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }
}
