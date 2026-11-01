package com.gymmanagement.controller;

import com.gymmanagement.dao.DataAccessException;
import com.gymmanagement.dao.UserAccountDao;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.MainView;

/**
 * Controller xử lý đăng nhập người dùng
 */
public class LoginController {
    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
        view.getBtnLogin().addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = view.getUsername();
        String password = view.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            view.setError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        UserSession session;
        try {
            session = new UserAccountDao().login(username, password);
        } catch (DataAccessException e) {
            // Lỗi kết nối database phải khác với sai mật khẩu
            view.setError("<html>" + e.getMessage() + "</html>");
            return;
        }

        if (session == null) {
            view.setError("Tên đăng nhập hoặc mật khẩu không chính xác!");
            return;
        }
        if (!session.hasAnyPermission()) {
            view.setError("Tài khoản chưa được cấp quyền. Vui lòng liên hệ Quản lý.");
            return;
        }
        if (session.isMustChangePassword() && !PasswordDialog.show(view, session.getUsername(), true)) {
            view.clearPassword();
            view.setError("Bạn cần đổi mật khẩu tạm để đăng nhập.");
            return;
        }

        view.setError(" ");
        view.dispose(); // Đóng màn hình đăng nhập

        MainView mainView = new MainView();
        new MainController(mainView, session);
        mainView.setVisible(true);
    }
}
