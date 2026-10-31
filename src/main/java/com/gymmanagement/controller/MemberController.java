package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Member;
import com.gymmanagement.view.MemberView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối chức năng Quản lý hội viên
 */
public class MemberController {
    private final MemberView view;
    private final SampleData data;

    public MemberController(MemberView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getMembers());
    }

    private void initListeners() {
        // Nút Tìm kiếm
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        // Nút Làm mới
        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getMembers());
        });

        // Nút Thêm
        view.getBtnAdd().addActionListener(e -> showMemberDialog(null));

        // Nút Chỉnh sửa
        view.getBtnEdit().addActionListener(e -> {
            int selectedRow = view.getTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một hội viên từ danh sách để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String memberId = (String) view.getTableModel().getValueAt(selectedRow, 0);
            Member member = findMemberById(memberId);
            if (member != null) {
                showMemberDialog(member);
            }
        });

        // Nút Xóa
        view.getBtnDelete().addActionListener(e -> {
            int selectedRow = view.getTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một hội viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String memberId = (String) view.getTableModel().getValueAt(selectedRow, 0);
            String fullName = (String) view.getTableModel().getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc chắn muốn xóa hội viên: " + fullName + " (" + memberId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getMembers().removeIf(m -> m.getMemberId().equalsIgnoreCase(memberId));
                loadData(data.getMembers());
                JOptionPane.showMessageDialog(view, "Xóa hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Member> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Member m : list) {
            model.addRow(new Object[]{
                    m.getMemberId(),
                    m.getFullName(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getJoinDate(),
                    m.getAddress(),
                    m.getBirthDate(),
                    m.getGender()
            });
        }
    }

    private void handleSearch() {
        String query = view.getTxtSearch().getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadData(data.getMembers());
            return;
        }

        List<Member> filtered = data.getMembers().stream()
                .filter(m -> m.getMemberId().toLowerCase().contains(query)
                        || m.getFullName().toLowerCase().contains(query)
                        || m.getPhone().contains(query)
                        || m.getEmail().toLowerCase().contains(query))
                .toList();

        loadData(filtered);
    }

    private Member findMemberById(String id) {
        for (Member m : data.getMembers()) {
            if (m.getMemberId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    /**
     * Mở biểu mẫu chi tiết (Thêm / Chỉnh sửa)
     */
    private void showMemberDialog(Member memberToEdit) {
        boolean isEdit = (memberToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), 
                isEdit ? "Chỉnh sửa thông tin hội viên" : "Thêm hội viên mới", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JTextField txtId = UITheme.createTextField(15);
        JTextField txtName = UITheme.createTextField(15);
        JTextField txtEmail = UITheme.createTextField(15);
        JTextField txtPhone = UITheme.createTextField(15);
        JTextField txtJoinDate = UITheme.createTextField(15);
        JTextField txtAddress = UITheme.createTextField(15);
        JTextField txtBirthDate = UITheme.createTextField(15);
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cbGender.setFont(UITheme.FONT_REGULAR);
        cbGender.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(memberToEdit.getMemberId());
            txtId.setEditable(false);
            txtName.setText(memberToEdit.getFullName());
            txtEmail.setText(memberToEdit.getEmail());
            txtPhone.setText(memberToEdit.getPhone());
            txtJoinDate.setText(memberToEdit.getJoinDate());
            txtAddress.setText(memberToEdit.getAddress());
            txtBirthDate.setText(memberToEdit.getBirthDate());
            cbGender.setSelectedItem(memberToEdit.getGender());
        } else {
            txtId.setText("MB" + String.format("%03d", data.getMembers().size() + 1));
            txtJoinDate.setText(java.time.LocalDate.now().toString());
            txtBirthDate.setText("2000-01-01");
        }

        // Add form rows
        addFormRow(panel, gbc, 0, "Mã hội viên (*):", txtId);
        addFormRow(panel, gbc, 1, "Họ và tên (*):", txtName);
        addFormRow(panel, gbc, 2, "Email (*):", txtEmail);
        addFormRow(panel, gbc, 3, "Số điện thoại (*):", txtPhone);
        addFormRow(panel, gbc, 4, "Ngày tham gia:", txtJoinDate);
        addFormRow(panel, gbc, 5, "Địa chỉ:", txtAddress);
        addFormRow(panel, gbc, 6, "Ngày sinh:", txtBirthDate);
        addFormRow(panel, gbc, 7, "Giới tính:", cbGender);

        // Nút lưu & hủy
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String joinDate = txtJoinDate.getText().trim();
            String address = txtAddress.getText().trim();
            String birthDate = txtBirthDate.getText().trim();
            String gender = (String) cbGender.getSelectedItem();

            // Kiểm tra các trường bắt buộc
            if (id.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường bắt buộc (*): Mã, Tên, Số điện thoại!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                memberToEdit.setFullName(name);
                memberToEdit.setEmail(email);
                memberToEdit.setPhone(phone);
                memberToEdit.setJoinDate(joinDate);
                memberToEdit.setAddress(address);
                memberToEdit.setBirthDate(birthDate);
                memberToEdit.setGender(gender);
                JOptionPane.showMessageDialog(dialog, "Cập nhật thông tin hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Kiểm tra trùng mã
                if (findMemberById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã hội viên đã tồn tại, vui lòng chọn mã khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Member newMember = new Member(id, name, email, phone, joinDate, address, birthDate, gender);
                data.getMembers().add(newMember);
                JOptionPane.showMessageDialog(dialog, "Thêm mới hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getMembers());
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
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }
}
