package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Equipment;
import com.gymmanagement.model.MaintenanceAssignment;
import com.gymmanagement.model.Staff;
import com.gymmanagement.view.MaintenanceView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối chức năng Phân công bảo trì thiết bị
 */
public class MaintenanceController {
    private final MaintenanceView view;
    private final SampleData data;

    public MaintenanceController(MaintenanceView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getMaintenanceAssignments());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getTxtSearch().addActionListener(e -> handleSearch());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            loadData(data.getMaintenanceAssignments());
        });

        view.getBtnAdd().addActionListener(e -> showMaintenanceDialog());

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng phân công để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String stId = (String) view.getTableModel().getValueAt(row, 0);
            String eqId = (String) view.getTableModel().getValueAt(row, 2);

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa phân công NV " + stId + " bảo trì thiết bị " + eqId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                data.getMaintenanceAssignments().removeIf(m -> 
                        m.getStaffId().equalsIgnoreCase(stId) && m.getEquipmentId().equalsIgnoreCase(eqId));
                loadData(data.getMaintenanceAssignments());
                JOptionPane.showMessageDialog(view, "Xóa phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<MaintenanceAssignment> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (MaintenanceAssignment m : list) {
            String staffName = getStaffName(m.getStaffId());
            String eqName = getEquipmentName(m.getEquipmentId());
            model.addRow(new Object[]{
                    m.getStaffId(),
                    staffName,
                    m.getEquipmentId(),
                    eqName,
                    m.getMaintenanceDate(),
                    m.getNote(),
                    m.getStatus()
            });
        }
    }

    private String getStaffName(String id) {
        for (Staff s : data.getStaffList()) {
            if (s.getStaffId().equalsIgnoreCase(id)) return s.getFullName();
        }
        return "N/A";
    }

    private String getEquipmentName(String id) {
        for (Equipment e : data.getEquipmentList()) {
            if (e.getEquipmentId().equalsIgnoreCase(id)) return e.getEquipmentName();
        }
        return "N/A";
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(data.getMaintenanceAssignments());
            return;
        }

        List<MaintenanceAssignment> filtered = data.getMaintenanceAssignments().stream()
                .filter(m -> m.getStaffId().toLowerCase().contains(q)
                        || m.getEquipmentId().toLowerCase().contains(q)
                        || getStaffName(m.getStaffId()).toLowerCase().contains(q)
                        || getEquipmentName(m.getEquipmentId()).toLowerCase().contains(q)
                        || m.getNote().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private void showMaintenanceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Phân công bảo trì thiết bị mới", true);
        dialog.setSize(480, 420);
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

        JComboBox<Equipment> cbEquipment = new JComboBox<>();
        for (Equipment eq : data.getEquipmentList()) cbEquipment.addItem(eq);
        cbEquipment.setFont(UITheme.FONT_REGULAR);
        cbEquipment.setBackground(Color.WHITE);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JTextField txtNote = UITheme.createTextField(15);
        txtNote.setText("Kiểm tra định kỳ và bôi trơn máy");

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang bảo trì", "Chờ linh kiện", "Đã hoàn thành"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        addFormRow(panel, gbc, 0, "Chọn nhân viên (*):", cbStaff);
        addFormRow(panel, gbc, 1, "Chọn thiết bị (*):", cbEquipment);
        addFormRow(panel, gbc, 2, "Ngày phân công (*):", txtDate);
        addFormRow(panel, gbc, 3, "Nội dung bảo trì (*):", txtNote);
        addFormRow(panel, gbc, 4, "Trạng thái:", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu phân công");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            Staff selStaff = (Staff) cbStaff.getSelectedItem();
            Equipment selEq = (Equipment) cbEquipment.getSelectedItem();
            String date = txtDate.getText().trim();
            String note = txtNote.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (selStaff == null || selEq == null || date.isEmpty() || note.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin phân công!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            MaintenanceAssignment newAssign = new MaintenanceAssignment(
                    selStaff.getStaffId(), selEq.getEquipmentId(), date, note, status);
            data.getMaintenanceAssignments().add(newAssign);

            // Cập nhật trạng thái của thiết bị sang Đang bảo trì nếu phù hợp
            if ("Đang bảo trì".equals(status)) {
                selEq.setStatus("Đang bảo trì");
            }

            JOptionPane.showMessageDialog(dialog, "Phân công bảo trì thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadData(data.getMaintenanceAssignments());
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
