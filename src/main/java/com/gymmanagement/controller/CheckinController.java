package com.gymmanagement.controller;

import com.gymmanagement.dao.CheckinDao;
import com.gymmanagement.dao.MemberDao;
import com.gymmanagement.dao.StaffDao;
import com.gymmanagement.model.Checkin;
import com.gymmanagement.model.Member;
import com.gymmanagement.model.Position;
import com.gymmanagement.model.Staff;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.ActionColumn;
import com.gymmanagement.view.CheckinView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller điều phối chức năng Quản lý Check-in
 */
public class CheckinController {
    private final CheckinView view;
    private final UserSession session;
    private final CheckinDao dao = new CheckinDao();
    private final MemberDao memberDao = new MemberDao();
    private final StaffDao staffDao = new StaffDao();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CheckinController(CheckinView view, UserSession session) {
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

        // Check-in mới
        view.getBtnCheckin().addActionListener(e -> showCheckinDialog());

        // Check-out
        // Cột Thao tác: check-out (chỉ bấm được khi chưa ra) và xóa
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionColumn.Action action = view.getActionColumn().actionAt(e.getPoint());
                if (action == null || e.getClickCount() != 1) return;
                String checkinId = (String) view.getTableModel().getValueAt(view.getTable().rowAtPoint(e.getPoint()), 0);
                if (action == ActionColumn.Action.CHECKOUT) checkout(checkinId);
                else if (action == ActionColumn.Action.DELETE) confirmDelete(checkinId);
            }
        });
    }

    private void checkout(String checkinId) {
        Checkin c = findCheckinById(checkinId);
        if (c == null) return;
        // Bảng có thể đã cũ (máy khác vừa check-out lượt này), nên vẫn kiểm tra lại với dữ liệu mới nhất
        if (c.getCheckoutTime() != null) {
            JOptionPane.showMessageDialog(view, "Lượt check-in này đã được check-out trước đó (" + Formats.text(c.getCheckoutTime()) + ")!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadData(fetchAll());
            return;
        }
        if (!DbAction.run(view, () -> dao.checkout(checkinId, LocalDateTime.now()))) return;
        loadData(fetchAll());
        JOptionPane.showMessageDialog(view, "Check-out thành công cho hội viên: " + c.getMemberId(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmDelete(String checkinId) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa lịch sử check-in: " + checkinId + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (!DbAction.run(view, () -> dao.delete(checkinId))) return;
            loadData(fetchAll());
            JOptionPane.showMessageDialog(view, "Xóa lượt check-in thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private List<Checkin> fetchAll() {
        return DbAction.fetch(view, dao::findAll, List.of());
    }

    public void reload() {
        handleSearch();
    }

    public void loadData(List<Checkin> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Checkin c : list) {
            model.addRow(new Object[]{
                    c.getCheckinId(),
                    c.getMemberId(),
                    c.getMemberName(),
                    Formats.text(c.getCheckinTime()),
                    c.getCheckoutTime() == null ? CheckinView.STILL_INSIDE : Formats.text(c.getCheckoutTime()),
                    c.getReceptionistId(),
                    "" // cột Thao tác: chỉ vẽ biểu tượng
            });
        }
        view.setCount(list.size(), (int) list.stream().filter(c -> c.getCheckoutTime() == null).count());
    }

    private void handleSearch() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadData(fetchAll());
            return;
        }

        List<Checkin> filtered = fetchAll().stream()
                .filter(c -> c.getCheckinId().toLowerCase().contains(q)
                        || c.getMemberId().toLowerCase().contains(q)
                        || c.getMemberName().toLowerCase().contains(q)
                        || c.getReceptionistId().toLowerCase().contains(q))
                .toList();

        loadData(filtered);
    }

    private Checkin findCheckinById(String id) {
        for (Checkin c : fetchAll()) {
            if (c.getCheckinId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    private void showCheckinDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Check-in Hội Viên Mới", true);
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
        txtId.setText(DbAction.fetch(view, dao::nextId, ""));

        JComboBox<Member> cbMember = new JComboBox<>();
        for (Member m : DbAction.fetch(view, memberDao::findAll, List.<Member>of())) cbMember.addItem(m);
        cbMember.setFont(UITheme.FONT_REGULAR);
        cbMember.setBackground(Color.WHITE);

        JTextField txtTime = UITheme.createTextField(15);
        txtTime.setText(LocalDateTime.now().format(dtf));

        JComboBox<Staff> cbReceptionist = new JComboBox<>();
        for (Staff st : DbAction.fetch(view, () -> staffDao.findByPosition(Position.RECEPTIONIST), List.<Staff>of())) {
            cbReceptionist.addItem(st);
            if (st.getStaffId().equals(session.getStaffId())) cbReceptionist.setSelectedItem(st); // Lễ tân đang đăng nhập
        }
        cbReceptionist.setFont(UITheme.FONT_REGULAR);
        cbReceptionist.setBackground(Color.WHITE);

        addFormRow(panel, gbc, 0, "Mã Check-in (*):", txtId);
        addFormRow(panel, gbc, 1, "Chọn hội viên (*):", cbMember);
        addFormRow(panel, gbc, 2, "Thời gian vào (*):", txtTime);
        addFormRow(panel, gbc, 3, "Lễ tân tiếp nhận (*):", cbReceptionist);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Xác nhận Check-in");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            Member selMember = (Member) cbMember.getSelectedItem();
            String timeText = txtTime.getText().trim();
            Staff selStaff = (Staff) cbReceptionist.getSelectedItem();

            if (id.isEmpty() || selMember == null || timeText.isEmpty() || selStaff == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime time;
            try {
                time = Formats.parseDateTime(timeText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Thời gian phải có dạng yyyy-MM-dd HH:mm!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Checkin newCheckin = new Checkin(id, time, null, selMember.getMemberId(), selStaff.getStaffId());
            if (!DbAction.run(dialog, () -> dao.insert(newCheckin))) return;
            JOptionPane.showMessageDialog(dialog, "Check-in thành công cho: " + selMember.getFullName(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            loadData(fetchAll());
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
