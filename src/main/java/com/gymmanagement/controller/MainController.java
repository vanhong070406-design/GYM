package com.gymmanagement.controller;

import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.MainView;
import com.gymmanagement.view.PTSessionView;
import com.gymmanagement.view.TrainerView;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * Controller chính: chỉ khởi tạo các màn hình tài khoản được phép thấy, ẩn các nút vượt quyền.
 * Màn hình không có quyền không được tạo controller nên cũng không tải dữ liệu.
 */
public class MainController {
    private final MainView view;
    private final UserSession session;
    private String firstScreen;
    private Runnable firstScreenOpener;

    public MainController(MainView view, UserSession session) {
        this.view = view;
        this.session = session;
        view.setCurrentUser(session.getFullName() + " (" + session.getPositionName() + ")");
        initDashboard();
        initScreens();
        initActions();
        if (firstScreen != null) firstScreenOpener.run();
    }

    /** Dashboard mở khi bấm vào GYM FITNESS PRO, không còn mục menu riêng. */
    private void initDashboard() {
        if (!session.canView(MainView.CARD_DASHBOARD)) return;
        DashboardController c = new DashboardController(view.getDashboardView());
        Runnable open = () -> {
            c.loadData();
            view.showScreen(MainView.CARD_DASHBOARD, "TỔNG QUAN HỆ THỐNG (DASHBOARD)");
        };
        view.getBrandPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getBrandPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                open.run();
            }
        });
        firstScreen = MainView.CARD_DASHBOARD;
        firstScreenOpener = open;
    }

    private void initScreens() {
        screen(MainView.CARD_MEMBER, "QUẢN LÝ HỘI VIÊN", () -> {
            // Sửa/xóa nằm ở cột Thao tác của từng dòng; không có quyền quản lý thì ẩn cột đó và nút thêm
            view.getMemberView().setManageable(session.canManage(MainView.CARD_MEMBER));
            return new MemberController(view.getMemberView())::reload;
        });
        screen(MainView.CARD_PACKAGE, "QUẢN LÝ GÓI TẬP", () -> {
            view.getPackageView().setManageable(session.canManage(MainView.CARD_PACKAGE));
            return new PackageController(view.getPackageView())::reload;
        });
        screen(MainView.CARD_SUBSCRIPTION, "QUẢN LÝ ĐĂNG KÝ GÓI TẬP", () -> {
            view.getSubscriptionView().setManageable(session.canManage(MainView.CARD_SUBSCRIPTION));
            return new SubscriptionController(view.getSubscriptionView(), session)::reload;
        });
        screen(MainView.CARD_PAYMENT, "QUẢN LÝ HÓA ĐƠN & THANH TOÁN", () -> {
            view.getPaymentView().setManageable(session.canManage(MainView.CARD_PAYMENT));
            return new PaymentController(view.getPaymentView(), session)::reload;
        });
        screen(MainView.CARD_CHECKIN, "CHECK IN / CHECK OUT", () -> {
            view.getCheckinView().setManageable(session.canManage(MainView.CARD_CHECKIN));
            return new CheckinController(view.getCheckinView(), session)::reload;
        });
        initTrainerScreen();
        screen(MainView.CARD_STAFF, "QUẢN LÝ NHÂN VIÊN", () -> {
            // Sửa/xóa nằm ở cột Thao tác của từng dòng; không có quyền quản lý thì ẩn cột đó và nút thêm
            view.getStaffView().setManageable(session.canManage(MainView.CARD_STAFF));
            return new StaffController(view.getStaffView(), session)::reload;
        });
        initEquipmentScreen();
    }

    /**
     * Màn hình Huấn luyện viên PT gộp cả Lịch tập PT; mở được nếu có quyền xem một trong hai.
     * Nút của danh sách HLV theo quyền TRAINER, nút của bảng lịch tập theo quyền PTSESSION.
     */
    private void initTrainerScreen() {
        boolean canTrainer = session.canView(MainView.CARD_TRAINER);
        boolean canSession = session.canView(MainView.CARD_PTSESSION);
        if (!canTrainer && !canSession) {
            view.hideMenuItem(MainView.CARD_TRAINER);
            return;
        }

        TrainerView trainerView = view.getTrainerView();
        PTSessionView sessionView = trainerView.getDetailView().getSessionView();
        // Sửa/xóa nằm ở cột Thao tác của từng dòng; biểu tượng và nút thêm hiện theo quyền
        if (canTrainer) {
            trainerView.setManageable(session.canManage(MainView.CARD_TRAINER));
        }
        if (canSession) {
            sessionView.setManageable(session.canManage(MainView.CARD_PTSESSION));
        }
        String title = canTrainer ? "QUẢN LÝ HUẤN LUYỆN VIÊN" : "LỊCH TẬP PT";
        register(MainView.CARD_TRAINER, title, new TrainerScreenController(trainerView, session)::reload);
    }

    /** Màn hình Thiết bị gồm tab Danh sách thiết bị và tab Bảo trì; tab nào không có quyền thì bị gỡ. */
    private void initEquipmentScreen() {
        boolean canEquipment = session.canView(MainView.CARD_EQUIPMENT);
        boolean canMaintenance = session.canView(MainView.CARD_MAINTENANCE);
        if (!canEquipment && !canMaintenance) {
            view.hideMenuItem(MainView.CARD_EQUIPMENT);
            return;
        }

        List<Runnable> reloads = new ArrayList<>();
        if (canEquipment) {
            // Sửa/xóa nằm ở cột Thao tác của từng dòng; không có quyền quản lý thì ẩn cột đó và nút thêm
            boolean canManageEquipment = session.canManage(MainView.CARD_EQUIPMENT);
            view.getEquipmentView().getBtnAdd().setVisible(canManageEquipment);
            view.getEquipmentView().setManageable(canManageEquipment);
            reloads.add(new EquipmentController(view.getEquipmentView())::reload);
        } else {
            view.getEquipmentTabs().remove(view.getEquipmentView());
        }
        if (canMaintenance) {
            // Có quyền quản lý: thêm, đổi trạng thái, sửa, xóa; chỉ xem thì ẩn nút thêm và cột Thao tác
            boolean canManageMaintenance = session.canManage(MainView.CARD_MAINTENANCE);
            view.getMaintenanceView().getBtnAdd().setVisible(canManageMaintenance);
            view.getMaintenanceView().setManageable(canManageMaintenance);
            reloads.add(new MaintenanceController(view.getMaintenanceView())::reload);
        } else {
            view.getEquipmentTabs().remove(view.getMaintenanceView());
        }
        register(MainView.CARD_EQUIPMENT, "QUẢN LÝ THIẾT BỊ PHÒNG TẬP", () -> reloads.forEach(Runnable::run));
    }

    /** Có quyền thì tạo controller và nối menu; không có thì ẩn menu. */
    private void screen(String card, String title, Supplier<Runnable> createController) {
        if (!session.canView(card)) {
            view.hideMenuItem(card);
            return;
        }
        register(card, title, createController.get());
    }

    private void register(String card, String title, Runnable reload) {
        Runnable open = () -> {
            reload.run();
            view.showScreen(card, title);
        };
        view.getSidebarButtons().get(card).addActionListener(e -> open.run());
        if (firstScreen == null) {
            firstScreen = card;
            firstScreenOpener = open;
        }
    }

    private void initActions() {
        view.getBtnChangePassword().addActionListener(e -> PasswordDialog.show(view, session.getUsername(), false));

        view.getBtnLogout().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                view.dispose();
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.setVisible(true);
            }
        });
    }
}