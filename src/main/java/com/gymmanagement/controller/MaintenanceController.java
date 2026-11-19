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
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối chức năng Phân công bảo trì thiết bị
 */
public class MaintenanceController {
    private final MaintenanceView view;
    private final UserSession session;
    /** Phiếu đang hiện trong bảng, cùng thứ tự với các dòng. */
    private List<MaintenanceAssignment> shown = List.of();
    /** Đang trả ô trạng thái về giá trị cũ, bỏ qua sự kiện do chính việc này sinh ra. */
    private boolean reverting;
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
        view.getBtnSearch().addActionListener(e -> handleFilter());
        view.getTxtSearch().addActionListener(e -> handleFilter());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            view.getCbFilterStatus().setSelectedIndex(0);
            loadData(fetchAll());
        });

        view.getBtnAdd().addActionListener(e -> showMaintenanceDialog(null));

        // Cột Thao tác: bút chì để sửa nội dung, thùng rác để xóa phiếu
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = view.getTable();
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0 || table.convertColumnIndexToModel(col) != MaintenanceView.ACTION_COLUMN) return;
                MaintenanceAssignment m = shown.get(table.convertRowIndexToModel(row));
                if (view.actionAt(e.getPoint()) == MaintenanceView.Action.EDIT) {
                    showMaintenanceDialog(m);
                } else {
                    confirmDelete(m);
                }
            }
        });

        // Đổi trạng thái bằng combobox trong bảng thì lưu ngay; hủy hoặc lưu lỗi thì trả ô về giá trị cũ
        view.getTableModel().addTableModelListener(e -> {
            if (reverting || e.getType() != TableModelEvent.UPDATE || e.getColumn() != MaintenanceView.STATUS_COLUMN) return;
            int row = e.getFirstRow();
            if (row < 0 || row >= shown.size()) return;
            // Lấy phiếu ngay bây giờ: tới lúc lưu, bảng có thể đã được tải lại và số dòng không còn đúng
            MaintenanceAssignment m = shown.get(row);
            String newStatus = (String) view.getTableModel().getValueAt(row, MaintenanceView.STATUS_COLUMN);
            // Chờ bảng đóng combobox xong mới hỏi/lưu, để hộp thoại không hiện giữa lúc đang sửa ô
            SwingUtilities.invokeLater(() -> saveStatus(m, newStatus));
        });
    }

    private void saveStatus(MaintenanceAssignment m, String newStatus) {
        String previousStatus = m.getStatus();
        if (newStatus.equals(previousStatus)) return;
        if (MaintenanceAssignment.STATUS_DONE.equals(newStatus) && !confirmComplete(view, m)) {
            revertStatus(m);
            return;
        }
        m.setStatus(newStatus);
        if (!DbAction.run(view, () -> dao.update(m, previousStatus))) {
            m.setStatus(previousStatus);
            revertStatus(m);
            return;
        }
        // Đang lọc theo trạng thái thì phiếu vừa đổi không còn khớp, tải lại để nó biến khỏi danh sách
        if (!MaintenanceView.FILTER_ALL.equals(view.getCbFilterStatus().getSelectedItem())) handleFilter();
        else view.getTable().repaint(); // phiếu vừa hoàn thành hiện ổ khóa ngay
    }

    /** Trả ô trạng thái của phiếu về giá trị đang lưu, nếu phiếu vẫn còn trên bảng. */
    private void revertStatus(MaintenanceAssignment m) {
        int row = shown.indexOf(m);
        if (row < 0) return;
        reverting = true;
        view.getTableModel().setValueAt(m.getStatus(), row, MaintenanceView.STATUS_COLUMN);
        reverting = false;
    }

    /** Hỏi trước khi hoàn thành phiếu, vì thiết bị sẽ đổi trạng thái và phiếu bị khóa. */
    private boolean confirmComplete(Component parent, MaintenanceAssignment m) {
        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Hoàn thành phiếu bảo trì " + m.getEquipmentName() + " (" + m.getEquipmentId() + ")?\n"
                        + "Thiết bị sẽ về \"" + Equipment.STATUS_GOOD + "\" nếu không còn phiếu nào đang mở.\n"
                        + "Sau khi hoàn thành, phiếu không đổi trạng thái được nữa.",
                "Xác nhận hoàn thành",
                JOptionPane.YES_NO_OPTION
        );
        return confirm == JOptionPane.YES_OPTION;
    }

    private void confirmDelete(MaintenanceAssignment m) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa phiếu bảo trì " + m.getEquipmentName() + " (" + m.getEquipmentId() + ") giao cho "
                        + m.getStaffName() + " ngày " + Formats.text(m.getMaintenanceDate()) + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        if (!DbAction.run(view, () -> dao.delete(m.getStaffId(), m.getEquipmentId(), m.getMaintenanceDate()))) return;
        handleFilter();
        JOptionPane.showMessageDialog(view, "Xóa phiếu bảo trì thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<MaintenanceAssignment> fetchAll() {
        List<MaintenanceAssignment> all = DbAction.fetch(view, dao::findAll, List.of());
        if (!session.isOwnOnly(MainView.CARD_MAINTENANCE)) return all;
        // Kỹ thuật viên chỉ thấy phiếu được giao cho mình
        return all.stream().filter(m -> m.getStaffId().equals(session.getStaffId())).toList();
    }

    public void reload() {
        handleFilter();
    }

    public void loadData(List<MaintenanceAssignment> list) {
        // Đang mở combobox trạng thái thì đóng lại trước, tránh lưu nhầm vào dòng của danh sách mới
        if (view.getTable().isEditing()) view.getTable().getCellEditor().cancelCellEditing();
        shown = list;
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (MaintenanceAssignment m : list) {
            model.addRow(new Object[]{
                    Formats.text(m.getMaintenanceDate()),
                    MaintenanceView.twoLine(m.getEquipmentName(), m.getEquipmentId()),
                    MaintenanceView.twoLine(m.getStaffName(), m.getStaffId()),
                    m.getNote(),
                    m.getStatus(),
                    ""
            });
        }
        view.setCount(list.size());
    }

    /** Lọc theo ô tìm kiếm và trạng thái cùng lúc. */
    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String status = (String) view.getCbFilterStatus().getSelectedItem();
        boolean allStatuses = status == null || MaintenanceView.FILTER_ALL.equals(status);

        List<MaintenanceAssignment> filtered = fetchAll().stream()
                .filter(m -> allStatuses || status.equals(m.getStatus()))
                .filter(m -> q.isEmpty()
                        || m.getStaffId().toLowerCase().contains(q)
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
        dialog.setSize(640, 420);
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

        // Phân công mới chỉ chọn được thiết bị cần thay linh kiện / tới hạn bảo trì và chưa có phiếu đang mở.
        // Cập nhật phiếu cũ thì cần cả thiết bị đã về "Hoạt động tốt" để hiện đúng thiết bị của phiếu (ô này bị khóa).
        JComboBox<Equipment> cbEquipment = new JComboBox<>();
        for (Equipment eq : DbAction.fetch(view, isEdit ? equipmentDao::findAll : equipmentDao::findNeedingMaintenance, List.<Equipment>of())) {
            cbEquipment.addItem(eq);
        }
        if (!isEdit && cbEquipment.getItemCount() == 0) {
            dialog.dispose();
            JOptionPane.showMessageDialog(view,
                    "Không có thiết bị nào cần bảo trì.\nChỉ phân công được thiết bị đang \"" + Equipment.STATUS_NEEDS_PARTS + "\" hoặc \""
                            + Equipment.STATUS_MAINTENANCE_DUE + "\" và chưa có phiếu bảo trì đang mở.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        cbEquipment.setFont(UITheme.FONT_REGULAR);
        cbEquipment.setBackground(Color.WHITE);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JTextField txtNote = UITheme.createTextField(15);
        txtNote.setText("Kiểm tra định kỳ và bôi trơn máy");

        JComboBox<String> cbStatus = new JComboBox<>(MaintenanceAssignment.STATUSES.toArray(new String[0]));
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
            // Phiếu đã hoàn thành bị khóa trạng thái, chỉ còn sửa được nội dung
            cbStatus.setEnabled(!MaintenanceAssignment.STATUS_DONE.equals(toEdit.getStatus()));
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
                if (MaintenanceAssignment.STATUS_DONE.equals(status) && !MaintenanceAssignment.STATUS_DONE.equals(previousStatus)
                        && !confirmComplete(dialog, toEdit)) {
                    return;
                }
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
            reload();
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
