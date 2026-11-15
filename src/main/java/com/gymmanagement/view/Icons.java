package com.gymmanagement.view;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Biểu tượng nhỏ vẽ bằng Java2D, hiển thị giống nhau trên macOS và Windows
 * (không phụ thuộc phông chữ hay emoji của hệ điều hành).
 */
public final class Icons {
    private static final int SIZE = 16;

    private Icons() {}

    @FunctionalInterface
    private interface Painter {
        void paint(Graphics2D g);
    }

    private static Icon create(Color color, Painter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                painter.paint(g2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return SIZE; }

            @Override
            public int getIconHeight() { return SIZE; }
        };
    }

    /** Bút chì: chỉnh sửa. */
    public static Icon edit(Color color) {
        return create(color, g -> {
            Path2D body = new Path2D.Double();
            body.moveTo(2.5, 13.5);
            body.lineTo(3.2, 10.6);
            body.lineTo(10.8, 3.0);
            body.lineTo(13.0, 5.2);
            body.lineTo(5.4, 12.8);
            body.closePath();
            g.draw(body);
            g.draw(new java.awt.geom.Line2D.Double(9.4, 4.4, 11.6, 6.6));
        });
    }

    /** Thùng rác: xóa. */
    public static Icon trash(Color color) {
        return create(color, g -> {
            g.draw(new java.awt.geom.Line2D.Double(2.5, 4.5, 13.5, 4.5));
            Path2D handle = new Path2D.Double();
            handle.moveTo(6.0, 4.5);
            handle.lineTo(6.0, 2.5);
            handle.lineTo(10.0, 2.5);
            handle.lineTo(10.0, 4.5);
            g.draw(handle);
            Path2D bin = new Path2D.Double();
            bin.moveTo(3.8, 4.5);
            bin.lineTo(4.6, 13.8);
            bin.lineTo(11.4, 13.8);
            bin.lineTo(12.2, 4.5);
            g.draw(bin);
            g.draw(new java.awt.geom.Line2D.Double(6.8, 7.0, 6.8, 11.5));
            g.draw(new java.awt.geom.Line2D.Double(9.2, 7.0, 9.2, 11.5));
        });
    }

    /** Ổ khóa: không đổi được nữa. */
    public static Icon lock(Color color) {
        return create(color, g -> {
            g.draw(new java.awt.geom.RoundRectangle2D.Double(3.5, 7.5, 9, 6.5, 2, 2));
            g.draw(new Arc2D.Double(5.2, 3.0, 5.6, 7.0, 0, 180, Arc2D.OPEN));
            g.draw(new java.awt.geom.Line2D.Double(5.2, 6.5, 5.2, 7.5));
            g.draw(new java.awt.geom.Line2D.Double(10.8, 6.5, 10.8, 7.5));
        });
    }

    /** Con mắt: xem chi tiết. */
    public static Icon eye(Color color) {
        return create(color, g -> {
            Path2D outline = new Path2D.Double();
            outline.moveTo(1.5, 8.0);
            outline.quadTo(8.0, 1.0, 14.5, 8.0);
            outline.quadTo(8.0, 15.0, 1.5, 8.0);
            outline.closePath();
            g.draw(outline);
            g.draw(new java.awt.geom.Ellipse2D.Double(5.6, 5.6, 4.8, 4.8));
        });
    }

    /** Cửa và mũi tên đi ra: check-out. */
    public static Icon logout(Color color) {
        return create(color, g -> {
            Path2D door = new Path2D.Double();
            door.moveTo(7.0, 2.5);
            door.lineTo(3.0, 2.5);
            door.lineTo(3.0, 13.5);
            door.lineTo(7.0, 13.5);
            g.draw(door);
            g.draw(new java.awt.geom.Line2D.Double(6.5, 8.0, 13.5, 8.0));
            Path2D head = new Path2D.Double();
            head.moveTo(10.5, 5.0);
            head.lineTo(13.5, 8.0);
            head.lineTo(10.5, 11.0);
            g.draw(head);
        });
    }

    /** Lịch có dấu cộng: gia hạn. */
    public static Icon calendarPlus(Color color) {
        return create(color, g -> {
            g.draw(new java.awt.geom.RoundRectangle2D.Double(2.0, 3.0, 12.0, 11.0, 2.5, 2.5));
            g.draw(new java.awt.geom.Line2D.Double(2.0, 6.5, 14.0, 6.5));
            g.draw(new java.awt.geom.Line2D.Double(5.0, 1.5, 5.0, 4.0));
            g.draw(new java.awt.geom.Line2D.Double(11.0, 1.5, 11.0, 4.0));
            g.draw(new java.awt.geom.Line2D.Double(8.0, 8.3, 8.0, 12.3));
            g.draw(new java.awt.geom.Line2D.Double(6.0, 10.3, 10.0, 10.3));
        });
    }

    /** Tờ tiền: thu tiền. */
    public static Icon cash(Color color) {
        return create(color, g -> {
            g.draw(new java.awt.geom.RoundRectangle2D.Double(1.5, 4.0, 13.0, 8.0, 2.0, 2.0));
            g.draw(new java.awt.geom.Ellipse2D.Double(6.2, 6.2, 3.6, 3.6));
            g.draw(new java.awt.geom.Line2D.Double(3.8, 8.0, 4.2, 8.0));
            g.draw(new java.awt.geom.Line2D.Double(11.8, 8.0, 12.2, 8.0));
        });
    }

    /** Mũi tên xoay vòng: làm mới. */
    public static Icon refresh(Color color) {
        return create(color, g -> {
            g.draw(new Arc2D.Double(2.5, 2.5, 11, 11, 70, 280, Arc2D.OPEN));
            // Đầu mũi tên ở cuối cung (góc 350°), chỉ theo chiều ngược kim đồng hồ
            double a = Math.toRadians(350);
            double px = 8 + 5.5 * Math.cos(a), py = 8 - 5.5 * Math.sin(a);
            double dx = -Math.sin(a), dy = -Math.cos(a);
            Path2D head = new Path2D.Double();
            head.moveTo(px + 3.0 * dx, py + 3.0 * dy);
            head.lineTo(px + 2.4 * dy, py - 2.4 * dx);
            head.lineTo(px - 2.4 * dy, py + 2.4 * dx);
            head.closePath();
            g.fill(head);
        });
    }
}
