package com.gymmanagement.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    public static final Color BG_SIDEBAR = new Color(15, 23, 42);     // Slate-900 (#0F172A)
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);  // Slate-800 (#1E293B)
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235); // Blue-600
    
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate-900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate-500
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);  // Slate-100
    
    public static final Color BORDER = new Color(226, 232, 240);      // Slate-200
    public static final Color SUCCESS = new Color(22, 163, 74);       // Green-600
    public static final Color DANGER = new Color(220, 38, 38);        // Red-600
    public static final Color WARNING = new Color(217, 119, 6);       // Amber-600

    // Phông chữ chuẩn (Segoe UI / Arial)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BIG_STAT = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Tạo nút bấm chính (Primary Button)
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            
            @SuppressWarnings("override")
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    /**
     * Tạo nút bấm phụ (Secondary Button)
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(241, 245, 249));
            
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    /**
     * Tạo nút bấm xóa/nguy hiểm (Danger Button)
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(DANGER, 1),
                new EmptyBorder(8, 16, 8, 16)
            
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(185, 28, 28));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DANGER);
            }
        });
        return btn;
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
     * Áp dụng phong cách chuẩn cho JTable
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY);
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
