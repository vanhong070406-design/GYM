package com.gymmanagement.controller;

import com.gymmanagement.dao.MemberDao;
import com.gymmanagement.dao.PTSessionDao;
import com.gymmanagement.dao.TrainerDao;
import com.gymmanagement.model.Member;
import com.gymmanagement.model.PTSession;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.MainView;
import com.gymmanagement.view.PTSessionView;
import com.gymmanagement.view.UITheme;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Controller điều phối chức năng Quản lý lịch tập PT
 */
public class PTSessionController {
    private final PTSessionView view;
    private final UserSession session;
    private final MemberDao memberDao = new MemberDao();
    private final PTSessionDao dao = new PTSessionDao();
    private final TrainerDao trainerDao = new TrainerDao();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PTSessionController(PTSessionView view, UserSession session) {
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

        view.getBtnAdd().addActionListener(e -> showSessionDialog(null));

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một lịch tập để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sesId = (String) view.getTableModel().getValueAt(row, 0);
            PTSession session = findSessionById(sesId);
            if (session != null) {
                showSessionDialog(session);
            }
        });

        view.getBtnDelete().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một lịch tập cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sesId = (String) view.getTableModel().getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc muốn xóa lịch tập: " + sesId + " không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!DbAction.run(view, () -> dao.delete(sesId))) return;
                loadData(fetchAll());
                JOptionPane.showMessageDialog(view, "Xóa lịch tập PT thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private List<PTSession> fetchAll() {
        List<PTSession> all = DbAction.fetch(view, dao::findAll, List.of());
        if (!session.isOwnOnly(MainView.CARD_PTSESSION)) return all;
        // HLV chỉ thấy buổi tập của mình
        return all.stream().filter(s -> s.getTrainerId().equals(session.getTrainerId())).toList();
    }

    public void reload() {
        handleFilter();
    }

    public void loadData(List<PTSession> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (PTSession s : list) {
            model.addRow(new Object[]{
                    s.getSessionId(),
                    Formats.text(s.getStartTime()),
                    Formats.text(s.getEndTime()),
                    s.getStatus(),
                    s.getTrainerId(),
                    s.getTrainerName(),
                    s.getMemberId(),
                    s.getMemberName()
            });
        }
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String statusFilter = (String) view.getCbFilterStatus().getSelectedItem();

        List<PTSession> filtered = fetchAll().stream()
                .filter(s -> {
                    boolean matchQ = q.isEmpty()
                            || s.getSessionId().toLowerCase().contains(q)
                            || s.getTrainerId().toLowerCase().contains(q)
                            || s.getTrainerName().toLowerCase().contains(q)
                            || s.getMemberId().toLowerCase().contains(q)
                            || s.getMemberName().toLowerCase().contains(q);

                    boolean matchStatus = "Tất cả".equals(statusFilter)
                            || s.getStatus().equalsIgnoreCase(statusFilter);

                    return matchQ && matchStatus;
                })
                .toList();

        loadData(filtered);
    }

    private PTSession findSessionById(String id) {
        for (PTSession s : fetchAll()) {
            if (s.getSessionId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void showSessionDialog(PTSession sesToEdit) {
        boolean isEdit = (sesToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view),
                isEdit ? "Chỉnh sửa lịch tập PT" : "Đặt lịch tập PT mới", true);
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
        JTextField txtStart = UITheme.createTextField(15);
        JTextField txtEnd = UITheme.createTextField(15);

        JComboBox<Trainer> cbTrainer = new JComboBox<>();
        for (Trainer t : DbAction.fetch(view, trainerDao::findAll, List.<Trainer>of())) cbTrainer.addItem(t);
        cbTrainer.setFont(UITheme.FONT_REGULAR);
        cbTrainer.setBackground(Color.WHITE);

        JComboBox<Member> cbMember = new JComboBox<>();
        if (!session.isOwnOnly(MainView.CARD_PTSESSION)) {
            for (Member m : DbAction.fetch(view, memberDao::findAll, List.<Member>of())) cbMember.addItem(m);
        } else if (isEdit) {
            // HLV không có quyền xem danh sách hội viên, chỉ hiện hội viên của buổi này
            Member current = new Member();
            current.setMemberId(sesToEdit.getMemberId());
            current.setFullName(sesToEdit.getMemberName());
            cbMember.addItem(current);
        }
        cbMember.setFont(UITheme.FONT_REGULAR);
        cbMember.setBackground(Color.WHITE);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đã đặt", "Đang diễn ra", "Hoàn thành", "Đã hủy"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(sesToEdit.getSessionId());
            txtId.setEditable(false);
            txtStart.setText(Formats.text(sesToEdit.getStartTime()));
            txtEnd.setText(Formats.text(sesToEdit.getEndTime()));
            for (int i = 0; i < cbTrainer.getItemCount(); i++) {
                if (cbTrainer.getItemAt(i).getTrainerId().equalsIgnoreCase(sesToEdit.getTrainerId())) {
                    cbTrainer.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbMember.getItemCount(); i++) {
                if (cbMember.getItemAt(i).getMemberId().equalsIgnoreCase(sesToEdit.getMemberId())) {
                    cbMember.setSelectedIndex(i);
                    break;
                }
            }
            cbStatus.setSelectedItem(sesToEdit.getStatus());
            if (session.isOwnOnly(MainView.CARD_PTSESSION)) {
                // HLV chỉ cập nhật trạng thái; thời gian và phân công do Lễ tân/Quản lý sắp xếp
                txtStart.setEditable(false);
                txtEnd.setEditable(false);
                cbTrainer.setEnabled(false);
                cbMember.setEnabled(false);
            }
        } else {
            txtId.setText(DbAction.fetch(view, dao::nextId, ""));
            LocalDateTime now = LocalDateTime.now();
            txtStart.setText(now.format(dtf));
            txtEnd.setText(now.plusHours(1).plusMinutes(30).format(dtf));
        }

        addFormRow(panel, gbc, 0, "Mã buổi tập (*):", txtId);
        addFormRow(panel, gbc, 1, "Thời gian bắt đầu (*):", txtStart);
        addFormRow(panel, gbc, 2, "Thời gian kết thúc (*):", txtEnd);
        addFormRow(panel, gbc, 3, "Huấn luyện viên PT (*):", cbTrainer);
        addFormRow(panel, gbc, 4, "Hội viên (*):", cbMember);
        addFormRow(panel, gbc, 5, "Trạng thái:", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String startText = txtStart.getText().trim();
            String endText = txtEnd.getText().trim();
            Trainer selTrainer = (Trainer) cbTrainer.getSelectedItem();
            Member selMember = (Member) cbMember.getSelectedItem();
            String status = (String) cbStatus.getSelectedItem();

            if (id.isEmpty() || startText.isEmpty() || endText.isEmpty() || selTrainer == null || selMember == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin lịch tập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime start;
            LocalDateTime end;
            try {
                start = Formats.parseDateTime(startText);
                end = Formats.parseDateTime(endText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Thời gian phải có dạng yyyy-MM-dd HH:mm!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                sesToEdit.setStartTime(start);
                sesToEdit.setEndTime(end);
                sesToEdit.setTrainerId(selTrainer.getTrainerId());
                sesToEdit.setMemberId(selMember.getMemberId());
                sesToEdit.setStatus(status);
                if (!DbAction.run(dialog, () -> dao.update(sesToEdit))) return;
                JOptionPane.showMessageDialog(dialog, "Cập nhật lịch tập PT thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                PTSession newSes = new PTSession(id, start, end, status, selTrainer.getTrainerId(), selMember.getMemberId());
                if (!DbAction.run(dialog, () -> dao.insert(newSes))) return;
                JOptionPane.showMessageDialog(dialog, "Đặt lịch tập PT mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

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