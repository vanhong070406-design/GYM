package com.gymmanagement.controller;

import com.gymmanagement.dao.MemberDao;
import com.gymmanagement.dao.PackageDao;
import com.gymmanagement.dao.SubscriptionDao;
import com.gymmanagement.model.Member;
import com.gymmanagement.model.PackageModel;
import com.gymmanagement.model.Subscription;
import com.gymmanagement.view.SubscriptionView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối chức năng Đăng ký gói tập
 */
public class SubscriptionController {
    private final SubscriptionView view;
    private final SubscriptionDao dao = new SubscriptionDao();
    private final MemberDao memberDao = new MemberDao();
    private final PackageDao packageDao = new PackageDao();

    public SubscriptionController(SubscriptionView view) {
        this.view = view;
        initListeners();
        reload();
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleFilter());
        view.getTxtSearch().addActionListener(e -> handleFilter());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            view.getCbFilterStatus().setSelectedIndex(0);
            loadData(fetchAll());
        });

        view.getBtnAdd().addActionListener(e -> showSubscriptionDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng đăng ký để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String subId = (String) view.getTableModel().getValueAt(row, 0);
            Subscription sub = findSubscriptionById(subId);
            if (sub != null) {
                showSubscriptionDialog(sub);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn thông tin đăng ký cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String subId = (String) view.getTableModel().getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa đăng ký gói: " + subId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(subId))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<Subscription> fetchAll() {
        return DbAction.fetch(view, () -> {
            dao.expireOverdue();
            return dao.findAll();
        }, List.of());
    }

    public void reload() {
        handleFilter();
    }

    public void loadData(List<Subscription> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Subscription s : list) {
            model.addRow(new Object[]{
                    s.getSubscriptionId(),
                    s.getMemberId(),
                    s.getMemberName(),
                    s.getPackageId(),
                    Formats.text(s.getStartDate()),
                    Formats.text(s.getEndDate()),
                    s.getStatus()
            });
        }
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String statusFilter = (String) view.getCbFilterStatus().getSelectedItem();

        List<Subscription> filtered = fetchAll().stream()
                .filter(s -> {
                    boolean matchQuery = q.isEmpty()
                            || s.getSubscriptionId().toLowerCase().contains(q)
                            || s.getMemberId().toLowerCase().contains(q)
                            || s.getPackageId().toLowerCase().contains(q)
                            || s.getMemberName().toLowerCase().contains(q);

                    boolean matchStatus = "Tất cả".equals(statusFilter)
                            || s.getStatus().equalsIgnoreCase(statusFilter);

                    return matchQuery && matchStatus;
                })
                .toList();

        loadData(filtered);
    }

    private Subscription findSubscriptionById(String id) {
        for (Subscription s : fetchAll()) {
            if (s.getSubscriptionId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void showSubscriptionDialog(Subscription subToEdit) {
        boolean isEdit = (subToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa đăng ký gói tập" : "Đăng ký gói tập mới", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = UITheme.createTextField(15);
        JComboBox<Member> cbMember = new JComboBox<>();
        for (Member m : DbAction.fetch(view, memberDao::findAll, List.<Member>of())) cbMember.addItem(m);
        cbMember.setFont(UITheme.FONT_REGULAR);
        cbMember.setBackground(Color.WHITE);

        JComboBox<PackageModel> cbPackage = new JComboBox<>();
        for (PackageModel p : DbAction.fetch(view, packageDao::findAll, List.<PackageModel>of())) cbPackage.addItem(p);
        cbPackage.setFont(UITheme.FONT_REGULAR);
        cbPackage.setBackground(Color.WHITE);

        JTextField txtStartDate = UITheme.createTextField(15);
        JTextField txtEndDate = UITheme.createTextField(15);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Hết hạn", "Tạm dừng"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(subToEdit.getSubscriptionId());
            txtId.setEditable(false);
            for (int i = 0; i < cbMember.getItemCount(); i++) {
                if (cbMember.getItemAt(i).getMemberId().equalsIgnoreCase(subToEdit.getMemberId())) {
                    cbMember.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbPackage.getItemCount(); i++) {
                if (cbPackage.getItemAt(i).getPackageId().equalsIgnoreCase(subToEdit.getPackageId())) {
                    cbPackage.setSelectedIndex(i);
                    break;
                }
            }
            txtStartDate.setText(Formats.text(subToEdit.getStartDate()));
            txtEndDate.setText(Formats.text(subToEdit.getEndDate()));
            cbStatus.setSelectedItem(subToEdit.getStatus());
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            LocalDate now = LocalDate.now();
            txtStartDate.setText(now.toString());
            txtEndDate.setText(now.plusDays(30).toString());
        }

        // Tự động cập nhật EndDate khi chọn gói tập khác
        cbPackage.addActionListener(e -> {
            if (!isEdit && cbPackage.getSelectedItem() != null) {
                PackageModel selected = (PackageModel) cbPackage.getSelectedItem();
                try {
                    LocalDate start = LocalDate.parse(txtStartDate.getText().trim());
                    txtEndDate.setText(start.plusDays(selected.getDuration()).toString());
                } catch (Exception ignored) {}
            }
        });

        addFormRow(panel, gbc, 0, "Mã đăng ký (*):", txtId);
        addFormRow(panel, gbc, 1, "Chọn hội viên (*):", cbMember);
        addFormRow(panel, gbc, 2, "Chọn gói tập (*):", cbPackage);
        addFormRow(panel, gbc, 3, "Ngày bắt đầu (*):", txtStartDate);
        addFormRow(panel, gbc, 4, "Ngày kết thúc (*):", txtEndDate);
        addFormRow(panel, gbc, 5, "Trạng thái:", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            Member selMember = (Member) cbMember.getSelectedItem();
            PackageModel selPkg = (PackageModel) cbPackage.getSelectedItem();
            String startText = txtStartDate.getText().trim();
            String endText = txtEndDate.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (id.isEmpty() || selMember == null || selPkg == null || startText.isEmpty() || endText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin đăng ký!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate;
            LocalDate endDate;
            try {
                startDate = Formats.parseDate(startText);
                endDate = Formats.parseDate(endText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                subToEdit.setMemberId(selMember.getMemberId());
                subToEdit.setPackageId(selPkg.getPackageId());
                subToEdit.setStartDate(startDate);
                subToEdit.setEndDate(endDate);
                subToEdit.setStatus(status);
                if (!DbAction.run(dialog, () -> dao.update(subToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Subscription newSub = new Subscription(id, startDate, endDate, status, selMember.getMemberId(), selPkg.getPackageId());
                if (!DbAction.run(dialog, () -> dao.insert(newSub))) return;
                JOptionPane.showMessageDialog(dialog, "Đăng ký gói tập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(fetchAll());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 6;
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
