package com.gymmanagement.controller;

import com.gymmanagement.dao.PositionDao;
import com.gymmanagement.dao.ShiftDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Shift;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.StaffView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối Quản lý nhân viên
 */
public class StaffController {
    private final StaffView view;
    private final UserSession session;
    private final PositionDao positionDao = new PositionDao();
    private final StaffDao dao = new StaffDao();
    private final ShiftDao shiftDao = new ShiftDao();

    public StaffController(StaffView view, UserSession session) {
        this.view = view;
        this.session = session;
        initListeners();
        reload();
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(fetchAll());
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

            if (stId.equalsIgnoreCase(session.getStaffId())) {
                JOptionPane.showMessageDialog(view, "Không thể xóa nhân viên đang đăng nhập!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa nhân viên: " + stName + " (" + stId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(stId))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<Staff> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleSearch();
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
                    s.getShift().toString(),
                    Formats.text(s.getHireDate()),
                    s.getManagerId() == null ? "None" : s.getManagerId(),
                    s.getPositionName()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<Staff> filtered = fetchAll().stream()
                .filter(s -> s.getStaffId().toLowerCase().contains(q)
                        || s.getFullName().toLowerCase().contains(q)
                        || s.getPhone().contains(q)
                        || s.getShift().toString().toLowerCase().contains(q)
                        || s.getPositionName().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Staff findStaffById(String id) {
        for (Staff s : fetchAll()) {
            if (s.getStaffId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void showStaffDialog(Staff staffToEdit) {
        boolean isEdit = (staffToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa nhân viên" : "Thêm nhân viên mới", true);
        dialog.setSize(480, 530);
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

        JComboBox<Shift> cbShift = new JComboBox<>();
        for (Shift sh : DbAction.fetch(view, shiftDao::findAll, List.<Shift>of())) cbShift.addItem(sh);
        cbShift.setFont(UITheme.FONT_REGULAR);
        cbShift.setBackground(Color.WHITE);

        JComboBox<Position> cbPosition = new JComboBox<>();
        for (Position p : DbAction.fetch(view, positionDao::findStaffPositions, List.<Position>of())) cbPosition.addItem(p);
        cbPosition.setFont(UITheme.FONT_REGULAR);
        cbPosition.setBackground(Color.WHITE);

        JTextField txtHireDate = UITheme.createTextField(15);
        JTextField txtManagerId = UITheme.createTextField(15);

        if (isEdit) {
            txtId.setText(staffToEdit.getStaffId());
            txtId.setEditable(false);
            txtName.setText(staffToEdit.getFullName());
            txtPhone.setText(staffToEdit.getPhone());
            txtSalary.setText(staffToEdit.getSalary().toPlainString());
            for (int i = 0; i < cbShift.getItemCount(); i++) {
                if (cbShift.getItemAt(i).getShiftId().equals(staffToEdit.getShiftId())) {
                    cbShift.setSelectedIndex(i);
                    break;
                }
            }
            txtHireDate.setText(Formats.text(staffToEdit.getHireDate()));
            txtManagerId.setText(Formats.nullToEmpty(staffToEdit.getManagerId()));
            for (int i = 0; i < cbPosition.getItemCount(); i++) {
                if (cbPosition.getItemAt(i).getPositionId().equals(staffToEdit.getPositionId())) {
                    cbPosition.setSelectedIndex(i);
                    break;
                }
            }
            // Quản lý không tự đổi chức vụ của mình, tránh mất quyền quản trị
            cbPosition.setEnabled(!staffToEdit.getStaffId().equals(session.getStaffId()));
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
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
        addFormRow(panel, gbc, 7, "Chức vụ (*):", cbPosition);

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
            Shift shift = (Shift) cbShift.getSelectedItem();
            Position position = (Position) cbPosition.getSelectedItem();
            String hireDateText = txtHireDate.getText().trim();
            String managerText = txtManagerId.getText().trim();
            String managerId = managerText.isEmpty() || managerText.equalsIgnoreCase("None") ? null : managerText;

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || salaryStr.isEmpty() || hireDateText.isEmpty() || shift == null || position == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate hireDate;
            try {
                hireDate = Formats.parseDate(hireDateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal salary;
            try {
                salary = new BigDecimal(salaryStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Mức lương phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                staffToEdit.setFullName(name);
                staffToEdit.setPhone(phone);
                staffToEdit.setSalary(salary);
                staffToEdit.setShiftId(shift.getShiftId());
                staffToEdit.setHireDate(hireDate);
                staffToEdit.setManagerId(managerId);
                staffToEdit.setPositionId(position.getPositionId());
                if (!DbAction.run(dialog, () -> dao.update(staffToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Staff newStaff = new Staff(id, name, phone, salary, shift.getShiftId(), hireDate, managerId, position.getPositionId());
                if (!DbAction.run(dialog, () -> dao.insert(newStaff))) return;
                JOptionPane.showMessageDialog(dialog, "Thêm mới nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(fetchAll());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 8;
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
