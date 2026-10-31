package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.view.TrainerView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối Quản lý huấn luyện viên
 */
public class TrainerController {
    private final TrainerView view;
    private final SampleData data;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TrainerController(TrainerView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getTrainers());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getTrainers());
        });

        view.getBtnAdd().addActionListener(e -> showTrainerDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một huấn luyện viên để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String trId = (String) view.getTableModel().getValueAt(row, 0);
            Trainer tr = findTrainerById(trId);
            if (tr != null) {
                showTrainerDialog(tr);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một huấn luyện viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String trId = (String) view.getTableModel().getValueAt(row, 0);
            String trName = (String) view.getTableModel().getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa huấn luyện viên: " + trName + " (" + trId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getTrainers().removeIf(t -> t.getTrainerId().equalsIgnoreCase(trId));
                loadData(data.getTrainers());
                JOptionPane.showMessageDialog(view, "Xóa huấn luyện viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Trainer> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Trainer t : list) {
            model.addRow(new Object[]{
                    t.getTrainerId(),
                    t.getFullName(),
                    t.getPhone(),
                    t.getExperience(),
                    t.getSpecialty()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getTrainers());
            return;
        }

        List<Trainer> filtered = data.getTrainers().stream()
                .filter(t -> t.getTrainerId().toLowerCase().contains(q)
                        || t.getFullName().toLowerCase().contains(q)
                        || t.getSpecialty().toLowerCase().contains(q)
                        || t.getPhone().contains(q))
                .toList();

        loadData(filtered);
    }

    private Trainer findTrainerById(String id) {
        for (Trainer t : data.getTrainers()) {
            if (t.getTrainerId().equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    private void showTrainerDialog(Trainer trainerToEdit) {
        boolean isEdit = (trainerToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa huấn luyện viên" : "Thêm huấn luyện viên mới", true);
        dialog.setSize(460, 380);
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
        JTextField txtExp = UITheme.createTextField(15);
        JTextField txtSpecialty = UITheme.createTextField(15);

        if (isEdit) {
            txtId.setText(trainerToEdit.getTrainerId());
            txtId.setEditable(false);
            txtName.setText(trainerToEdit.getFullName());
            txtPhone.setText(trainerToEdit.getPhone());
            txtExp.setText(trainerToEdit.getExperience());
            txtSpecialty.setText(trainerToEdit.getSpecialty());
        } else {
            txtId.setText("TR" + String.format("%03d", data.getTrainers().size() + 1));
            txtExp.setText("3 năm");
        }

        addFormRow(panel, gbc, 0, "Mã HLV (*):", txtId);
        addFormRow(panel, gbc, 1, "Họ và tên (*):", txtName);
        addFormRow(panel, gbc, 2, "Số điện thoại (*):", txtPhone);
        addFormRow(panel, gbc, 3, "Kinh nghiệm:", txtExp);
        addFormRow(panel, gbc, 4, "Chuyên môn (*):", txtSpecialty);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String exp = txtExp.getText().trim();
            String spec = txtSpecialty.getText().trim();

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các thông tin bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                trainerToEdit.setFullName(name);
                trainerToEdit.setPhone(phone);
                trainerToEdit.setExperience(exp);
                trainerToEdit.setSpecialty(spec);
                JOptionPane.showMessageDialog(dialog, "Cập nhật HLV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (findTrainerById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã HLV đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Trainer newTrainer = new Trainer(id, name, phone, exp, spec);
                data.getTrainers().add(newTrainer);
                JOptionPane.showMessageDialog(dialog, "Thêm mới HLV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getTrainers());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 5;
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
