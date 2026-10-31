package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Staff;
import com.gymmanagement.view.StaffView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối Quản lý nhân viên
 */
public class StaffController {
    private final StaffView view;
    private final SampleData data;

    public StaffController(StaffView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getStaffList());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getStaffList());
        });

        view.getBtnAdd().addActionListener(e -> showStaffDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một nhân viên để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String stId = (String) view.getTableModel().getValueAt(row, 0);
            Staff staff = findStaffById(stId);
            if (staff != null) {
                showStaffDialog(staff);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một nhân viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String stId = (String) view.getTableModel().getValueAt(row, 0);
            String stName = (String) view.getTableModel().getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa nhân viên: " + stName + " (" + stId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getStaffList().removeIf(s -> s.getStaffId().equalsIgnoreCase(stId));
                loadData(data.getStaffList());
                JOptionPane.showMessageDialog(view, "Xóa nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Staff> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Staff s : list) {
            model.addRow(new Object[]{
                    s.getStaffId(),
                    s.getFullName(),
                    s.getPhone(),
                    String.format("%,.0f", s.getSalary()),
                    s.getShift(),
                    s.getHireDate(),
                    s.getManagerId()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getStaffList());
            return;
        }

        List<Staff> filtered = data.getStaffList().stream()
                .filter(s -> s.getStaffId().toLowerCase().contains(q)
                        || s.getFullName().toLowerCase().contains(q)
                        || s.getPhone().contains(q)
                        || s.getShift().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Staff findStaffById(String id) {
        for (Staff s : data.getStaffList()) {
            if (s.getStaffId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void showStaffDialog(Staff staffToEdit) {
        boolean isEdit = (staffToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa nhân viên" : "Thêm nhân viên mới", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = UITheme.createTextField(15);
        JTextField txtName = UITheme.createTextField(15);
        JTextField txtPhone = UITheme.createTextField(15);
        JTextField txtSalary = UITheme.createTextField(15);

        JComboBox<String> cbShift = new JComboBox<>(new String[]{
                "Ca sáng (06:00 - 14:00)", 
                "Ca chiều (14:00 - 22:00)", 
                "Ca hành chính"
        });
        cbShift.setFont(UITheme.FONT_REGULAR);
        cbShift.setBackground(Color.WHITE);

        JTextField txtHireDate = UITheme.createTextField(15);
        JTextField txtManagerId = UITheme.createTextField(15);

        if (isEdit) {
            txtId.setText(staffToEdit.getStaffId());
            txtId.setEditable(false);
            txtName.setText(staffToEdit.getFullName());
            txtPhone.setText(staffToEdit.getPhone());
            txtSalary.setText(String.valueOf((long) staffToEdit.getSalary()));
            cbShift.setSelectedItem(staffToEdit.getShift());
            txtHireDate.setText(staffToEdit.getHireDate());
            txtManagerId.setText(staffToEdit.getManagerId());
        } else {
            txtId.setText("ST" + String.format("%03d", data.getStaffList().size() + 1));
            txtSalary.setText("8000000");
            txtHireDate.setText(LocalDate.now().toString());
            txtManagerId.setText("ST001");
        }

        addFormRow(panel, gbc, 0, "Mã nhân viên (*):", txtId);
        addFormRow(panel, gbc, 1, "Họ và tên (*):", txtName);
        addFormRow(panel, gbc, 2, "Số điện thoại (*):", txtPhone);
        addFormRow(panel, gbc, 3, "Mức lương (VNĐ) (*):", txtSalary);
        addFormRow(panel, gbc, 4, "Ca làm việc (*):", cbShift);
        addFormRow(panel, gbc, 5, "Ngày tuyển dụng (*):", txtHireDate);
        addFormRow(panel, gbc, 6, "Mã quản lý:", txtManagerId);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String salaryStr = txtSalary.getText().trim();
            String shift = (String) cbShift.getSelectedItem();
            String hireDate = txtHireDate.getText().trim();
            String managerId = txtManagerId.getText().trim();

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || salaryStr.isEmpty() || hireDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double salary;
            try {
                salary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Mức lương phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                staffToEdit.setFullName(name);
                staffToEdit.setPhone(phone);
                staffToEdit.setSalary(salary);
                staffToEdit.setShift(shift);
                staffToEdit.setHireDate(hireDate);
                staffToEdit.setManagerId(managerId);
                JOptionPane.showMessageDialog(dialog, "Cập nhật nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (findStaffById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã nhân viên đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Staff newStaff = new Staff(id, name, phone, salary, shift, hireDate, managerId.isEmpty() ? "None" : managerId);
                data.getStaffList().add(newStaff);
                JOptionPane.showMessageDialog(dialog, "Thêm mới nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getStaffList());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 7;
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
