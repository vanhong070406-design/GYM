package com.gymmanagement.controller;

import com.gymmanagement.dao.PackageDao;
import com.gymmanagement.model.PackageModel;
import com.gymmanagement.view.PackageView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller điều phối Quản lý gói tập
 */
public class PackageController {
    private final PackageView view;
    private final PackageDao dao = new PackageDao();

    public PackageController(PackageView view) {
        this.view = view;
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

        view.getBtnAdd().addActionListener(e -> showPackageDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một gói tập để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String pkgId = (String) view.getTableModel().getValueAt(row, 0);
            PackageModel pkg = findPackageById(pkgId);
            if (pkg != null) {
                showPackageDialog(pkg);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một gói tập cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String pkgId = (String) view.getTableModel().getValueAt(row, 0);
            String pkgName = (String) view.getTableModel().getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa gói tập: " + pkgName + " (" + pkgId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(pkgId))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa gói tập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<PackageModel> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleSearch();
    }

    public void loadData(List<PackageModel> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (PackageModel p : list) {
            model.addRow(new Object[]{
                    p.getPackageId(),
                    p.getPackageName(),
                    String.format("%,.0f", p.getPrice()),
                    p.getDuration(),
                    Formats.nullToEmpty(p.getDescription())
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<PackageModel> filtered = fetchAll().stream()
                .filter(p -> p.getPackageId().toLowerCase().contains(q)
                        || p.getPackageName().toLowerCase().contains(q)
                        || Formats.nullToEmpty(p.getDescription()).toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private PackageModel findPackageById(String id) {
        for (PackageModel p : fetchAll()) {
            if (p.getPackageId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    private void showPackageDialog(PackageModel pkgToEdit) {
        boolean isEdit = (pkgToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa gói tập" : "Thêm gói tập mới", true);
        dialog.setSize(460, 420);
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
        JTextField txtPrice = UITheme.createTextField(15);
        JTextField txtDuration = UITheme.createTextField(15);
        JTextArea txtDesc = new JTextArea(3, 15);
        txtDesc.setFont(UITheme.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);

        if (isEdit) {
            txtId.setText(pkgToEdit.getPackageId());
            txtId.setEditable(false);
            txtName.setText(pkgToEdit.getPackageName());
            txtPrice.setText(pkgToEdit.getPrice().toPlainString());
            txtDuration.setText(String.valueOf(pkgToEdit.getDuration()));
            txtDesc.setText(Formats.nullToEmpty(pkgToEdit.getDescription()));
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            txtPrice.setText("1000000");
            txtDuration.setText("30");
        }

        addFormRow(panel, gbc, 0, "Mã gói tập (*):", txtId);
        addFormRow(panel, gbc, 1, "Tên gói tập (*):", txtName);
        addFormRow(panel, gbc, 2, "Giá tiền (VNĐ) (*):", txtPrice);
        addFormRow(panel, gbc, 3, "Thời lượng (ngày) (*):", txtDuration);
        addFormRow(panel, gbc, 4, "Mô tả quyền lợi:", scrollDesc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String durStr = txtDuration.getText().trim();
            String desc = Formats.emptyToNull(txtDesc.getText().trim());

            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty() || durStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal price;
            int duration;
            try {
                price = new BigDecimal(priceStr);
                duration = Integer.parseInt(durStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá tiền và thời lượng phải là số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                pkgToEdit.setPackageName(name);
                pkgToEdit.setPrice(price);
                pkgToEdit.setDuration(duration);
                pkgToEdit.setDescription(desc);
                if (!DbAction.run(dialog, () -> dao.update(pkgToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật gói tập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                PackageModel newPkg = new PackageModel(id, name, price, duration, desc);
                if (!DbAction.run(dialog, () -> dao.insert(newPkg))) return;
                JOptionPane.showMessageDialog(dialog, "Thêm mới gói tập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
