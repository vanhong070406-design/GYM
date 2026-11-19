package com.gymmanagement.controller;

import com.gymmanagement.dao.TrainerDao;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.view.ActionColumn;
import com.gymmanagement.view.TrainerView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối Quản lý huấn luyện viên
 */
public class TrainerController {
    private final TrainerView view;
    private final TrainerDao dao = new TrainerDao();
    /** Mở trang chi tiết của HLV có mã được truyền vào. */
    private Consumer<String> onOpenDetail = id -> {};

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TrainerController(TrainerView view) {
        this.view = view;
        initListeners();
        reload();
    }

    public void setOnOpenDetail(Consumer<String> onOpenDetail) {
        this.onOpenDetail = onOpenDetail;
    }

    private void initListeners() {
        // Cột Thao tác: xem chi tiết / sửa / xóa. Nháy đúp vào dòng bất kỳ ngoài cột đó cũng mở trang chi tiết.
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = view.getTable();
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                String trId = (String) view.getTableModel().getValueAt(row, 0);
                ActionColumn.Action action = view.getActionColumn().actionAt(e.getPoint());
                if (action == null) {
                    if (e.getClickCount() == 2) onOpenDetail.accept(trId);
                    return;
                }
                if (e.getClickCount() != 1) return; // nháy đúp vào biểu tượng không chạy hành động hai lần
                switch (action) {
                    case VIEW -> onOpenDetail.accept(trId);
                    case EDIT -> {
                        Trainer tr = findTrainerById(trId);
                        if (tr != null) showTrainerDialog(tr);
                    }
                    case DELETE -> confirmDelete(trId, (String) view.getTableModel().getValueAt(row, 1));
                }
            }
        });

        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(fetchAll());
        });

        view.getBtnAdd().addActionListener(e -> showTrainerDialog(null));
    }

    private void confirmDelete(String trId, String trName) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa huấn luyện viên: " + trName + " (" + trId + ") không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(trId))) return;
            loadData(fetchAll());
            JOptionPane.showMessageDialog(view, "Xóa huấn luyện viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private List<Trainer> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleSearch();
    }

    /** Tách chuỗi nhập "Yoga, Pilates & Giảm mỡ" thành từng chuyên môn, bỏ trùng. */
    private static List<String> parseSpecialties(String text) {
        return Arrays.stream(text.split("[,;&|]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    public void loadData(List<Trainer> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Trainer t : list) {
            model.addRow(new Object[]{
                    t.getTrainerId(),
                    t.getFullName(),
                    t.getPhone(),
                    t.getExperienceYears() + " năm",
                    String.join(", ", t.getSpecialties()),
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

        List<Trainer> filtered = fetchAll().stream()
                .filter(t -> t.getTrainerId().toLowerCase().contains(q)
                        || t.getFullName().toLowerCase().contains(q)
                        || String.join(", ", t.getSpecialties()).toLowerCase().contains(q)
                        || t.getPhone().contains(q))
                .toList();

        loadData(filtered);
    }

    private Trainer findTrainerById(String id) {
        for (Trainer t : fetchAll()) {
            if (t.getTrainerId().equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    private void showTrainerDialog(Trainer trainerToEdit) {
        boolean isEdit = (trainerToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa huấn luyện viên" : "Thêm huấn luyện viên mới", true);
        dialog.setSize(620, 380);
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
        txtSpecialty.setToolTipText("Nhiều chuyên môn thì cách nhau bằng dấu phẩy");

        if (isEdit) {
            txtId.setText(trainerToEdit.getTrainerId());
            txtId.setEditable(false);
            txtName.setText(trainerToEdit.getFullName());
            txtPhone.setText(trainerToEdit.getPhone());
            txtExp.setText(String.valueOf(trainerToEdit.getExperienceYears()));
            txtSpecialty.setText(String.join(", ", trainerToEdit.getSpecialties()));
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            txtExp.setText("3");
        }

        addFormRow(panel, gbc, 0, "Mã HLV (*):", txtId);
        addFormRow(panel, gbc, 1, "Họ và tên (*):", txtName);
        addFormRow(panel, gbc, 2, "Số điện thoại (*):", txtPhone);
        addFormRow(panel, gbc, 3, "Kinh nghiệm (năm):", txtExp);
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
            String expText = txtExp.getText().trim();
            List<String> spec = parseSpecialties(txtSpecialty.getText());

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các thông tin bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int exp;
            try {
                exp = expText.isEmpty() ? 0 : Integer.parseInt(expText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Kinh nghiệm phải là số năm (số nguyên)!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                trainerToEdit.setFullName(name);
                trainerToEdit.setPhone(phone);
                trainerToEdit.setExperienceYears(exp);
                trainerToEdit.setSpecialties(spec);
                if (!DbAction.run(dialog, () -> dao.update(trainerToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật HLV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Trainer newTrainer = new Trainer(id, name, phone, exp, spec);
                if (!DbAction.run(dialog, () -> dao.insert(newTrainer))) return;
                JOptionPane.showMessageDialog(dialog, "Thêm mới HLV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(fetchAll());
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
