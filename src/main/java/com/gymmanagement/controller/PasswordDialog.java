package com.gymmanagement.controller;

import com.gymmanagement.dao.UserAccountDao;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Hộp thoại người dùng tự đổi mật khẩu của mình. */
final class PasswordDialog {
    static final int MIN_LENGTH = 6;

    private PasswordDialog() {}

    /** Trả về true nếu đã đổi thành công. */
    static boolean show(Window owner, String username, boolean forced) {
        JDialog dialog = new JDialog(owner, "Đổi mật khẩu", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 320);
        dialog.setLocationRelativeTo(owner);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField txtCurrent = UITheme.createPasswordField(15);
        JPasswordField txtNew = UITheme.createPasswordField(15);
        JPasswordField txtConfirm = UITheme.createPasswordField(15);

        int row = 0;
        if (forced) {
            JLabel note = new JLabel("Bạn cần đổi mật khẩu tạm trước khi tiếp tục.");
            note.setFont(UITheme.FONT_REGULAR);
            note.setForeground(UITheme.DANGER);
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            panel.add(note, gbc);
        }
        addRow(panel, gbc, row++, "Mật khẩu hiện tại (*):", txtCurrent);
        addRow(panel, gbc, row++, "Mật khẩu mới (*):", txtNew);
        addRow(panel, gbc, row++, "Nhập lại mật khẩu mới (*):", txtConfirm);

        boolean[] changed = {false};
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu mật khẩu");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String current = new String(txtCurrent.getPassword());
            String next = new String(txtNew.getPassword());
            if (next.length() < MIN_LENGTH) {
                warn(dialog, "Mật khẩu mới phải có ít nhất " + MIN_LENGTH + " ký tự!");
            } else if (!next.equals(new String(txtConfirm.getPassword()))) {
                warn(dialog, "Mật khẩu nhập lại không khớp!");
            } else if (next.equals(current)) {
                warn(dialog, "Mật khẩu mới phải khác mật khẩu hiện tại!");
            } else {
                boolean[] correct = {false};
                if (!DbAction.run(dialog, () -> correct[0] = new UserAccountDao().changePassword(username, current, next))) return;
                if (!correct[0]) {
                    warn(dialog, "Mật khẩu hiện tại không đúng!");
                    return;
                }
                changed[0] = true;
                JOptionPane.showMessageDialog(dialog, "Đổi mật khẩu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
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
        return changed[0];
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
