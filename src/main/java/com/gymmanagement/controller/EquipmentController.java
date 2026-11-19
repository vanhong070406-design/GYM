package com.gymmanagement.controller;

import com.gymmanagement.dao.EquipmentDao;
import com.gymmanagement.model.Equipment;
import com.gymmanagement.view.EquipmentView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối Quản lý thiết bị
 */
public class EquipmentController {
    private final EquipmentView view;
    private final EquipmentDao dao = new EquipmentDao();
    /** Thiết bị đang hiện trong bảng, để biết trạng thái cũ khi phải hoàn tác. */
    private List<Equipment> shown = List.of();
    /** Đang trả ô trạng thái về giá trị cũ, bỏ qua sự kiện do chính việc này sinh ra. */
    private boolean reverting;

    public EquipmentController(EquipmentView view) {
        this.view = view;
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

        view.getBtnAdd().addActionListener(e -> showEquipmentDialog(null));

        // Cột Thao tác: nửa trái là biểu tượng sửa, nửa phải là biểu tượng xóa
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = view.getTable();
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0 || table.convertColumnIndexToModel(col) != EquipmentView.ACTION_COLUMN) return;
                String eqId = (String) view.getTableModel().getValueAt(row, 0);
                if (view.isEditHalf(e.getPoint())) {
                    Equipment eq = findEquipmentById(eqId);
                    if (eq != null) showEquipmentDialog(eq);
                } else {
                    confirmDelete(eqId, (String) view.getTableModel().getValueAt(row, 1));
                }
            }
        });

        // Đổi trạng thái bằng combobox trong bảng thì lưu ngay; lưu lỗi thì trả ô về giá trị cũ
        view.getTableModel().addTableModelListener(e -> {
            if (reverting || e.getType() != TableModelEvent.UPDATE || e.getColumn() != EquipmentView.STATUS_COLUMN) return;
            int row = e.getFirstRow();
            String eqId = (String) view.getTableModel().getValueAt(row, 0);
            String newStatus = (String) view.getTableModel().getValueAt(row, EquipmentView.STATUS_COLUMN);
            // Chờ bảng đóng combobox xong mới lưu, để hộp thoại lỗi (nếu có) không hiện giữa lúc đang sửa ô
            SwingUtilities.invokeLater(() -> saveStatus(row, eqId, newStatus));
        });
    }

    private void saveStatus(int row, String eqId, String newStatus) {
        Equipment eq = shown.stream().filter(x -> x.getEquipmentId().equals(eqId)).findFirst().orElse(null);
        if (eq == null || newStatus.equals(eq.getStatus())) return;
        if (DbAction.run(view, () -> dao.updateStatus(eqId, newStatus))) {
            eq.setStatus(newStatus);
            // Đang lọc theo trạng thái thì thiết bị vừa đổi không còn khớp, tải lại để nó biến khỏi danh sách
            if (!"Tất cả".equals(view.getCbFilterStatus().getSelectedItem())) handleFilter();
        } else {
            reverting = true;
            view.getTableModel().setValueAt(eq.getStatus(), row, EquipmentView.STATUS_COLUMN);
            reverting = false;
        }
    }

    private void confirmDelete(String eqId, String eqName) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa thiết bị: " + eqName + " (" + eqId + ") không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        if (!DbAction.run(view, () -> dao.delete(eqId))) return;
        handleFilter();
        JOptionPane.showMessageDialog(view, "Xóa thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<Equipment> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleFilter();
    }

    public void loadData(List<Equipment> list) {
        shown = list;
        view.setCount(list.size());
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Equipment eq : list) {
            model.addRow(new Object[]{
                    eq.getEquipmentId(),
                    eq.getEquipmentName(),
                    String.format("%,.0f", eq.getValue()),
                    Formats.text(eq.getPurchaseDate()),
                    eq.getStatus(),
                    ""
            });
        }
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String statusFilter = (String) view.getCbFilterStatus().getSelectedItem();

        List<Equipment> filtered = fetchAll().stream()
                .filter(eq -> {
                    boolean matchQ = q.isEmpty()
                            || eq.getEquipmentId().toLowerCase().contains(q)
                            || eq.getEquipmentName().toLowerCase().contains(q);

                    boolean matchStatus = "Tất cả".equals(statusFilter)
                            || eq.getStatus().equalsIgnoreCase(statusFilter);

                    return matchQ && matchStatus;
                })
                .toList();

        loadData(filtered);
    }

    private Equipment findEquipmentById(String id) {
        for (Equipment eq : fetchAll()) {
            if (eq.getEquipmentId().equalsIgnoreCase(id)) return eq;
        }
        return null;
    }

    private void showEquipmentDialog(Equipment eqToEdit) {
        boolean isEdit = (eqToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa thiết bị" : "Thêm thiết bị mới", true);
        dialog.setSize(640, 390);
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

        JComboBox<String> cbStatus = new JComboBox<>(Equipment.STATUSES.toArray(new String[0]));
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(eqToEdit.getEquipmentId());
            txtId.setEditable(false);
            txtName.setText(eqToEdit.getEquipmentName());
            txtValue.setText(eqToEdit.getValue().toPlainString());
            txtPurchaseDate.setText(Formats.text(eqToEdit.getPurchaseDate()));
            cbStatus.setSelectedItem(eqToEdit.getStatus());
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            txtValue.setText("25000000");
            txtPurchaseDate.setText(LocalDate.now().toString());
        }

        addFormRow(panel, gbc, 0, "Mã thiết bị (*):", txtId);
        addFormRow(panel, gbc, 1, "Tên thiết bị (*):", txtName);
        addFormRow(panel, gbc, 2, "Giá trị (VNĐ) (*):", txtValue);
        addFormRow(panel, gbc, 3, "Ngày mua (*):", txtPurchaseDate);
        addFormRow(panel, gbc, 4, "Trạng thái (*):", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String valStr = txtValue.getText().trim();
            String pDateText = txtPurchaseDate.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (id.isEmpty() || name.isEmpty() || valStr.isEmpty() || pDateText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin thiết bị!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate pDate;
            try {
                pDate = Formats.parseDate(pDateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal val;
            try {
                val = new BigDecimal(valStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá trị thiết bị phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                eqToEdit.setEquipmentName(name);
                eqToEdit.setValue(val);
                eqToEdit.setPurchaseDate(pDate);
                eqToEdit.setStatus(status);
                if (!DbAction.run(dialog, () -> dao.update(eqToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Equipment newEq = new Equipment(id, name, val, pDate, status);
                if (!DbAction.run(dialog, () -> dao.insert(newEq))) return;
                JOptionPane.showMessageDialog(dialog, "Thêm mới thiết bị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            handleFilter();
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