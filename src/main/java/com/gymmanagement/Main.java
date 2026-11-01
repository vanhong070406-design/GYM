package com.gymmanagement;

import com.gymmanagement.controller.LoginController;
import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;

/**
 * Điểm khởi chạy chương trình Quản lý phòng Gym
 */
public class Main {
    public static void main(String[] args) {
        // Thiết lập Look and Feel hiện đại theo hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Sử dụng mặc định nếu không load được
        }

        // Bật khử răng cưa cho văn bản (Anti-aliasing) trên màn hình Windows
        UIManager.put("Button.rollover", false);
        UITheme.installDefaults();
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Khởi chạy giao diện trên luồng sự kiện của Swing (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}
