package com.gymmanagement.controller;

import com.gymmanagement.dao.DataAccessException;
import java.awt.Component;
import java.util.function.Supplier;
import javax.swing.JOptionPane;

/** Chạy thao tác database từ giao diện; khi lỗi thì hiện thông báo thay vì làm hỏng luồng Swing. */
final class DbAction {
    private DbAction() {}

    /** Trả về true nếu thao tác thành công. */
    static boolean run(Component parent, Runnable action) {
        try {
            action.run();
            return true;
        } catch (DataAccessException e) {
            showError(parent, e);
            return false;
        }
    }

    static <T> T fetch(Component parent, Supplier<T> query, T fallback) {
        try {
            return query.get();
        } catch (DataAccessException e) {
            showError(parent, e);
            return fallback;
        }
    }

    private static void showError(Component parent, DataAccessException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
