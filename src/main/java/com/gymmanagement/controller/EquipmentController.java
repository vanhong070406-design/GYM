package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Area;
import com.gymmanagement.model.Equipment;
import com.gymmanagement.view.EquipmentView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối Quản lý thiết bị
 */
public class EquipmentController {
    private final EquipmentView view;
    private final SampleData data;

    public EquipmentController(EquipmentView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getEquipmentList());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleFilter());
        view.getTxtSearch().addActionListener(e -> handleFilter());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            view.getCbFilterStatus().setSelectedIndex(0);
            loadData(data.getEquipmentList());
        });

        view.getBtnAdd().addActionListener(e -> showEquipmentDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một thiết bị để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String eqId = (String) view.getTableModel().getValueAt(row, 0);
            Equipment eq = findEquipmentById(eqId);
            if (eq != null) {
                showEquipmentDialog(eq);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một thiết bị cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String eqId = (String) view.getTableModel().getValueAt(row, 0);
            String eqName = (String) view.getTableModel().getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa thiết bị: " + eqName + " (" + eqId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getEquipmentList().removeIf(e1 -> e1.getEquipmentId().equalsIgnoreCase(eqId));
                loadData(data.getEquipmentList());
                JOptionPane.showMessageDialog(view, "Xóa thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Equipment> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Equipment eq : list) {
            model.addRow(new Object[]{
                    eq.getEquipmentId(),
                    eq.getEquipmentName(),
                    String.format("%,.0f", eq.getValue()),
                    eq.getPurchaseDate(),
                    eq.getStatus(),
                    eq.getAreaId()
            });
        }
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String statusFilter = (String) view.getCbFilterStatus().getSelectedItem();

        List<Equipment> filtered = data.getEquipmentList().stream()
                .filter(eq -> {
                    boolean matchQ = q.isEmpty()
                            || eq.getEquipmentId().toLowerCase().contains(q)
                            || eq.getEquipmentName().toLowerCase().contains(q)
                            || eq.getAreaId().toLowerCase().contains(q);

                    boolean matchStatus = "Tất cả".equals(statusFilter)
                            || eq.getStatus().equalsIgnoreCase(statusFilter);

                    return matchQ && matchStatus;
                })
                .toList();

        loadData(filtered);
    }

    private Equipment findEquipmentById(String id) {
        for (Equipment eq : data.getEquipmentList()) {
            if (eq.getEquipmentId().equalsIgnoreCase(id)) return eq;
        }
        return null;
    }

    private void showEquipmentDialog(Equipment eqToEdit) {
        boolean isEdit = (eqToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa thiết bị" : "Thêm thiết bị mới", true);
        dialog.setSize(480, 440);
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
        JTextField txtValue = UITheme.createTextField(15);
        JTextField txtPurchaseDate = UITheme.createTextField(15);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Tốt", "Đang bảo trì", "Cần thay thế linh kiện"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        JComboBox<Area> cbArea = new JComboBox<>();
        for (Area a : data.getAreas()) cbArea.addItem(a);
        cbArea.setFont(UITheme.FONT_REGULAR);
        cbArea.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(eqToEdit.getEquipmentId());
            txtId.setEditable(false);
            txtName.setText(eqToEdit.getEquipmentName());
            txtValue.setText(String.valueOf((long) eqToEdit.getValue()));
            txtPurchaseDate.setText(eqToEdit.getPurchaseDate());
            cbStatus.setSelectedItem(eqToEdit.getStatus());
            for (int i = 0; i < cbArea.getItemCount(); i++) {
                if (cbArea.getItemAt(i).getAreaId().equalsIgnoreCase(eqToEdit.getAreaId())) {
                    cbArea.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            txtId.setText("EQ" + String.format("%02d", data.getEquipmentList().size() + 1));
            txtValue.setText("25000000");
            txtPurchaseDate.setText(LocalDate.now().toString());
        }

        addFormRow(panel, gbc, 0, "Mã thiết bị (*):", txtId);
        addFormRow(panel, gbc, 1, "Tên thiết bị (*):", txtName);
        addFormRow(panel, gbc, 2, "Giá trị (VNĐ) (*):", txtValue);
        addFormRow(panel, gbc, 3, "Ngày mua (*):", txtPurchaseDate);
        addFormRow(panel, gbc, 4, "Trạng thái (*):", cbStatus);
        addFormRow(panel, gbc, 5, "Khu vực đặt (*):", cbArea);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String valStr = txtValue.getText().trim();
            String pDate = txtPurchaseDate.getText().trim();
            String status = (String) cbStatus.getSelectedItem();
            Area selArea = (Area) cbArea.getSelectedItem();

            if (id.isEmpty() || name.isEmpty() || valStr.isEmpty() || pDate.isEmpty() || selArea == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin thiết bị!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double val;
            try {
                val = Double.parseDouble(valStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá trị thiết bị phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                eqToEdit.setEquipmentName(name);
                eqToEdit.setValue(val);
                eqToEdit.setPurchaseDate(pDate);
                eqToEdit.setStatus(status);
                eqToEdit.setAreaId(selArea.getAreaId());
                JOptionPane.showMessageDialog(dialog, "Cập nhật thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (findEquipmentById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã thiết bị đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Equipment newEq = new Equipment(id, name, val, pDate, status, selArea.getAreaId());
                data.getEquipmentList().add(newEq);
                JOptionPane.showMessageDialog(dialog, "Thêm mới thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getEquipmentList());
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
