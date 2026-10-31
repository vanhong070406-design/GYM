package com.gymmanagement.controller;

import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.MainView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller xử lý đăng nhập người dùng
 */
public class LoginController {
    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
        initController();
    }

    private void initController() {
        view.getBtnLogin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = view.getUsername();
        String password = view.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            view.setError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Kiểm tra đăng nhập minh họa (admin / 123456 hoặc bất kỳ user nào trong giai đoạn UI)
        if ("admin".equalsIgnoreCase(username) && "123456".equals(password)) {
            view.setError(" ");
            view.dispose(); // Đóng màn hình đăng nhập

            // Khởi tạo MainView và MainController
            MainView mainView = new MainView();
            new MainController(mainView);
            mainView.setVisible(true);
        } else {
            view.setError("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
    }
}
