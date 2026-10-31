package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Area;
import com.gymmanagement.model.AreaCleaning;
import com.gymmanagement.model.Staff;
import com.gymmanagement.view.AreaCleaningView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối chức năng Phân công vệ sinh khu vực
 */
public class AreaCleaningController {
    private final AreaCleaningView view;
    private final SampleData data;

    public AreaCleaningController(AreaCleaningView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getAreaCleanings());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getAreaCleanings());
        });

        view.getBtnAdd().addActionListener(e -> showCleaningDialog());

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng phân công để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String stId = (String) view.getTableModel().getValueAt(row, 0);
            String arId = (String) view.getTableModel().getValueAt(row, 2);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa phân công NV " + stId + " dọn khu vực " + arId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getAreaCleanings().removeIf(ac -> 
                        ac.getStaffId().equalsIgnoreCase(stId) && ac.getAreaId().equalsIgnoreCase(arId));
                loadData(data.getAreaCleanings());
                JOptionPane.showMessageDialog(view, "Xóa phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<AreaCleaning> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (AreaCleaning ac : list) {
            String staffName = getStaffName(ac.getStaffId());
            String areaName = getAreaName(ac.getAreaId());
            model.addRow(new Object[]{
                    ac.getStaffId(),
                    staffName,
                    ac.getAreaId(),
                    areaName,
                    ac.getCleaningDate(),
                    ac.getShift(),
                    ac.getStatus()
            });
        }
    }

    private String getStaffName(String id) {
        for (Staff s : data.getStaffList()) {
            if (s.getStaffId().equalsIgnoreCase(id)) return s.getFullName();
        }
        return "N/A";
    }

    private String getAreaName(String id) {
        for (Area a : data.getAreas()) {
            if (a.getAreaId().equalsIgnoreCase(id)) return a.getAreaName();
        }
        return "N/A";
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getAreaCleanings());
            return;
        }

        List<AreaCleaning> filtered = data.getAreaCleanings().stream()
                .filter(ac -> ac.getStaffId().toLowerCase().contains(q)
                        || ac.getAreaId().toLowerCase().contains(q)
                        || getStaffName(ac.getStaffId()).toLowerCase().contains(q)
                        || getAreaName(ac.getAreaId()).toLowerCase().contains(q)
                        || ac.getShift().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private void showCleaningDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Phân công vệ sinh mới", true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Staff> cbStaff = new JComboBox<>();
        for (Staff s : data.getStaffList()) cbStaff.addItem(s);
        cbStaff.setFont(UITheme.FONT_REGULAR);
        cbStaff.setBackground(Color.WHITE);

        JComboBox<Area> cbArea = new JComboBox<>();
        for (Area a : data.getAreas()) cbArea.addItem(a);
        cbArea.setFont(UITheme.FONT_REGULAR);
        cbArea.setBackground(Color.WHITE);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JComboBox<String> cbShift = new JComboBox<>(new String[]{"Ca sáng", "Ca chiều", "Ca đêm"});
        cbShift.setFont(UITheme.FONT_REGULAR);
        cbShift.setBackground(Color.WHITE);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Chưa làm", "Đang thực hiện", "Đã hoàn thành"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        addFormRow(panel, gbc, 0, "Chọn nhân viên (*):", cbStaff);
        addFormRow(panel, gbc, 1, "Chọn khu vực (*):", cbArea);
        addFormRow(panel, gbc, 2, "Ngày phân công (*):", txtDate);
        addFormRow(panel, gbc, 3, "Ca trực (*):", cbShift);
        addFormRow(panel, gbc, 4, "Trạng thái:", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu phân công");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            Staff selStaff = (Staff) cbStaff.getSelectedItem();
            Area selArea = (Area) cbArea.getSelectedItem();
            String date = txtDate.getText().trim();
            String shift = (String) cbShift.getSelectedItem();
            String status = (String) cbStatus.getSelectedItem();

            if (selStaff == null || selArea == null || date.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin phân công!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AreaCleaning newClean = new AreaCleaning(selStaff.getStaffId(), selArea.getAreaId(), date, shift, status);
            data.getAreaCleanings().add(newClean);

            JOptionPane.showMessageDialog(dialog, "Phân công vệ sinh thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadData(data.getAreaCleanings());
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
