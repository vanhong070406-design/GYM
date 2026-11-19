package com.gymmanagement.controller;

import com.gymmanagement.dao.UserAccountDao;
import com.gymmanagement.model.Staff;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


// Hộp thoại Quản lý cấp tài khoản cho nhân viên 
// Nhân viên chưa có tài khoản: nhập tên đăng nhập và mật khẩu tạm
// Nhân viên đã có tài khoản: giữ nguyên tên đăng nhập, chỉ đặt lại mật khẩu

final class AccountDialog {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._]{3,50}");

    private AccountDialog() {}

    // existing = null nếu nhân viên chưa có tài khoản. Trả về true nếu đã lưu thành công.
    static boolean show(Window owner, Staff staff, UserAccountDao.StaffAccount existing) {
        UserAccountDao dao = new UserAccountDao();
        boolean isNew = existing == null;

        JDialog dialog = new JDialog(owner, isNew ? "Cấp tài khoản" : "Đặt lại mật khẩu", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 400);
        dialog.setLocationRelativeTo(owner);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtUser = UITheme.createTextField(15);
        JPasswordField txtPass = UITheme.createPasswordField(15);
        JPasswordField txtConfirm = UITheme.createPasswordField(15);
        JCheckBox chkMustChange = new JCheckBox("Bắt đổi mật khẩu khi đăng nhập lần đầu");
        chkMustChange.setFont(UITheme.FONT_REGULAR);
        chkMustChange.setOpaque(false);
        chkMustChange.setSelected(true);

        JLabel lblStaff = new JLabel(staff.getStaffId() + " – " + staff.getFullName() + " (" + staff.getPositionName() + ")");
        lblStaff.setFont(UITheme.FONT_REGULAR);
        lblStaff.setForeground(UITheme.TEXT_DARK);

        if (!isNew) {
            txtUser.setText(existing.username());
            txtUser.setEditable(false);
        }

        int row = 0;
        if (!isNew) {
            JLabel note = new JLabel("Nhân viên này đã có tài khoản. Nhập mật khẩu mới để đặt lại.");
            note.setFont(UITheme.FONT_REGULAR);
            note.setForeground(UITheme.WARNING);
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            panel.add(note, gbc);
        }
        addRow(panel, gbc, row++, "Nhân viên:", lblStaff);
        addRow(panel, gbc, row++, "Tên đăng nhập (*):", txtUser);
        addRow(panel, gbc, row++, isNew ? "Mật khẩu tạm (*):" : "Mật khẩu mới (*):", txtPass);
        addRow(panel, gbc, row++, "Nhập lại mật khẩu (*):", txtConfirm);
        addRow(panel, gbc, row++, "", chkMustChange);

        boolean[] saved = {false};
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton(isNew ? "Cấp tài khoản" : "Lưu mật khẩu");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
            boolean mustChange = chkMustChange.isSelected();

            if (isNew && !USERNAME.matcher(username).matches()) {
                warn(dialog, "Tên đăng nhập gồm 3–50 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới!");
                return;
            }
            if (password.length() < PasswordDialog.MIN_LENGTH) {
                warn(dialog, "Mật khẩu phải có ít nhất " + PasswordDialog.MIN_LENGTH + " ký tự!");
                return;
            }
            if (!password.equals(new String(txtConfirm.getPassword()))) {
                warn(dialog, "Mật khẩu nhập lại không khớp!");
                return;
            }
            if (isNew) {
                boolean[] taken = {false};
                if (!DbAction.run(dialog, () -> taken[0] = dao.usernameExists(username))) return;
                if (taken[0]) {
                    warn(dialog, "Tên đăng nhập \"" + username + "\" đã có người dùng, hãy chọn tên khác!");
                    return;
                }
            }

            boolean ok = DbAction.run(dialog, () -> {
                if (isNew) dao.createStaffAccount(staff.getStaffId(), username, password, mustChange);
                else dao.resetStaffPassword(staff.getStaffId(), password, mustChange);
            });
            if (!ok) return;

            saved[0] = true;
            JOptionPane.showMessageDialog(dialog,
                    isNew ? "Đã cấp tài khoản \"" + username + "\" cho " + staff.getFullName() + "."
                          : "Đã đặt lại mật khẩu cho tài khoản \"" + existing.username() + "\".",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
        return saved[0];
    }

    private static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
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