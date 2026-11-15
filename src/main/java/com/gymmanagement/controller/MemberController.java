package com.gymmanagement.controller;

import com.gymmanagement.dao.MemberDao;
import com.gymmanagement.model.Member;
import com.gymmanagement.view.ActionColumn;
import com.gymmanagement.view.MemberView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối chức năng Quản lý hội viên
 */
public class MemberController {
    private final MemberView view;
    private final MemberDao dao = new MemberDao();

    public MemberController(MemberView view) {
        this.view = view;
        initListeners();
        reload();
    }

    private void initListeners() {
        // Nút Tìm kiếm
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        // Nút Làm mới
        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(fetchAll());
        });

        // Nút Thêm
        view.getBtnAdd().addActionListener(e -> showMemberDialog(null));

        // Cột Thao tác: bút chì để sửa, thùng rác để xóa
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionColumn.Action action = view.getActionColumn().actionAt(e.getPoint());
                if (action == null || e.getClickCount() != 1) return;
                int row = view.getTable().rowAtPoint(e.getPoint());
                String memberId = (String) view.getTableModel().getValueAt(row, 0);
                if (action == ActionColumn.Action.EDIT) {
                    Member member = findMemberById(memberId);
                    if (member != null) showMemberDialog(member);
                } else if (action == ActionColumn.Action.DELETE) {
                    confirmDelete(memberId, (String) view.getTableModel().getValueAt(row, 1));
                }
            }
        });
    }

    private void confirmDelete(String memberId, String fullName) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc chắn muốn xóa hội viên: " + fullName + " (" + memberId + ") không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(memberId))) return;
            loadData(fetchAll());
            JOptionPane.showMessageDialog(view, "Xóa hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private List<Member> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    /** Nạp lại từ database, giữ nguyên từ khóa tìm kiếm đang nhập. */
    public void reload() {
        handleSearch();
    }

    public void loadData(List<Member> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Member m : list) {
            model.addRow(new Object[]{
                    m.getMemberId(),
                    m.getFullName(),
                    Formats.nullToEmpty(m.getEmail()),
                    m.getPhone(),
                    Formats.text(m.getJoinDate()),
                    Formats.nullToEmpty(m.getAddress()),
                    Formats.text(m.getBirthDate()),
                    m.getGender(),
                    "" // cột Thao tác: chỉ vẽ biểu tượng
            });
        }
        view.setCount(list.size());
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<Member> filtered = fetchAll().stream()
                .filter(m -> m.getMemberId().toLowerCase().contains(q)
                        || m.getFullName().toLowerCase().contains(q)
                        || m.getPhone().contains(q)
                        || Formats.nullToEmpty(m.getEmail()).toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Member findMemberById(String id) {
        for (Member m : fetchAll()) {
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
            txtEmail.setText(Formats.nullToEmpty(memberToEdit.getEmail()));
            txtPhone.setText(memberToEdit.getPhone());
            txtJoinDate.setText(Formats.text(memberToEdit.getJoinDate()));
            txtAddress.setText(Formats.nullToEmpty(memberToEdit.getAddress()));
            txtBirthDate.setText(Formats.text(memberToEdit.getBirthDate()));
            cbGender.setSelectedItem(memberToEdit.getGender());
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
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
            String email = Formats.emptyToNull(txtEmail.getText().trim());
            String phone = txtPhone.getText().trim();
            String joinDateText = txtJoinDate.getText().trim();
            String address = Formats.emptyToNull(txtAddress.getText().trim());
            String birthDateText = txtBirthDate.getText().trim();
            String gender = (String) cbGender.getSelectedItem();

            // Kiểm tra các trường bắt buộc
            if (id.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường bắt buộc (*): Mã, Tên, Số điện thoại!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.time.LocalDate parsedJoin;
            java.time.LocalDate birthDate;
            try {
                parsedJoin = Formats.parseDate(joinDateText);
                birthDate = Formats.parseDate(birthDateText);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.time.LocalDate joinDate = parsedJoin != null ? parsedJoin : java.time.LocalDate.now();

            if (isEdit) {
                memberToEdit.setFullName(name);
                memberToEdit.setEmail(email);
                memberToEdit.setPhone(phone);
                memberToEdit.setJoinDate(joinDate);
                memberToEdit.setAddress(address);
                memberToEdit.setBirthDate(birthDate);
                memberToEdit.setGender(gender);
                if (!DbAction.run(dialog, () -> dao.update(memberToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật thông tin hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Member newMember = new Member(id, name, email, phone, joinDate, address, birthDate, gender);
                if (!DbAction.run(dialog, () -> dao.insert(newMember))) return;
                JOptionPane.showMessageDialog(dialog, "Thêm mới hội viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
