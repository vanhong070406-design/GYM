package com.gymmanagement.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Giao diện Đăng nhập hệ thống
 * Thiết kế tối giản, chuyên nghiệp, hỗ trợ phím Enter để đăng nhập
 */
public class LoginView extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginView() {
        setTitle("Gym Management System - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header Panel (Logo & Tiêu đề)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.BG_MAIN);

        JLabel lblBadge = new JLabel("NUTRI BOOST");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBadge.setForeground(UITheme.PRIMARY_HOVER);
        lblBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ PHÒNG GYM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Đăng nhập để truy cập trang quản trị");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblBadge);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(lblSubtitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Form Card Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        // Tên đăng nhập
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_DARK);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = UITheme.createTextField(20);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setText("admin");

        // Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_DARK);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setText("123456");

        // Thông báo lỗi
        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút Đăng nhập
        btnLogin = UITheme.createPrimaryButton("Đăng nhập vào hệ thống");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardPanel.add(lblUser);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        cardPanel.add(txtUsername);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        cardPanel.add(lblPass);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        cardPanel.add(txtPassword);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        cardPanel.add(lblError);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        cardPanel.add(btnLogin);

        // Footer gợi ý
        JLabel lblHint = new JLabel("Tài khoản mặc định: admin | Mật khẩu: 123456");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        lblHint.setHorizontalAlignment(SwingConstants.CENTER);
        lblHint.setBorder(new EmptyBorder(16, 0, 0, 0));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(lblHint, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        getRootPane().setDefaultButton(btnLogin);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public void setError(String error) {
        lblError.setText(error != null ? error : " ");
    }
}
