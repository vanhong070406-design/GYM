package com.gymmanagement.controller;

import com.gymmanagement.dao.EquipmentDao;
import com.gymmanagement.dao.MaintenanceDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.model.Equipment;
import com.gymmanagement.model.MaintenanceAssignment;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.MainView;
import com.gymmanagement.view.MaintenanceView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối chức năng Phân công bảo trì thiết bị
 */
public class MaintenanceController {
    private final MaintenanceView view;
    private final UserSession session;
    private List<MaintenanceAssignment> shown = List.of();
    private final MaintenanceDao dao = new MaintenanceDao();
    private final StaffDao staffDao = new StaffDao();
    private final EquipmentDao equipmentDao = new EquipmentDao();

    public MaintenanceController(MaintenanceView view, UserSession session) {
        this.view = view;
        this.session = session;
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

        view.getBtnAdd().addActionListener(e -> showMaintenanceDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            MaintenanceAssignment selected = selectedRow("Vui lòng chọn một dòng phân công để cập nhật!");
            if (selected != null) showMaintenanceDialog(selected);
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng phân công để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String stId = (String) view.getTableModel().getValueAt(row, 0);
            String eqId = (String) view.getTableModel().getValueAt(row, 2);
            LocalDate maintenanceDate = LocalDate.parse((String) view.getTableModel().getValueAt(row, 4));

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa phân công NV " + stId + " bảo trì thiết bị " + eqId + " ngày " + maintenanceDate + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(stId, eqId, maintenanceDate))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<MaintenanceAssignment> fetchAll() {
        List<MaintenanceAssignment> all = DbAction.fetch(view, dao::findAll, List.of());
        if (!session.isOwnOnly(MainView.CARD_MAINTENANCE)) return all;
        // Kỹ thuật viên chỉ thấy phiếu được giao cho mình
        return all.stream().filter(m -> m.getStaffId().equals(session.getStaffId())).toList();
    }

    public void reload() {
        handleSearch();
    }

    /** Dòng đang chọn trên bảng, hoặc null kèm thông báo nếu chưa chọn. */
    private MaintenanceAssignment selectedRow(String message) {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return shown.get(view.getTable().convertRowIndexToModel(row));
    }

    public void loadData(List<MaintenanceAssignment> list) {
        shown = list;
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (MaintenanceAssignment m : list) {
            model.addRow(new Object[]{
                    m.getStaffId(),
                    m.getStaffName(),
                    m.getEquipmentId(),
                    m.getEquipmentName(),
                    Formats.text(m.getMaintenanceDate()),
                    m.getNote(),
                    m.getStatus()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<MaintenanceAssignment> filtered = fetchAll().stream()
                .filter(m -> m.getStaffId().toLowerCase().contains(q)
                        || m.getEquipmentId().toLowerCase().contains(q)
                        || m.getStaffName().toLowerCase().contains(q)
                        || m.getEquipmentName().toLowerCase().contains(q)
                        || m.getNote().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private void showMaintenanceDialog(MaintenanceAssignment toEdit) {
        boolean isEdit = toEdit != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Cập nhật phiếu bảo trì" : "Phân công bảo trì thiết bị mới", true);
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
        for (Staff s : DbAction.fetch(view, isEdit ? staffDao::findAll : () -> staffDao.findByPosition(Position.TECHNICIAN), List.<Staff>of())) cbStaff.addItem(s);
        cbStaff.setFont(UITheme.FONT_REGULAR);
        cbStaff.setBackground(Color.WHITE);

        JComboBox<Equipment> cbEquipment = new JComboBox<>();
        for (Equipment eq : DbAction.fetch(view, equipmentDao::findAll, List.<Equipment>of())) cbEquipment.addItem(eq);
        cbEquipment.setFont(UITheme.FONT_REGULAR);
        cbEquipment.setBackground(Color.WHITE);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JTextField txtNote = UITheme.createTextField(15);
        txtNote.setText("Kiểm tra định kỳ và bôi trơn máy");

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang bảo trì", "Chờ linh kiện", "Đã hoàn thành"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            // Nhân viên, thiết bị và ngày là khóa của phiếu nên không đổi được; muốn đổi thì xóa và phân công lại
            for (int i = 0; i < cbStaff.getItemCount(); i++) {
                if (cbStaff.getItemAt(i).getStaffId().equals(toEdit.getStaffId())) cbStaff.setSelectedIndex(i);
            }
            for (int i = 0; i < cbEquipment.getItemCount(); i++) {
                if (cbEquipment.getItemAt(i).getEquipmentId().equals(toEdit.getEquipmentId())) cbEquipment.setSelectedIndex(i);
            }
            txtDate.setText(Formats.text(toEdit.getMaintenanceDate()));
            txtNote.setText(toEdit.getNote());
            cbStatus.setSelectedItem(toEdit.getStatus());
            cbStaff.setEnabled(false);
            cbEquipment.setEnabled(false);
            txtDate.setEditable(false);
        }

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
            String dateText = txtDate.getText().trim();
            String note = txtNote.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (selStaff == null || selEq == null || dateText.isEmpty() || note.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin phân công!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate date;
            try {
                date = Formats.parseDate(dateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                String previousStatus = toEdit.getStatus();
                toEdit.setNote(note);
                toEdit.setStatus(status);
                if (!DbAction.run(dialog, () -> dao.update(toEdit, previousStatus))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật phiếu bảo trì thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                reload();
                dialog.dispose();
                return;
            }

            MaintenanceAssignment newAssign = new MaintenanceAssignment(
                    selStaff.getStaffId(), selEq.getEquipmentId(), date, note, status);
            // DAO cập nhật luôn trạng thái thiết bị sang Đang bảo trì nếu phù hợp
            if (!DbAction.run(dialog, () -> dao.insert(newAssign))) return;

            JOptionPane.showMessageDialog(dialog, "Phân công bảo trì thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
