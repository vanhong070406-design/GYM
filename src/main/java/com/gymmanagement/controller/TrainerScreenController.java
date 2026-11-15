package com.gymmanagement.controller;

import com.gymmanagement.dao.PTSessionDao;
import com.gymmanagement.dao.TrainerDao;
import com.gymmanagement.model.PTSession;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.MainView;
import com.gymmanagement.view.TrainerDetailView;
import com.gymmanagement.view.TrainerView;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Điều phối màn hình Huấn luyện viên PT gộp: danh sách HLV và trang chi tiết có lịch tập.
 * Quyền TRAINER quyết định danh sách và nút thêm/sửa/xóa HLV; quyền PTSESSION quyết định bảng lịch tập.
 * Tài khoản không xem được danh sách HLV (tài khoản HLV) thì vào thẳng trang của mình, không có nút quay lại.
 */
public class TrainerScreenController {
    private final TrainerView view;
    private final TrainerDetailView detail;
    private final UserSession session;
    private final TrainerController trainers;      // null nếu không có quyền TRAINER
    private final PTSessionController sessions;    // null nếu không có quyền PTSESSION
    private final TrainerDao trainerDao = new TrainerDao();
    private final PTSessionDao sessionDao = new PTSessionDao();
    private String currentTrainerId;

    public TrainerScreenController(TrainerView view, UserSession session) {
        this.view = view;
        this.detail = view.getDetailView();
        this.session = session;
        this.trainers = session.canView(MainView.CARD_TRAINER) ? new TrainerController(view) : null;
        this.sessions = session.canView(MainView.CARD_PTSESSION) ? new PTSessionController(detail.getSessionView(), session) : null;

        detail.setSessionsVisible(sessions != null);
        view.getBtnAllSessions().setVisible(sessions != null);
        detail.getBtnBack().setVisible(trainers != null);

        if (trainers != null) trainers.setOnOpenDetail(this::showTrainer);
        if (sessions != null) sessions.setOnChange(this::refreshStats);
        view.getBtnAllSessions().addActionListener(e -> showAllSessions());
        detail.getBtnBack().addActionListener(e -> showList());
    }

    /** Gọi mỗi lần bấm menu. */
    public void reload() {
        if (trainers != null) {
            showList();
        } else if (session.getTrainerId() != null) {
            showTrainer(session.getTrainerId());
        } else {
            showAllSessions();
        }
    }

    private void showList() {
        trainers.reload();
        view.showList();
    }

    private void showTrainer(String trainerId) {
        Trainer t = DbAction.fetch(view, trainerDao::findAll, List.<Trainer>of()).stream()
                .filter(x -> x.getTrainerId().equalsIgnoreCase(trainerId))
                .findFirst().orElse(null);
        if (t == null) return;

        currentTrainerId = t.getTrainerId();
        detail.showTrainer(t.getTrainerId() + " – " + t.getFullName(), t.getPhone(),
                t.getExperienceYears() + " năm", String.join(", ", t.getSpecialties()));
        if (sessions != null) {
            sessions.showTrainer(currentTrainerId);
            refreshStats();
        }
        view.showDetail();
    }

    private void showAllSessions() {
        currentTrainerId = null;
        detail.showAllSessions("Lịch tập PT của tất cả huấn luyện viên");
        sessions.showTrainer(null);
        view.showDetail();
    }

    /** Thống kê trên trang chi tiết; ở chế độ tất cả HLV thì phần này đang ẩn nên bỏ qua. */
    private void refreshStats() {
        if (currentTrainerId == null) return;
        List<PTSession> list = DbAction.fetch(view, sessionDao::findAll, List.<PTSession>of()).stream()
                .filter(s -> s.getTrainerId().equalsIgnoreCase(currentTrainerId))
                .toList();
        LocalDateTime now = LocalDateTime.now();
        int completed = (int) list.stream().filter(s -> "Hoàn thành".equals(s.getStatus())).count();
        int cancelled = (int) list.stream().filter(s -> "Đã hủy".equals(s.getStatus())).count();
        int upcoming = (int) list.stream().filter(s -> "Đã đặt".equals(s.getStatus()) && s.getStartTime().isAfter(now)).count();
        detail.setStats(list.size(), completed, upcoming, cancelled);
    }
}
