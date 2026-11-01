package com.gymmanagement.controller;

import com.gymmanagement.model.UserSession;
import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.MainView;
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
        initScreens();
        initActions();
        if (firstScreen != null) firstScreenOpener.run();
    }

    private void initScreens() {
        screen(MainView.CARD_DASHBOARD, "TỔNG QUAN HỆ THỐNG (DASHBOARD)", () -> {
            DashboardController c = new DashboardController(view.getDashboardView());
            return c::loadData;
        });
        screen(MainView.CARD_MEMBER, "QUẢN LÝ HỘI VIÊN", () -> {
            buttons(MainView.CARD_MEMBER, view.getMemberView().getBtnAdd(), view.getMemberView().getBtnEdit(), view.getMemberView().getBtnDelete());
            return new MemberController(view.getMemberView())::reload;
        });
        screen(MainView.CARD_PACKAGE, "QUẢN LÝ GÓI TẬP", () -> {
            buttons(MainView.CARD_PACKAGE, view.getPackageView().getBtnAdd(), view.getPackageView().getBtnEdit(), view.getPackageView().getBtnDelete());
            return new PackageController(view.getPackageView())::reload;
        });
        screen(MainView.CARD_SUBSCRIPTION, "QUẢN LÝ ĐĂNG KÝ GÓI TẬP", () -> {
            buttons(MainView.CARD_SUBSCRIPTION, view.getSubscriptionView().getBtnAdd(), view.getSubscriptionView().getBtnEdit(), view.getSubscriptionView().getBtnDelete());
            return new SubscriptionController(view.getSubscriptionView())::reload;
        });
        screen(MainView.CARD_PAYMENT, "QUẢN LÝ HÓA ĐƠN & THANH TOÁN", () -> {
            buttons(MainView.CARD_PAYMENT, view.getPaymentView().getBtnAdd(), null, view.getPaymentView().getBtnDelete());
            return new PaymentController(view.getPaymentView(), session)::reload;
        });
        screen(MainView.CARD_CHECKIN, "QUẢN LÝ LỊCH SỬ CHECK-IN / CHECK-OUT", () -> {
            buttons(MainView.CARD_CHECKIN, view.getCheckinView().getBtnCheckin(), view.getCheckinView().getBtnCheckout(), view.getCheckinView().getBtnDelete());
            return new CheckinController(view.getCheckinView(), session)::reload;
        });
        screen(MainView.CARD_TRAINER, "QUẢN LÝ HUẤN LUYỆN VIÊN (PT)", () -> {
            buttons(MainView.CARD_TRAINER, view.getTrainerView().getBtnAdd(), view.getTrainerView().getBtnEdit(), view.getTrainerView().getBtnDelete());
            return new TrainerController(view.getTrainerView())::reload;
        });
        screen(MainView.CARD_PTSESSION, "QUẢN LÝ LỊCH TẬP PT", () -> {
            buttons(MainView.CARD_PTSESSION, view.getPtSessionView().getBtnAdd(), view.getPtSessionView().getBtnEdit(), view.getPtSessionView().getBtnDelete());
            return new PTSessionController(view.getPtSessionView(), session)::reload;
        });
        screen(MainView.CARD_STAFF, "QUẢN LÝ NHÂN VIÊN", () -> {
            buttons(MainView.CARD_STAFF, view.getStaffView().getBtnAdd(), view.getStaffView().getBtnEdit(), view.getStaffView().getBtnDelete());
            return new StaffController(view.getStaffView(), session)::reload;
        });
        screen(MainView.CARD_AREA, "QUẢN LÝ KHU VỰC PHÒNG GYM", () -> {
            buttons(MainView.CARD_AREA, view.getAreaView().getBtnAdd(), view.getAreaView().getBtnEdit(), view.getAreaView().getBtnDelete());
            return new AreaController(view.getAreaView())::reload;
        });
        screen(MainView.CARD_EQUIPMENT, "QUẢN LÝ THIẾT BỊ PHÒNG TẬP", () -> {
            buttons(MainView.CARD_EQUIPMENT, view.getEquipmentView().getBtnAdd(), view.getEquipmentView().getBtnEdit(), view.getEquipmentView().getBtnDelete());
            return new EquipmentController(view.getEquipmentView())::reload;
        });
        screen(MainView.CARD_MAINTENANCE, "QUẢN LÝ PHÂN CÔNG BẢO TRÌ THIẾT BỊ", () -> {
            buttons(MainView.CARD_MAINTENANCE, view.getMaintenanceView().getBtnAdd(), view.getMaintenanceView().getBtnEdit(), view.getMaintenanceView().getBtnDelete());
            return new MaintenanceController(view.getMaintenanceView(), session)::reload;
        });
        screen(MainView.CARD_CLEANING, "QUẢN LÝ PHÂN CÔNG VỆ SINH KHU VỰC", () -> {
            buttons(MainView.CARD_CLEANING, view.getAreaCleaningView().getBtnAdd(), view.getAreaCleaningView().getBtnEdit(), view.getAreaCleaningView().getBtnDelete());
            return new AreaCleaningController(view.getAreaCleaningView(), session)::reload;
        });
        screen(MainView.CARD_ACCOUNT, "QUẢN LÝ TÀI KHOẢN", () -> new AccountController(view.getAccountView(), session)::reload);
    }

    /** Có quyền thì tạo controller và nối menu; không có thì ẩn menu. */
    private void screen(String card, String title, Supplier<Runnable> createController) {
        if (!session.canView(card)) {
            view.hideMenuItem(card);
            return;
        }
        Runnable reload = createController.get();
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

    /**
     * Ẩn nút theo quyền. VIEW: ẩn hết. OWN: chỉ giữ nút sửa, để cập nhật việc của mình.
     * editButton = null với màn hình không có nút sửa.
     */
    private void buttons(String card, JButton addButton, JButton editButton, JButton deleteButton) {
        if (session.canManage(card)) return;
        addButton.setVisible(false);
        deleteButton.setVisible(false);
        if (editButton != null && !session.isOwnOnly(card)) editButton.setVisible(false);
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
