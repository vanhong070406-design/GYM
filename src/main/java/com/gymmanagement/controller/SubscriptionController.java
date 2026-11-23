package com.gymmanagement.controller;

import com.gymmanagement.dao.MemberDao;
import com.gymmanagement.dao.PackageDao;
import com.gymmanagement.dao.PaymentDao;
import com.gymmanagement.dao.SubscriptionDao;
import com.gymmanagement.model.Member;
import com.gymmanagement.model.PackageModel;
import com.gymmanagement.model.Payment;
import com.gymmanagement.model.Subscription;
import com.gymmanagement.model.Subscription.State;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.ActionColumn;
import com.gymmanagement.view.SubscriptionView;
import com.gymmanagement.view.SubscriptionView.Cell;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller điều phối chức năng Đăng ký gói tập:
 * danh sách có số ngày còn lại và tình trạng thanh toán, gia hạn và thu tiền ngay trên từng dòng.
 */
public class SubscriptionController {
    private static final String[] PAYMENT_METHODS = {"Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"};

    private final SubscriptionView view;
    private final UserSession session;
    private final SubscriptionDao dao = new SubscriptionDao();
    private final MemberDao memberDao = new MemberDao();
    private final PackageDao packageDao = new PackageDao();
    private final PaymentDao paymentDao = new PaymentDao();
    /** Các đăng ký đang hiện trong bảng, cùng thứ tự với các dòng. */
    private List<Subscription> shown = List.of();

    public SubscriptionController(SubscriptionView view, UserSession session) {
        this.view = view;
        this.session = session;
        // Đã thu đủ thì biểu tượng thu tiền mờ đi
        view.getActionColumn().setDisabledWhen(
                (row, action) -> action == ActionColumn.Action.PAY && row < shown.size() && shown.get(row).isFullyPaid(),
                java.util.Map.of(ActionColumn.Action.PAY, "Đã thu đủ tiền"));
        initListeners();
        reload();
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleFilter());
        view.getTxtSearch().addActionListener(e -> handleFilter());
        view.getCbFilterStatus().addActionListener(e -> handleFilter());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            // Đổi về "Tất cả" thì listener của ô lọc đã tự tải lại; đang ở "Tất cả" sẵn thì tải lại ở đây
            if (view.getCbFilterStatus().getSelectedIndex() != 0) view.getCbFilterStatus().setSelectedIndex(0);
            else handleFilter();
        });

        view.getBtnAdd().addActionListener(e -> showSubscriptionDialog(null, null));

        // Cột Thao tác: gia hạn / thu tiền / sửa / xóa
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionColumn.Action action = view.getActionColumn().actionAt(e.getPoint());
                if (action == null || e.getClickCount() != 1) return;
                String subId = (String) view.getTableModel().getValueAt(view.getTable().rowAtPoint(e.getPoint()), 0);
                Subscription sub = findSubscriptionById(subId); // đọc lại từ database để số tiền đã thu là mới nhất
                if (sub == null) return;
                switch (action) {
                    case RENEW -> showSubscriptionDialog(null, sub);
                    case PAY -> showPaymentDialog(sub);
                    case EDIT -> showSubscriptionDialog(sub, null);
                    case DELETE -> confirmDelete(sub);
                    default -> { }
                }
            }
        });
    }

    private List<Subscription> fetchAll() {
        return DbAction.fetch(view, () -> {
            dao.expireOverdue();
            return dao.findAll();
        }, List.of());
    }

    public void reload() {
        handleFilter();
    }

    private void handleFilter() {
        LocalDate today = LocalDate.now();
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String filter = (String) view.getCbFilterStatus().getSelectedItem();
        List<Subscription> all = fetchAll();

        List<Subscription> filtered = all.stream()
                .filter(s -> q.isEmpty()
                        || s.getSubscriptionId().toLowerCase().contains(q)
                        || s.getMemberId().toLowerCase().contains(q)
                        || s.getMemberName().toLowerCase().contains(q)
                        || s.getPackageId().toLowerCase().contains(q)
                        || s.getPackageName().toLowerCase().contains(q))
                .filter(s -> matchesFilter(s, filter, today))
                .toList();

        loadData(filtered, today);
        int active = (int) all.stream().filter(s -> isRunning(s, today)).count();
        int expiring = (int) all.stream().filter(s -> s.state(today) == State.EXPIRING).count();
        int unpaid = (int) all.stream().filter(s -> !s.isFullyPaid()).count();
        view.setSummary(filtered.size(), active, expiring, unpaid);
    }

    /** Đang hoạt động gồm cả các gói sắp hết hạn. */
    private static boolean isRunning(Subscription s, LocalDate today) {
        State st = s.state(today);
        return st == State.ACTIVE || st == State.EXPIRING;
    }

    private static boolean matchesFilter(Subscription s, String filter, LocalDate today) {
        if (filter == null) return true;
        return switch (filter) {
            case SubscriptionView.FILTER_ACTIVE -> isRunning(s, today);
            case SubscriptionView.FILTER_EXPIRING -> s.state(today) == State.EXPIRING;
            case SubscriptionView.FILTER_EXPIRED -> s.state(today) == State.EXPIRED;
            case SubscriptionView.FILTER_PAUSED -> s.state(today) == State.PAUSED;
            case SubscriptionView.FILTER_UNPAID -> !s.isFullyPaid();
            default -> true;
        };
    }

    private void loadData(List<Subscription> list, LocalDate today) {
        shown = new ArrayList<>(list);
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Subscription s : list) {
            model.addRow(new Object[]{
                    s.getSubscriptionId(),
                    new Cell(s.getMemberName(), null, s.getMemberId() + " – " + s.getMemberName()),
                    new Cell(s.getPackageName(), null,
                            s.getPackageId() + " · " + money(s.getPrice()) + " đ · " + s.getDurationDays() + " ngày"),
                    Formats.text(s.getStartDate()),
                    Formats.text(s.getEndDate()),
                    remainingCell(s, today),
                    paymentCell(s),
                    "" // cột Thao tác: chỉ vẽ biểu tượng
            });
        }
    }

    private static Cell remainingCell(Subscription s, LocalDate today) {
        long left = s.daysLeft(today);
        String endInfo = "Kết thúc " + Formats.text(s.getEndDate());
        if (s.getStartDate().isAfter(today) && s.state(today) != State.PAUSED && s.state(today) != State.EXPIRED) {
            long wait = java.time.temporal.ChronoUnit.DAYS.between(today, s.getStartDate());
            return new Cell("Chưa bắt đầu", UITheme.PRIMARY_HOVER, "Bắt đầu sau " + wait + " ngày (" + Formats.text(s.getStartDate()) + ")");
        }
        return switch (s.state(today)) {
            case PAUSED -> new Cell("Tạm dừng", UITheme.TEXT_MUTED, endInfo);
            case EXPIRED -> left < 0
                    ? new Cell("Quá hạn " + (-left) + " ngày", UITheme.DANGER, endInfo + ", hết hạn " + (-left) + " ngày trước")
                    : new Cell("Hết hạn", UITheme.DANGER, endInfo);
            case EXPIRING -> new Cell(left == 0 ? "Hết hạn hôm nay" : "Còn " + left + " ngày", UITheme.WARNING, endInfo);
            case ACTIVE -> new Cell("Còn " + left + " ngày", UITheme.SUCCESS, endInfo);
        };
    }

    private static Cell paymentCell(Subscription s) {
        String detail = "Đã thu " + money(s.getPaidAmount()) + " / " + money(s.getPrice()) + " đ";
        if (s.isFullyPaid()) return new Cell("Đã thu đủ", UITheme.SUCCESS, detail);
        if (s.getPaidAmount().signum() == 0) return new Cell("Chưa thu", UITheme.DANGER, detail);
        return new Cell("Còn nợ " + money(s.remaining()), UITheme.WARNING, detail);
    }

    private static String money(BigDecimal v) {
        return String.format("%,.0f", v);
    }

    private Subscription findSubscriptionById(String id) {
        for (Subscription s : fetchAll()) {
            if (s.getSubscriptionId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void confirmDelete(Subscription sub) {
        // Còn hóa đơn thì database không cho xóa; báo trước bằng lời dễ hiểu thay vì lỗi khóa ngoại
        List<String> payments = DbAction.fetch(view, () -> dao.findPaymentIds(sub.getSubscriptionId()), null);
        if (payments == null) return;
        if (!payments.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    sub.getSubscriptionId() + " đã có hóa đơn " + String.join(", ", payments) + ".\n"
                            + "Hãy xóa hóa đơn ở màn Quản lý thanh toán trước khi xóa đăng ký.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa đăng ký " + sub.getSubscriptionId() + " của " + sub.getMemberName() + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(sub.getSubscriptionId()))) return;
            handleFilter();
            JOptionPane.showMessageDialog(view, "Xóa đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Thu tiền cho một đăng ký: lập hóa đơn, mặc định số tiền còn nợ, lễ tân thu là người đang đăng nhập. */
    private void showPaymentDialog(Subscription sub) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Thu tiền " + sub.getSubscriptionId(), true);
        dialog.setSize(640, 360);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtAmount = UITheme.createTextField(15);
        txtAmount.setText(sub.remaining().toBigInteger().toString());
        JComboBox<String> cbMethod = createMethodCombo();

        addFormRow(panel, gbc, 0, "Hội viên:", infoLabel(sub.getMemberId() + " – " + sub.getMemberName()));
        addFormRow(panel, gbc, 1, "Gói tập:", infoLabel(sub.getPackageName()));
        addFormRow(panel, gbc, 2, "Giá / đã thu:", infoLabel(money(sub.getPrice()) + " đ / " + money(sub.getPaidAmount()) + " đ"));
        addFormRow(panel, gbc, 3, "Số tiền thu (*):", txtAmount);
        addFormRow(panel, gbc, 4, "Phương thức:", cbMethod);
        addFormRow(panel, gbc, 5, "Lễ tân thu:", infoLabel(session.getFullName()));

        JButton btnSave = UITheme.createPrimaryButton("Lập hóa đơn");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            BigDecimal amount = parseMoney(txtAmount.getText());
            if (amount == null || amount.signum() <= 0) {
                JOptionPane.showMessageDialog(dialog, "Số tiền phải là số lớn hơn 0.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount.compareTo(sub.remaining()) > 0) {
                int ok = JOptionPane.showConfirmDialog(dialog,
                        "Số tiền " + money(amount) + " đ lớn hơn số còn nợ " + money(sub.remaining()) + " đ. Vẫn lập hóa đơn?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
            }
            String payId = DbAction.fetch(dialog, paymentDao::nextId, null);
            if (payId == null) return;
            Payment pay = new Payment(payId, LocalDate.now(), (String) cbMethod.getSelectedItem(), amount,
                    sub.getSubscriptionId(), session.getStaffId());
            if (!DbAction.run(dialog, () -> paymentDao.insert(pay))) return;
            JOptionPane.showMessageDialog(dialog, "Đã lập hóa đơn " + payId + " (" + money(amount) + " đ).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            handleFilter();
            dialog.dispose();
        });

        addButtons(panel, gbc, 6, btnCancel, btnSave);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    /**
     * Hộp thoại dùng chung cho 3 việc:
     * subToEdit != null: sửa; renewFrom != null: gia hạn (đăng ký mới nối tiếp gói cũ); cả hai null: đăng ký mới.
     * Ngày kết thúc tự tính lại khi đổi gói hoặc ngày bắt đầu. Đăng ký mới / gia hạn có thể thu tiền ngay.
     */
    private void showSubscriptionDialog(Subscription subToEdit, Subscription renewFrom) {
        boolean isEdit = subToEdit != null;
        boolean isRenew = renewFrom != null;
        LocalDate today = LocalDate.now();
        String title = isEdit ? "Chỉnh sửa đăng ký " + subToEdit.getSubscriptionId()
                : isRenew ? "Gia hạn " + renewFrom.getSubscriptionId() + " – " + renewFrom.getMemberName()
                : "Đăng ký gói tập mới";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), title, true);
        dialog.setSize(700, isEdit ? 430 : 500);
        dialog.setLocationRelativeTo(view);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Subscription> allSubs = fetchAll();

        JTextField txtId = UITheme.createTextField(15);
        txtId.setEditable(false);
        JComboBox<Member> cbMember = new JComboBox<>();
        for (Member m : DbAction.fetch(view, memberDao::findAll, List.<Member>of())) cbMember.addItem(m);
        cbMember.setFont(UITheme.FONT_REGULAR);
        cbMember.setBackground(Color.WHITE);

        JComboBox<PackageModel> cbPackage = new JComboBox<>();
        for (PackageModel p : DbAction.fetch(view, packageDao::findAll, List.<PackageModel>of())) cbPackage.addItem(p);
        cbPackage.setFont(UITheme.FONT_REGULAR);
        cbPackage.setBackground(Color.WHITE);

        JTextField txtStartDate = UITheme.createTextField(15);
        JTextField txtEndDate = UITheme.createTextField(15);
        JComboBox<String> cbStatus = new JComboBox<>(Subscription.STATUSES.toArray(new String[0]));
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        JLabel lblNote = new JLabel(" ");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.TEXT_MUTED);

        JCheckBox chkPay = new JCheckBox("Thu tiền ngay");
        chkPay.setFont(UITheme.FONT_REGULAR);
        chkPay.setOpaque(false);
        chkPay.setSelected(true);
        JComboBox<String> cbMethod = createMethodCombo();
        chkPay.addActionListener(e -> cbMethod.setEnabled(chkPay.isSelected()));
        cbMethod.setEnabled(chkPay.isSelected());

        // Ngày kết thúc = bắt đầu + số ngày của gói; tính lại mỗi khi đổi gói hoặc sửa ngày bắt đầu
        Runnable recalcEnd = () -> {
            PackageModel pkg = (PackageModel) cbPackage.getSelectedItem();
            if (pkg == null) return;
            try {
                txtEndDate.setText(SubscriptionRules.endFor(LocalDate.parse(txtStartDate.getText().trim()), pkg.getDuration()).toString());
            } catch (DateTimeParseException ignored) {
                // đang gõ dở ngày bắt đầu: giữ nguyên ngày kết thúc
            }
            chkPay.setText("Thu tiền ngay " + money(pkg.getPrice()) + " đ");
        };
        // Đăng ký mới: nếu hội viên còn gói đang chạy thì bắt đầu ngay sau gói đó để hai gói không chồng nhau
        Runnable suggestStart = () -> {
            Member m = (Member) cbMember.getSelectedItem();
            if (m == null) return;
            Optional<Subscription> running = SubscriptionRules.latestRunning(allSubs, m.getMemberId(), today, null);
            if (running.isPresent()) {
                Subscription r = running.get();
                txtStartDate.setText(r.getEndDate().plusDays(1).toString());
                lblNote.setText("Đang có " + r.getSubscriptionId() + " (" + r.getPackageName() + ") đến "
                        + Formats.text(r.getEndDate()) + " → bắt đầu ngay sau đó.");
            } else {
                txtStartDate.setText(today.toString());
                lblNote.setText(" ");
            }
        };

        if (isEdit) {
            txtId.setText(subToEdit.getSubscriptionId());
            selectMember(cbMember, subToEdit.getMemberId());
            selectPackage(cbPackage, subToEdit.getPackageId());
            txtStartDate.setText(Formats.text(subToEdit.getStartDate()));
            txtEndDate.setText(Formats.text(subToEdit.getEndDate()));
            cbStatus.setSelectedItem(subToEdit.getStatus());
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            if (isRenew) {
                selectMember(cbMember, renewFrom.getMemberId());
                cbMember.setEnabled(false); // gia hạn cho đúng hội viên của gói cũ
                selectPackage(cbPackage, renewFrom.getPackageId());
                txtStartDate.setText(SubscriptionRules.renewStart(renewFrom, today).toString());
                lblNote.setText(renewFrom.state(today) == State.EXPIRED
                        ? "Gói cũ đã hết hạn → bắt đầu từ hôm nay."
                        : "Nối tiếp ngay sau khi " + renewFrom.getSubscriptionId() + " kết thúc (" + Formats.text(renewFrom.getEndDate()) + ").");
            } else {
                suggestStart.run();
                cbMember.addActionListener(e -> {
                    suggestStart.run();
                    recalcEnd.run();
                });
            }
            recalcEnd.run();
        }

        cbPackage.addActionListener(e -> recalcEnd.run());
        txtStartDate.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcEnd.run(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcEnd.run(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcEnd.run(); }
        });

        int row = 0;
        addFormRow(panel, gbc, row++, "Mã đăng ký:", txtId);
        addFormRow(panel, gbc, row++, "Hội viên (*):", cbMember);
        addFormRow(panel, gbc, row++, "Gói tập (*):", cbPackage);
        addFormRow(panel, gbc, row++, "Ngày bắt đầu (*):", txtStartDate);
        addFormRow(panel, gbc, row++, "", lblNote);
        addFormRow(panel, gbc, row++, "Ngày kết thúc (*):", txtEndDate);
        if (isEdit) {
            addFormRow(panel, gbc, row++, "Trạng thái:", cbStatus);
        } else {
            addFormRow(panel, gbc, row++, "", chkPay);
            addFormRow(panel, gbc, row++, "Phương thức:", cbMethod);
        }

        JButton btnSave = UITheme.createPrimaryButton(isEdit ? "Lưu thông tin" : isRenew ? "Gia hạn" : "Đăng ký");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            Member selMember = (Member) cbMember.getSelectedItem();
            PackageModel selPkg = (PackageModel) cbPackage.getSelectedItem();
            String startText = txtStartDate.getText().trim();
            String endText = txtEndDate.getText().trim();

            if (id.isEmpty() || selMember == null || selPkg == null || startText.isEmpty() || endText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin đăng ký!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate;
            LocalDate endDate;
            try {
                startDate = Formats.parseDate(startText);
                endDate = Formats.parseDate(endText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày phải có dạng yyyy-MM-dd và là ngày có thật!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(dialog, "Ngày kết thúc không được trước ngày bắt đầu.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                // Đổi sang gói khác thì chốt giá của gói mới; giữ gói cũ thì giữ giá đã chốt lúc đăng ký
                if (!selPkg.getPackageId().equalsIgnoreCase(subToEdit.getPackageId())) subToEdit.setPrice(selPkg.getPrice());
                subToEdit.setMemberId(selMember.getMemberId());
                subToEdit.setPackageId(selPkg.getPackageId());
                subToEdit.setStartDate(startDate);
                subToEdit.setEndDate(endDate);
                subToEdit.setStatus((String) cbStatus.getSelectedItem());
                if (!DbAction.run(dialog, () -> dao.update(subToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Subscription newSub = new Subscription(id, startDate, endDate, Subscription.STATUS_ACTIVE,
                        selMember.getMemberId(), selPkg.getPackageId());
                newSub.setPrice(selPkg.getPrice()); // chốt giá gói tại thời điểm đăng ký / gia hạn
                Payment pay = null;
                if (chkPay.isSelected()) {
                    String payId = DbAction.fetch(dialog, paymentDao::nextId, null);
                    if (payId == null) return;
                    pay = new Payment(payId, today, (String) cbMethod.getSelectedItem(), selPkg.getPrice(), id, session.getStaffId());
                }
                Payment finalPay = pay;
                if (!DbAction.run(dialog, () -> dao.insertWithPayment(newSub, finalPay))) return;
                String done = (isRenew ? "Gia hạn thành công: " : "Đăng ký thành công: ") + id
                        + (finalPay != null ? "\nĐã lập hóa đơn " + finalPay.getPaymentId() + " (" + money(finalPay.getAmount()) + " đ)." : "");
                JOptionPane.showMessageDialog(dialog, done, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            handleFilter();
            dialog.dispose();
        });

        addButtons(panel, gbc, row, btnCancel, btnSave);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private static void selectMember(JComboBox<Member> cb, String memberId) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getMemberId().equalsIgnoreCase(memberId)) {
                cb.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void selectPackage(JComboBox<PackageModel> cb, String packageId) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getPackageId().equalsIgnoreCase(packageId)) {
                cb.setSelectedIndex(i);
                return;
            }
        }
    }

    private static JComboBox<String> createMethodCombo() {
        JComboBox<String> cb = new JComboBox<>(PAYMENT_METHODS);
        cb.setFont(UITheme.FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    private static JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_REGULAR);
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    /** "1.350.000", "1,350,000" hay "1350000" đều thành 1350000; không có chữ số nào thì null. */
    static BigDecimal parseMoney(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : new BigDecimal(digits);
    }

    private static void addButtons(JPanel panel, GridBagConstraints gbc, int row, JButton cancel, JButton save) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(cancel);
        btnPanel.add(save);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(btnPanel, gbc);
        gbc.insets = new Insets(6, 6, 6, 6);
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
