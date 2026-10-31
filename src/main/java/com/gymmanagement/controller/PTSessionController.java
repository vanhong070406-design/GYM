package com.gymmanagement.controller;

import com.gymmanagement.data.SampleData;
import com.gymmanagement.model.Area;
import com.gymmanagement.model.PTSession;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.view.PTSessionView;
import com.gymmanagement.view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller điều phối chức năng Quản lý lịch tập PT
 */
public class PTSessionController {
    private final PTSessionView view;
    private final SampleData data;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PTSessionController(PTSessionView view) {
        this.view = view;
        this.data = SampleData.getInstance();
        initListeners();
        loadData(data.getPtSessions());
    }

    private void initListeners() {
        view.getBtnSearch().addActionListener(e -> handleFilter());
        view.getTxtSearch().addActionListener(e -> handleFilter());

        view.getBtnRefresh().addActionListener(e -> {
            view.getTxtSearch().setText("");
            view.getCbFilterStatus().setSelectedIndex(0);
            loadData(data.getPtSessions());
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
                data.getPtSessions().removeIf(s -> s.getSessionId().equalsIgnoreCase(sesId));
                loadData(data.getPtSessions());
                JOptionPane.showMessageDialog(view, "Xóa lịch tập PT thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void loadData(List<PTSession> list) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (PTSession s : list) {
            String trainerName = getTrainerName(s.getTrainerId());
            model.addRow(new Object[]{
                    s.getSessionId(),
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getStatus(),
                    s.getTrainerId(),
                    trainerName,
                    s.getAreaId()
            });
        }
    }

    private String getTrainerName(String trainerId) {
        for (Trainer t : data.getTrainers()) {
            if (t.getTrainerId().equalsIgnoreCase(trainerId)) return t.getFullName();
        }
        return "N/A";
    }

    private void handleFilter() {
        String q = view.getTxtSearch().getText().trim().toLowerCase();
        String statusFilter = (String) view.getCbFilterStatus().getSelectedItem();

        List<PTSession> filtered = data.getPtSessions().stream()
                .filter(s -> {
                    boolean matchQ = q.isEmpty()
                            || s.getSessionId().toLowerCase().contains(q)
                            || s.getTrainerId().toLowerCase().contains(q)
                            || s.getAreaId().toLowerCase().contains(q)
                            || getTrainerName(s.getTrainerId()).toLowerCase().contains(q);

                    boolean matchStatus = "Tất cả".equals(statusFilter)
                            || s.getStatus().equalsIgnoreCase(statusFilter);

                    return matchQ && matchStatus;
                })
                .toList();

        loadData(filtered);
    }

    private PTSession findSessionById(String id) {
        for (PTSession s : data.getPtSessions()) {
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
        for (Trainer t : data.getTrainers()) cbTrainer.addItem(t);
        cbTrainer.setFont(UITheme.FONT_REGULAR);
        cbTrainer.setBackground(Color.WHITE);

        JComboBox<Area> cbArea = new JComboBox<>();
        for (Area a : data.getAreas()) cbArea.addItem(a);
        cbArea.setFont(UITheme.FONT_REGULAR);
        cbArea.setBackground(Color.WHITE);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đã đặt", "Đang diễn ra", "Hoàn thành", "Đã hủy"});
        cbStatus.setFont(UITheme.FONT_REGULAR);
        cbStatus.setBackground(Color.WHITE);

        if (isEdit) {
            txtId.setText(sesToEdit.getSessionId());
            txtId.setEditable(false);
            txtStart.setText(sesToEdit.getStartTime());
            txtEnd.setText(sesToEdit.getEndTime());
            for (int i = 0; i < cbTrainer.getItemCount(); i++) {
                if (cbTrainer.getItemAt(i).getTrainerId().equalsIgnoreCase(sesToEdit.getTrainerId())) {
                    cbTrainer.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbArea.getItemCount(); i++) {
                if (cbArea.getItemAt(i).getAreaId().equalsIgnoreCase(sesToEdit.getAreaId())) {
                    cbArea.setSelectedIndex(i);
                    break;
                }
            }
            cbStatus.setSelectedItem(sesToEdit.getStatus());
        } else {
            txtId.setText("SES" + String.format("%03d", data.getPtSessions().size() + 1));
            LocalDateTime now = LocalDateTime.now();
            txtStart.setText(now.format(dtf));
            txtEnd.setText(now.plusHours(1).plusMinutes(30).format(dtf));
        }

        addFormRow(panel, gbc, 0, "Mã buổi tập (*):", txtId);
        addFormRow(panel, gbc, 1, "Thời gian bắt đầu (*):", txtStart);
        addFormRow(panel, gbc, 2, "Thời gian kết thúc (*):", txtEnd);
        addFormRow(panel, gbc, 3, "Huấn luyện viên PT (*):", cbTrainer);
        addFormRow(panel, gbc, 4, "Khu vực tập (*):", cbArea);
        addFormRow(panel, gbc, 5, "Trạng thái:", cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSave = UITheme.createPrimaryButton("Lưu thông tin");
        JButton btnCancel = UITheme.createSecondaryButton("Hủy bỏ");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String start = txtStart.getText().trim();
            String end = txtEnd.getText().trim();
            Trainer selTrainer = (Trainer) cbTrainer.getSelectedItem();
            Area selArea = (Area) cbArea.getSelectedItem();
            String status = (String) cbStatus.getSelectedItem();

            if (id.isEmpty() || start.isEmpty() || end.isEmpty() || selTrainer == null || selArea == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin lịch tập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                sesToEdit.setStartTime(start);
                sesToEdit.setEndTime(end);
                sesToEdit.setTrainerId(selTrainer.getTrainerId());
                sesToEdit.setAreaId(selArea.getAreaId());
                sesToEdit.setStatus(status);
                JOptionPane.showMessageDialog(dialog, "Cập nhật lịch tập PT thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (findSessionById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "Mã buổi tập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                PTSession newSes = new PTSession(id, start, end, status, selTrainer.getTrainerId(), selArea.getAreaId());
                data.getPtSessions().add(newSes);
                JOptionPane.showMessageDialog(dialog, "Đặt lịch tập PT mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            loadData(data.getPtSessions());
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
