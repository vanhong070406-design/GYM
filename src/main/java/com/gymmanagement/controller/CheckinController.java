package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Checkin;
import com.gymmanagement.model.Member;
import com.gymmanagement.model.Staff;
import com.gymmanagement.view.CheckinView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller điều phối chức năng Quản lý Check-in
 */
public class CheckinController {
    private final CheckinView view;
    private final SampleData data;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CheckinController(CheckinView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getCheckins());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getCheckins());
        });

        // Check-in mới
        view.getBtnCheckin().addActionListener(e -> showCheckinDialog());

        // Check-out
        view.getBtnCheckout().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một lượt check-in để check-out!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String checkinId = (String) view.getTableModel().getValueAt(row, 0);
            Checkin c = findCheckinById(checkinId);
            if (c != null) {
                if (!"--:--".equals(c.getCheckoutTime()) && !c.getCheckoutTime().isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Lượt check-in này đã được check-out trước đó (" + c.getCheckoutTime() + ")!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                c.setCheckoutTime(LocalDateTime.now().format(dtf));
                loadData(data.getCheckins());
                JOptionPane.showMessageDialog(view, "Check-out thành công cho hội viên: " + c.getMemberId(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Xóa
        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một lượt check-in cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String checkinId = (String) view.getTableModel().getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa lịch sử check-in: " + checkinId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getCheckins().removeIf(c -> c.getCheckinId().equalsIgnoreCase(checkinId));
                loadData(data.getCheckins());
                JOptionPane.showMessageDialog(view, "Xóa lượt check-in thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Checkin> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Checkin c : list) {
            String memberName = getMemberName(c.getMemberId());
            model.addRow(new Object[]{
                    c.getCheckinId(),
                    c.getMemberId(),
                    memberName,
                    c.getCheckinTime(),
                    c.getCheckoutTime(),
                    c.getReceptionistId()
            });
        }
    }

    private String getMemberName(String memberId) {
        for (Member m : data.getMembers()) {
            if (m.getMemberId().equalsIgnoreCase(memberId)) return m.getFullName();
        }
        return "N/A";
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getCheckins());
            return;
        }

        List<Checkin> filtered = data.getCheckins().stream()
                .filter(c -> c.getCheckinId().toLowerCase().contains(q)
                        || c.getMemberId().toLowerCase().contains(q)
                        || getMemberName(c.getMemberId()).toLowerCase().contains(q)
                        || c.getReceptionistId().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Checkin findCheckinById(String id) {
        for (Checkin c : data.getCheckins()) {
            if (c.getCheckinId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    private void showCheckinDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Check-in Hội Viên Mới", true);
        dialog.setSize(460, 360);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = UITheme.createTextField(15);
        txtId.setText("CK" + String.format("%03d", data.getCheckins().size() + 1));

        JComboBox<Member> cbMember = new JComboBox<>();
        for (Member m : data.getMembers()) cbMember.addItem(m);
        cbMember.setFont(UITheme.FONT_REGULAR);
        cbMember.setBackground(Color.WHITE);

        JTextField txtTime = UITheme.createTextField(15);
        txtTime.setText(LocalDateTime.now().format(dtf));

        JComboBox<Staff> cbReceptionist = new JComboBox<>();
        for (Staff st : data.getStaffList()) cbReceptionist.addItem(st);
        cbReceptionist.setFont(UITheme.FONT_REGULAR);
        cbReceptionist.setBackground(Color.WHITE);

        addFormRow(panel, gbc, 0, "Mã Check-in (*):", txtId);
        addFormRow(panel, gbc, 1, "Chọn hội viên (*):", cbMember);
        addFormRow(panel, gbc, 2, "Thời gian vào (*):", txtTime);
        addFormRow(panel, gbc, 3, "Lễ tân tiếp nhận (*):", cbReceptionist);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Xác nhận Check-in");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            Member selMember = (Member) cbMember.getSelectedItem();
            String time = txtTime.getText().trim();
            Staff selStaff = (Staff) cbReceptionist.getSelectedItem();

            if (id.isEmpty() || selMember == null || time.isEmpty() || selStaff == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (findCheckinById(id) != null) {
                JOptionPane.showMessageDialog(dialog, "Mã check-in đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Checkin newCheckin = new Checkin(id, time, "--:--", selMember.getMemberId(), selStaff.getStaffId());
            data.getCheckins().add(newCheckin);
            JOptionPane.showMessageDialog(dialog, "Check-in thành công cho: " + selMember.getFullName(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            loadData(data.getCheckins());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
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
