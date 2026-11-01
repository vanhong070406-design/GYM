package com.gymmanagement.controller;

import com.gymmanagement.dao.AreaCleaningDao;
import com.gymmanagement.dao.AreaDao;
import com.gymmanagement.dao.ShiftDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.model.Area;
import com.gymmanagement.model.AreaCleaning;
import com.gymmanagement.model.Shift;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.AreaCleaningView;
import com.gymmanagement.view.MainView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối chức năng Phân công vệ sinh khu vực
 */
public class AreaCleaningController {
    private final AreaCleaningView view;
    private final UserSession session;
    private final AreaCleaningDao dao = new AreaCleaningDao();
    private final StaffDao staffDao = new StaffDao();
    private final AreaDao areaDao = new AreaDao();
    private final ShiftDao shiftDao = new ShiftDao();
    private List<AreaCleaning> shown = List.of();

    public AreaCleaningController(AreaCleaningView view, UserSession session) {
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

        view.getBtnAdd().addActionListener(e -> showCleaningDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            AreaCleaning selected = selectedRow("Vui lòng chọn một dòng phân công để cập nhật!");
            if (selected != null) showCleaningDialog(selected);
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng phân công để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Khóa chính gồm cả ngày và ca nên lấy trực tiếp từ dòng đang chọn
            AreaCleaning selected = shown.get(view.getTable().convertRowIndexToModel(row));
            String stId = selected.getStaffId();
            String arId = selected.getAreaId();

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa phân công NV " + stId + " dọn khu vực " + arId + " (" + selected.getCleaningDate() + ", " + selected.getShiftName() + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(selected.getAreaId(), selected.getCleaningDate(), selected.getShiftId()))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<AreaCleaning> fetchAll() {
        List<AreaCleaning> all = DbAction.fetch(view, dao::findAll, List.of());
        if (!session.isOwnOnly(MainView.CARD_CLEANING)) return all;
        // Tạp vụ chỉ thấy việc được giao cho mình
        return all.stream().filter(ac -> ac.getStaffId().equals(session.getStaffId())).toList();
    }

    public void reload() {
        handleSearch();
    }

    /** Dòng đang chọn trên bảng, hoặc null kèm thông báo nếu chưa chọn. */
    private AreaCleaning selectedRow(String message) {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return shown.get(view.getTable().convertRowIndexToModel(row));
    }

    public void loadData(List<AreaCleaning> list) {
        shown = list;
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (AreaCleaning ac : list) {
            model.addRow(new Object[]{
                    ac.getStaffId(),
                    ac.getStaffName(),
                    ac.getAreaId(),
                    ac.getAreaName(),
                    Formats.text(ac.getCleaningDate()),
                    ac.getShiftName(),
                    ac.getStatus()
            });
        }
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<AreaCleaning> filtered = fetchAll().stream()
                .filter(ac -> ac.getStaffId().toLowerCase().contains(q)
                        || ac.getAreaId().toLowerCase().contains(q)
                        || ac.getStaffName().toLowerCase().contains(q)
                        || ac.getAreaName().toLowerCase().contains(q)
                        || ac.getShiftName().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private void showCleaningDialog(AreaCleaning toEdit) {
        boolean isEdit = toEdit != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Cập nhật phân công vệ sinh" : "Phân công vệ sinh mới", true);
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
        boolean ownOnly = session.isOwnOnly(MainView.CARD_CLEANING);
        for (Staff s : DbAction.fetch(view, ownOnly ? () -> List.<Staff>of() : () -> staffDao.findByPosition(Position.CLEANER), List.<Staff>of())) cbStaff.addItem(s);
        cbStaff.setFont(UITheme.FONT_REGULAR);
        cbStaff.setBackground(Color.WHITE);

        JComboBox<Area> cbArea = new JComboBox<>();
        for (Area a : DbAction.fetch(view, areaDao::findAll, List.<Area>of())) cbArea.addItem(a);
        cbArea.setFont(UITheme.FONT_REGULAR);
        cbArea.setBackground(Color.WHITE);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JComboBox<Shift> cbShift = new JComboBox<>();
        for (Shift sh : DbAction.fetch(view, shiftDao::findAll, List.<Shift>of())) cbShift.addItem(sh);
        cbShift.setFont(UITheme.FONT_REGULAR);
        cbShift.setBackground(Color.WHITE);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Chưa làm", "Đang thực hiện", "Đã hoàn thành"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            // Khu vực, ngày và ca là khóa của phân công nên không đổi được; muốn đổi thì xóa và phân công lại
            boolean found = false;
            for (int i = 0; i < cbStaff.getItemCount(); i++) {
                if (cbStaff.getItemAt(i).getStaffId().equals(toEdit.getStaffId())) {
                    cbStaff.setSelectedIndex(i);
                    found = true;
                }
            }
            if (!found) {
                // Tạp vụ không có quyền xem danh sách nhân viên; người được giao có thể đã đổi chức vụ
                Staff current = new Staff();
                current.setStaffId(toEdit.getStaffId());
                current.setFullName(toEdit.getStaffName());
                cbStaff.addItem(current);
                cbStaff.setSelectedItem(current);
            }
            for (int i = 0; i < cbArea.getItemCount(); i++) {
                if (cbArea.getItemAt(i).getAreaId().equals(toEdit.getAreaId())) cbArea.setSelectedIndex(i);
            }
            for (int i = 0; i < cbShift.getItemCount(); i++) {
                if (cbShift.getItemAt(i).getShiftId().equals(toEdit.getShiftId())) cbShift.setSelectedIndex(i);
            }
            txtDate.setText(Formats.text(toEdit.getCleaningDate()));
            cbStatus.setSelectedItem(toEdit.getStatus());
            cbStaff.setEnabled(!ownOnly);
            cbArea.setEnabled(false);
            cbShift.setEnabled(false);
            txtDate.setEditable(false);
        }

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
            String dateText = txtDate.getText().trim();
            Shift shift = (Shift) cbShift.getSelectedItem();
            String status = (String) cbStatus.getSelectedItem();

            if (selStaff == null || selArea == null || dateText.isEmpty() || shift == null) {
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
                toEdit.setStaffId(selStaff.getStaffId());
                toEdit.setStatus(status);
                if (!DbAction.run(dialog, () -> dao.update(toEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                reload();
                dialog.dispose();
                return;
            }

            AreaCleaning newClean = new AreaCleaning(selStaff.getStaffId(), selArea.getAreaId(), date, shift.getShiftId(), status);
            if (!DbAction.run(dialog, () -> dao.insert(newClean))) return;

            JOptionPane.showMessageDialog(dialog, "Phân công vệ sinh thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
