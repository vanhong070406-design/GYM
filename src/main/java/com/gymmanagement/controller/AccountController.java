package com.gymmanagement.controller;

import com.gymmanagement.dao.UserAccountDao;
import com.gymmanagement.model.AccountOwner;
import com.gymmanagement.model.UserAccount;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.AccountView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối Quản lý tài khoản (chỉ Quản lý có quyền)
 */
public class AccountController {
    private final AccountView view;
    private final UserSession session;
    private final UserAccountDao dao = new UserAccountDao();

    public AccountController(AccountView view, UserSession session) {
        this.view = view;
        this.session = session;
        initListeners();
        reload();
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> reload());
        view.getTxtSearch().addActionListener(e -> reload());
        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            reload();
        });
        view.getBtnAdd().addActionListener(e -> showCreateDialog());
        view.getBtnReset().addActionListener(e -> {
            String username = selectedUsername();
            if (username != null) showResetDialog(username);
        });
        view.getBtnDelete().addActionListener(e -> handleDelete());
    }

    public void reload() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (UserAccount a : DbAction.fetch(view, dao::findAll, List.<UserAccount>of())) {
            if (q.isEmpty() || a.getUsername().toLowerCase().contains(q) || a.getOwnerId().toLowerCase().contains(q)
                    || a.getOwnerName().toLowerCase().contains(q) || a.getPositionName().toLowerCase().contains(q)) {
                model.addRow(new Object[]{a.getUsername(), a.getOwnerId(), a.getOwnerName(), a.getPositionName(),
                        a.isMustChangePassword() ? "Có" : "Không"});
            }
        }
    }

    private String selectedUsername() {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một tài khoản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) view.getTableModel().getValueAt(row, 0);
    }

    private void handleDelete() {
        String username = selectedUsername();
        if (username == null) return;
        if (username.equalsIgnoreCase(session.getUsername())) {
            JOptionPane.showMessageDialog(view, "Không thể xóa tài khoản đang đăng nhập!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa tài khoản: " + username + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(username))) return;
            reload();
            JOptionPane.showMessageDialog(view, "Xóa tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCreateDialog() {
        List<AccountOwner> owners = DbAction.fetch(view, dao::findOwnersWithoutAccount, List.<AccountOwner>of());
        if (owners.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tất cả nhân viên và HLV đều đã có tài khoản.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Tạo tài khoản mới", true);
        JPanel panel = newFormPanel(dialog, 300);
        GridBagConstraints gbc = newConstraints();

        JComboBox<AccountOwner> cbOwner = new JComboBox<>();
        for (AccountOwner o : owners) cbOwner.addItem(o);
        cbOwner.setFont(UITheme.FONT_REGULAR);
        cbOwner.setBackground(Color.WHITE);
        JTextField txtUsername = UITheme.createTextField(15);
        JPasswordField txtPassword = UITheme.createPasswordField(15);

        addFormRow(panel, gbc, 0, "Nhân viên / HLV (*):", cbOwner);
        addFormRow(panel, gbc, 1, "Tên đăng nhập (*):", txtUsername);
        addFormRow(panel, gbc, 2, "Mật khẩu tạm (*):", txtPassword);

        addButtons(dialog, panel, gbc, 3, "Lưu tài khoản", () -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            AccountOwner owner = (AccountOwner) cbOwner.getSelectedItem();
            if (username.isEmpty() || !validPassword(dialog, password)) {
                if (username.isEmpty()) warn(dialog, "Vui lòng nhập tên đăng nhập!");
                return;
            }
            if (!DbAction.run(dialog, () -> dao.create(username, password, owner))) return;
            JOptionPane.showMessageDialog(dialog, "Tạo tài khoản thành công! Người dùng sẽ phải đổi mật khẩu khi đăng nhập lần đầu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            reload();
            dialog.dispose();
        });
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showResetDialog(String username) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Đặt lại mật khẩu: " + username, true);
        JPanel panel = newFormPanel(dialog, 220);
        GridBagConstraints gbc = newConstraints();

        JPasswordField txtPassword = UITheme.createPasswordField(15);
        addFormRow(panel, gbc, 0, "Mật khẩu tạm mới (*):", txtPassword);

        addButtons(dialog, panel, gbc, 1, "Lưu mật khẩu", () -> {
            String password = new String(txtPassword.getPassword());
            if (!validPassword(dialog, password)) return;
            if (!DbAction.run(dialog, () -> dao.resetPassword(username, password))) return;
            JOptionPane.showMessageDialog(dialog, "Đặt lại mật khẩu thành công! Người dùng sẽ phải đổi mật khẩu khi đăng nhập lần sau.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            reload();
            dialog.dispose();
        });
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private boolean validPassword(Component parent, String password) {
        if (password.length() >= PasswordDialog.MIN_LENGTH) return true;
        warn(parent, "Mật khẩu phải có ít nhất " + PasswordDialog.MIN_LENGTH + " ký tự!");
        return false;
    }

    private void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private JPanel newFormPanel(JDialog dialog, int height) {
        dialog.setSize(480, height);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        return panel;
    }

    private GridBagConstraints newConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addButtons(JDialog dialog, JPanel panel, GridBagConstraints gbc, int row, String saveText, Runnable onSave) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton(saveText);
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> onSave.run());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(btnPanel, gbc);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }
}
