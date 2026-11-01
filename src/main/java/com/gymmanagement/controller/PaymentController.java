package com.gymmanagement.controller;

import com.gymmanagement.dao.PaymentDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.dao.SubscriptionDao;
import com.gymmanagement.model.Payment;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.model.Subscription;
import com.gymmanagement.view.PaymentView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối chức năng Quản lý thanh toán
 */
public class PaymentController {
    private final PaymentView view;
    private final UserSession session;
    private final PaymentDao dao = new PaymentDao();
    private final SubscriptionDao subscriptionDao = new SubscriptionDao();
    private final StaffDao staffDao = new StaffDao();

    public PaymentController(PaymentView view, UserSession session) {
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
            view.getCbFilterMethod().setSelectedIndex(0);
            loadData(fetchAll());
        });

        view.getBtnAdd().addActionListener(e -> showPaymentDialog());

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một hóa đơn cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String payId = (String) view.getTableModel().getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa hóa đơn thanh toán: " + payId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(payId))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<Payment> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleFilter();
    }

    public void loadData(List<Payment> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Payment p : list) {
            model.addRow(new Object[]{
                    p.getPaymentId(),
                    Formats.text(p.getPaymentDate()),
                    p.getPaymentMethod(),
                    String.format("%,.0f", p.getAmount()),
                    p.getSubscriptionId(),
                    p.getReceptionistId()
            });
        }
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String methodFilter = (String) view.getCbFilterMethod().getSelectedItem();

        List<Payment> filtered = fetchAll().stream()
                .filter(p -> {
                    boolean matchQ = q.isEmpty()
                            || p.getPaymentId().toLowerCase().contains(q)
                            || p.getSubscriptionId().toLowerCase().contains(q)
                            || p.getReceptionistId().toLowerCase().contains(q);

                    boolean matchMethod = "Tất cả".equals(methodFilter)
                            || p.getPaymentMethod().equalsIgnoreCase(methodFilter);

                    return matchQ && matchMethod;
                })
                .toList();

        loadData(filtered);
    }

    private void showPaymentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Lập hóa đơn thanh toán mới", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = UITheme.createTextField(15);
        txtId.setText(DbAction.fetch(view, dao::nextId, ""));

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(LocalDate.now().toString());

        JComboBox<String> cbMethod = new JComboBox<>(new String[]{"Chuyển khoản", "Tiền mặt", "Thẻ tín dụng"});
        cbMethod.setFont(UITheme.FONT_REGULAR);
        cbMethod.setBackground(Color.WHITE);

        JTextField txtAmount = UITheme.createTextField(15);
        txtAmount.setText("1000000");

        JComboBox<Subscription> cbSub = new JComboBox<>();
        for (Subscription s : DbAction.fetch(view, subscriptionDao::findAll, List.<Subscription>of())) cbSub.addItem(s);
        cbSub.setFont(UITheme.FONT_REGULAR);
        cbSub.setBackground(Color.WHITE);

        JComboBox<Staff> cbReceptionist = new JComboBox<>();
        for (Staff st : DbAction.fetch(view, () -> staffDao.findByPosition(Position.RECEPTIONIST), List.<Staff>of())) {
            cbReceptionist.addItem(st);
            if (st.getStaffId().equals(session.getStaffId())) cbReceptionist.setSelectedItem(st); // Lễ tân đang đăng nhập
        }
        cbReceptionist.setFont(UITheme.FONT_REGULAR);
        cbReceptionist.setBackground(Color.WHITE);

        addFormRow(panel, gbc, 0, "Mã hóa đơn (*):", txtId);
        addFormRow(panel, gbc, 1, "Ngày thanh toán (*):", txtDate);
        addFormRow(panel, gbc, 2, "Phương thức (*):", cbMethod);
        addFormRow(panel, gbc, 3, "Số tiền (VNĐ) (*):", txtAmount);
        addFormRow(panel, gbc, 4, "Mã đăng ký (*):", cbSub);
        addFormRow(panel, gbc, 5, "Lễ tân tiếp nhận (*):", cbReceptionist);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Xác nhận thanh toán");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String dateText = txtDate.getText().trim();
            String method = (String) cbMethod.getSelectedItem();
            String amountStr = txtAmount.getText().trim();
            Subscription selSub = (Subscription) cbSub.getSelectedItem();
            Staff selStaff = (Staff) cbReceptionist.getSelectedItem();

            if (id.isEmpty() || dateText.isEmpty() || amountStr.isEmpty() || selSub == null || selStaff == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ các trường!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal amount;
            LocalDate date;
            try {
                date = Formats.parseDate(dateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                amount = new BigDecimal(amountStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Số tiền không hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Payment newPay = new Payment(id, date, method, amount, selSub.getSubscriptionId(), selStaff.getStaffId());
            if (!DbAction.run(dialog, () -> dao.insert(newPay))) return;
            JOptionPane.showMessageDialog(dialog, "Lập hóa đơn thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            loadData(fetchAll());
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
