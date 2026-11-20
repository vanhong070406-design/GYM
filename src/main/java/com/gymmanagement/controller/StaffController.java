package com.gymmanagement.controller;

import com.gymmanagement.dao.PositionDao;
import com.gymmanagement.dao.ShiftDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.dao.UserAccountDao;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Shift;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.ActionColumn;
import com.gymmanagement.view.StaffView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// Controller điều phối Quản lý nhân viên, gồm cả cấp tài khoản đăng nhập cho nhân viên

public class StaffController {
    private static final String MANAGER_POSITION = "QL";

    private final StaffView view;
    private final UserSession session;
    private final PositionDao positionDao = new PositionDao();
    private final StaffDao dao = new StaffDao();
    private final ShiftDao shiftDao = new ShiftDao();
    private final UserAccountDao accountDao = new UserAccountDao();

    public StaffController(StaffView view, UserSession session) {
        this.view = view;
        this.session = session;
        view.setCurrentStaffId(session.getStaffId());
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

        // Cột Thao tác: chìa khóa cấp tài khoản, bút chì sửa, thùng rác xóa
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionColumn.Action action = view.getActionColumn().actionAt(e.getPoint());
                if (action == null || e.getClickCount() != 1) return;
                int row = view.getTable().rowAtPoint(e.getPoint());
                String stId = (String) view.getTableModel().getValueAt(row, 0);
                if (action == ActionColumn.Action.ACCOUNT) {
                    Staff staff = findStaffById(stId);
                    if (staff != null) showAccountDialog(staff);
                } else if (action == ActionColumn.Action.EDIT) {
                    Staff staff = findStaffById(stId);
                    if (staff != null) showStaffDialog(staff);
                } else if (action == ActionColumn.Action.DELETE) {
                    confirmDelete(stId, (String) view.getTableModel().getValueAt(row, 1));
                }
            }
        });
    }

    // Cấp tài khoản mới, hoặc đặt lại mật khẩu nếu nhân viên đã có tài khoản
    private void showAccountDialog(Staff staff) {
        // Đọc lại từ database: máy khác có thể vừa cấp tài khoản cho nhân viên này
        UserAccountDao.StaffAccount existing = fetchAccounts().get(staff.getStaffId());
        if (AccountDialog.show(SwingUtilities.getWindowAncestor(view), staff, existing)) {
            handleSearch();
        }
    }

    private void confirmDelete(String stId, String stName) {
        if (stId.equalsIgnoreCase(session.getStaffId())) {
            JOptionPane.showMessageDialog(view, "Không thể xóa nhân viên đang đăng nhập!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa nhân viên: " + stName + " (" + stId + ") không?\nTài khoản đăng nhập của nhân viên này (nếu có) cũng bị xóa.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(stId))) return;
            loadData(fetchAll());
            JOptionPane.showMessageDialog(view, "Xóa nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private List<Staff> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    private Map<String, UserAccountDao.StaffAccount> fetchAccounts() {
        return DbAction.fetch(view, accountDao::findStaffAccounts, Map.<String, UserAccountDao.StaffAccount>of());
    }

    public void reload() {
        handleSearch();
    }

    public void loadData(List<Staff> list) {
        Map<String, UserAccountDao.StaffAccount> accounts = fetchAccounts();
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        Set<String> loginStaffIds = new HashSet<>();
        for (Staff s : list) {
            UserAccountDao.StaffAccount acc = accounts.get(s.getStaffId());
            if (s.canLogin()) loginStaffIds.add(s.getStaffId());
            model.addRow(new Object[]{
                    s.getStaffId(),
                    s.getFullName(),
                    s.getPhone(),
                    String.format("%,.0f", s.getSalary()),
                    s.getShift().toString(),
                    Formats.text(s.getHireDate()),
                    s.getPositionName(),
                    usernameText(s, acc),
                    passwordStatus(s, acc),
                    "" // cột Thao tác: chỉ vẽ biểu tượng
            });
        }
        view.setLoginStaffIds(loginStaffIds);
        view.setCount(list.size());
    }

    private static String usernameText(Staff s, UserAccountDao.StaffAccount acc) {
        if (!s.canLogin()) return StaffView.NOT_APPLICABLE;
        return acc == null ? StaffView.NO_ACCOUNT : acc.username();
    }

    // Mật khẩu được băm một chiều nên không hiện được mật khẩu thật, chỉ hiện trạng thái để theo dõi
    private static String passwordStatus(Staff s, UserAccountDao.StaffAccount acc) {
        if (!s.canLogin()) return StaffView.NOT_APPLICABLE;
        if (acc == null) return StaffView.NO_ACCOUNT;
        return acc.mustChangePassword() ? "Tạm – chờ đổi" : "Đã thiết lập";
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        Map<String, UserAccountDao.StaffAccount> accounts = fetchAccounts();
        List<Staff> filtered = fetchAll().stream()
                .filter(s -> s.getStaffId().toLowerCase().contains(q)
                        || s.getFullName().toLowerCase().contains(q)
                        || s.getPhone().contains(q)
                        || s.getShift().toString().toLowerCase().contains(q)
                        || s.getPositionName().toLowerCase().contains(q)
                        || (accounts.containsKey(s.getStaffId()) && accounts.get(s.getStaffId()).username().toLowerCase().contains(q)))
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
        dialog.setSize(640, 530);
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

        // Danh sách chức vụ không có "Quản lý". Riêng khi sửa chính người quản lý thì thêm lại chức vụ này
        // (và khóa ô chọn) để lưu không làm họ bị đổi sang chức vụ khác.
        JComboBox<Position> cbPosition = new JComboBox<>();
        for (Position p : DbAction.fetch(view, positionDao::findStaffPositions, List.<Position>of())) cbPosition.addItem(p);
        boolean editingManager = isEdit && MANAGER_POSITION.equals(staffToEdit.getPositionId());
        if (editingManager) cbPosition.addItem(new Position(MANAGER_POSITION, staffToEdit.getPositionName(), staffToEdit.canLogin()));
        cbPosition.setFont(UITheme.FONT_REGULAR);
        cbPosition.setBackground(Color.WHITE);

        JTextField txtHireDate = UITheme.createTextField(15);

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
            for (int i = 0; i < cbPosition.getItemCount(); i++) {
                if (cbPosition.getItemAt(i).getPositionId().equals(staffToEdit.getPositionId())) {
                    cbPosition.setSelectedIndex(i);
                    break;
                }
            }
            // Quản lý không đổi chức vụ của mình, tránh mất quyền quản trị
            cbPosition.setEnabled(!editingManager && !staffToEdit.getStaffId().equals(session.getStaffId()));
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            txtSalary.setText("8000000");
            txtHireDate.setText(LocalDate.now().toString());
        }

        addFormRow(panel, gbc, 0, "Mã nhân viên (*):", txtId);
        addFormRow(panel, gbc, 1, "Họ và tên (*):", txtName);
        addFormRow(panel, gbc, 2, "Số điện thoại (*):", txtPhone);
        addFormRow(panel, gbc, 3, "Mức lương (VNĐ) (*):", txtSalary);
        addFormRow(panel, gbc, 4, "Ca làm việc (*):", cbShift);
        addFormRow(panel, gbc, 5, "Ngày tuyển dụng (*):", txtHireDate);
        addFormRow(panel, gbc, 6, "Chức vụ (*):", cbPosition);

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
                // Chuyển sang chức vụ không được đăng nhập mà đang có tài khoản: hỏi trước, đồng ý thì xóa tài khoản
                // (đọc lại từ database vì máy khác có thể vừa cấp tài khoản cho nhân viên này)
                UserAccountDao.StaffAccount account = position.canLogin() ? null : fetchAccounts().get(staffToEdit.getStaffId());
                if (account != null) {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "Chức vụ " + position.getPositionName() + " không được có tài khoản đăng nhập.\n"
                                    + "Tài khoản \"" + account.username() + "\" của nhân viên này sẽ bị xóa. Tiếp tục?",
                            "Xác nhận đổi chức vụ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }
                staffToEdit.setFullName(name);
                staffToEdit.setPhone(phone);
                staffToEdit.setSalary(salary);
                staffToEdit.setShiftId(shift.getShiftId());
                staffToEdit.setHireDate(hireDate);
                staffToEdit.setPositionId(position.getPositionId());
                Runnable save = account != null ? () -> dao.updateAndDeleteAccount(staffToEdit) : () -> dao.update(staffToEdit);
                if (!DbAction.run(dialog, save)) return;
                JOptionPane.showMessageDialog(dialog,
                        account != null ? "Cập nhật nhân viên thành công! Đã xóa tài khoản \"" + account.username() + "\"."
                                        : "Cập nhật nhân viên thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Staff newStaff = new Staff(id, name, phone, salary, shift.getShiftId(), hireDate, position.getPositionId());
                if (!DbAction.run(dialog, () -> dao.insert(newStaff))) return;
                JOptionPane.showMessageDialog(dialog,
                        position.canLogin()
                                ? "Thêm mới nhân viên thành công!\nBấm biểu tượng chìa khóa ở dòng nhân viên để cấp tài khoản đăng nhập."
                                : "Thêm mới nhân viên thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(fetchAll());
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