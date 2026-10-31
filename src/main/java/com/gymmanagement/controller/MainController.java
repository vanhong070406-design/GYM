package com.gymmanagement.controller;

import com.gymmanagement.view.LoginView;
import com.gymmanagement.view.MainView;
import java.util.Map;
import javax.swing.*;

/**
 * Controller chính điều phối chuyển đổi màn hình và khởi tạo các controller con
 */
public class MainController {
    private final MainView view;

    private final DashboardController dashboardController;
    private final MemberController memberController;
    private final PackageController packageController;
    private final SubscriptionController subscriptionController;
    private final PaymentController paymentController;
    private final CheckinController checkinController;
    private final TrainerController trainerController;
    private final PTSessionController ptSessionController;
    private final StaffController staffController;
    private final AreaController areaController;
    private final EquipmentController equipmentController;
    private final MaintenanceController maintenanceController;
    private final AreaCleaningController areaCleaningController;

    public MainController(MainView view) {
        this.view = view;

        // Khởi tạo các sub-controllers
        this.dashboardController = new DashboardController(view.getDashboardView());
        this.memberController = new MemberController(view.getMemberView());
        this.packageController = new PackageController(view.getPackageView());
        this.subscriptionController = new SubscriptionController(view.getSubscriptionView());
        this.paymentController = new PaymentController(view.getPaymentView());
        this.checkinController = new CheckinController(view.getCheckinView());
        this.trainerController = new TrainerController(view.getTrainerView());
        this.ptSessionController = new PTSessionController(view.getPtSessionView());
        this.staffController = new StaffController(view.getStaffView());
        this.areaController = new AreaController(view.getAreaView());
        this.equipmentController = new EquipmentController(view.getEquipmentView());
        this.maintenanceController = new MaintenanceController(view.getMaintenanceView());
        this.areaCleaningController = new AreaCleaningController(view.getAreaCleaningView());

        initNavigation();
    }

    private void initNavigation() {
        Map<String, JButton> buttons = view.getSidebarButtons();

        // 1. Dashboard
        buttons.get(MainView.CARD_DASHBOARD).addActionListener(e -> {
            dashboardController.loadData(); // Tự động làm mới số liệu khi quay về dashboard
            view.showScreen(MainView.CARD_DASHBOARD, "TỔNG QUAN HỆ THỐNG (DASHBOARD)");
        });

        // 2. Member
        buttons.get(MainView.CARD_MEMBER).addActionListener(e -> {
            view.showScreen(MainView.CARD_MEMBER, "QUẢN LÝ HỘI VIÊN");
        });

        // 3. Package
        buttons.get(MainView.CARD_PACKAGE).addActionListener(e -> {
            view.showScreen(MainView.CARD_PACKAGE, "QUẢN LÝ GÓI TẬP");
        });

        // 4. Subscription
        buttons.get(MainView.CARD_SUBSCRIPTION).addActionListener(e -> {
            view.showScreen(MainView.CARD_SUBSCRIPTION, "QUẢN LÝ ĐĂNG KÝ GÓI TẬP");
        });

        // 5. Payment
        buttons.get(MainView.CARD_PAYMENT).addActionListener(e -> {
            view.showScreen(MainView.CARD_PAYMENT, "QUẢN LÝ HÓA ĐƠN & THANH TOÁN");
        });

        // 6. Check-in
        buttons.get(MainView.CARD_CHECKIN).addActionListener(e -> {
            view.showScreen(MainView.CARD_CHECKIN, "QUẢN LÝ LỊCH SỬ CHECK-IN / CHECK-OUT");
        });

        // 7. Trainer
        buttons.get(MainView.CARD_TRAINER).addActionListener(e -> {
            view.showScreen(MainView.CARD_TRAINER, "QUẢN LÝ HUẤN LUYỆN VIÊN (PT)");
        });

        // 8. PT Session
        buttons.get(MainView.CARD_PTSESSION).addActionListener(e -> {
            view.showScreen(MainView.CARD_PTSESSION, "QUẢN LÝ LỊCH TẬP PT");
        });

        // 9. Staff
        buttons.get(MainView.CARD_STAFF).addActionListener(e -> {
            view.showScreen(MainView.CARD_STAFF, "QUẢN LÝ NHÂN VIÊN");
        });

        // 10. Area
        buttons.get(MainView.CARD_AREA).addActionListener(e -> {
            view.showScreen(MainView.CARD_AREA, "QUẢN LÝ KHU VỰC PHÒNG GYM");
        });

        // 11. Equipment
        buttons.get(MainView.CARD_EQUIPMENT).addActionListener(e -> {
            view.showScreen(MainView.CARD_EQUIPMENT, "QUẢN LÝ THIẾT BỊ PHÒNG TẬP");
        });

        // 12. Maintenance
        buttons.get(MainView.CARD_MAINTENANCE).addActionListener(e -> {
            view.showScreen(MainView.CARD_MAINTENANCE, "QUẢN LÝ PHÂN CÔNG BẢO TRÌ THIẾT BỊ");
        });

        // 13. Cleaning
        buttons.get(MainView.CARD_CLEANING).addActionListener(e -> {
            view.showScreen(MainView.CARD_CLEANING, "QUẢN LÝ PHÂN CÔNG VỆ SINH KHU VỰC");
        });

        // Đăng xuất
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

    public DashboardController getDashboardController() { return dashboardController; }
    public MemberController getMemberController() { return memberController; }
    public PackageController getPackageController() { return packageController; }
    public SubscriptionController getSubscriptionController() { return subscriptionController; }
    public PaymentController getPaymentController() { return paymentController; }
    public CheckinController getCheckinController() { return checkinController; }
    public TrainerController getTrainerController() { return trainerController; }
    public PTSessionController getPtSessionController() { return ptSessionController; }
    public StaffController getStaffController() { return staffController; }
    public AreaController getAreaController() { return areaController; }
    public EquipmentController getEquipmentController() { return equipmentController; }
    public MaintenanceController getMaintenanceController() { return maintenanceController; }
    public AreaCleaningController getAreaCleaningController() { return areaCleaningController; }
}
