package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Area;
import com.gymmanagement.view.AreaView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Controller điều phối chức năng Quản lý khu vực
 */
public class AreaController {
    private final AreaView view;
    private final SampleData data;

    public AreaController(AreaView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getAreas());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getAreas());
        });

        view.getBtnAdd().addActionListener(e -> showAreaDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một khu vực để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String areaId = (String) view.getTableModel().getValueAt(row, 0);
            Area area = findAreaById(areaId);
            if (area != null) {
                showAreaDialog(area);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một khu vực cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String areaId = (String) view.getTableModel().getValueAt(row, 0);
            String areaName = (String) view.getTableModel().getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa khu vực: " + areaName + " (" + areaId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getAreas().removeIf(a -> a.getAreaId().equalsIgnoreCase(areaId));
                loadData(data.getAreas());
                JOptionPane.showMessageDialog(view, "Xóa khu vực thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<Area> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Area a : list) {
            model.addRow(new Object[]{
                    a.getAreaId(),
                    a.getAreaName(),
                    a.getFloor(),
                    a.getDescription()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getAreas());
            return;
        }

        List<Area> filtered = data.getAreas().stream()
                .filter(a -> a.getAreaId().toLowerCase().contains(q)
                        || a.getAreaName().toLowerCase().contains(q)
                        || a.getFloor().toLowerCase().contains(q)
                        || a.getDescription().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Area findAreaById(String id) {
        for (Area a : data.getAreas()) {
            if (a.getAreaId().equalsIgnoreCase(id)) return a;
        }
        return null;
    }

    private void showAreaDialog(Area areaToEdit) {
        boolean isEdit = (areaToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa khu vực" : "Thêm khu vực mới", true);
        dialog.setSize(460, 360);
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
        JComboBox<String> cbFloor = new JComboBox<>(new String[]{"Tầng 1", "Tầng 2", "Tầng 3", "Tầng M"});
        cbFloor.setFont(UITheme.FONT_REGULAR);
        cbFloor.setBackground(Color.WHITE);

        JTextField txtDesc = UITheme.createTextField(15);

        if (isEdit) {
            txtId.setText(areaToEdit.getAreaId());
            txtId.setEditable(false);
            txtName.setText(areaToEdit.getAreaName());
            cbFloor.setSelectedItem(areaToEdit.getFloor());
            txtDesc.setText(areaToEdit.getDescription());
        } else {
            txtId.setText("AR" + String.format("%02d", data.getAreas().size() + 1));
        }

        addFormRow(panel, gbc, 0, "Mã khu vực (*):", txtId);
        addFormRow(panel, gbc, 1, "Tên khu vực (*):", txtName);
        addFormRow(panel, gbc, 2, "Vị trí tầng (*):", cbFloor);
        addFormRow(panel, gbc, 3, "Mô tả chức năng:", txtDesc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String floor = (String) cbFloor.getSelectedItem();
            String desc = txtDesc.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mã và tên khu vực!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                areaToEdit.setAreaName(name);
                areaToEdit.setFloor(floor);
                areaToEdit.setDescription(desc);
                JOptionPane.showMessageDialog(dialog, "Cập nhật khu vực thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (findAreaById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã khu vực đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Area newArea = new Area(id, name, floor, desc);
                data.getAreas().add(newArea);
                JOptionPane.showMessageDialog(dialog, "Thêm mới khu vực thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getAreas());
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 4;
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
