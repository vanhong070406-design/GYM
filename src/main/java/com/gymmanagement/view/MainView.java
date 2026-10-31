package com.gymmanagement.view;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Cửa sổ chính của phần mềm Quản lý Phòng Gym
 * Bố cục:
 * - Sidebar bên trái: Menu điều hướng
 * - Header phía trên: Tiêu đề màn hình & Thông tin người dùng
 * - Content ở giữa: CardLayout chuyển đổi các màn hình chức năng mà không mở cửa sổ mới
 */
public class MainView extends JFrame {
    // Tên các Card trong CardLayout
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_MEMBER = "MEMBER";
    public static final String CARD_PACKAGE = "PACKAGE";
    public static final String CARD_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String CARD_PAYMENT = "PAYMENT";
    public static final String CARD_CHECKIN = "CHECKIN";
    public static final String CARD_TRAINER = "TRAINER";
    public static final String CARD_PTSESSION = "PTSESSION";
    public static final String CARD_STAFF = "STAFF";
    public static final String CARD_AREA = "AREA";
    public static final String CARD_EQUIPMENT = "EQUIPMENT";
    public static final String CARD_MAINTENANCE = "MAINTENANCE";
    public static final String CARD_CLEANING = "CLEANING";

    private CardLayout cardLayout;
    private JPanel contentContainer;
    private JLabel lblScreenTitle;
    private JButton btnLogout;

    // Các màn hình con
    private DashboardView dashboardView;
    private MemberView memberView;
    private PackageView packageView;
    private SubscriptionView subscriptionView;
    private PaymentView paymentView;
    private CheckinView checkinView;
    private TrainerView trainerView;
    private PTSessionView ptSessionView;
    private StaffView staffView;
    private AreaView areaView;
    private EquipmentView equipmentView;
    private MaintenanceView maintenanceView;
    private AreaCleaningView areaCleaningView;

    // Lưu danh sách nút Sidebar để cập nhật giao diện khi chọn
    private final Map<String, JButton> sidebarButtons = new HashMap<>();

    public MainView() {
        setTitle("Phần mềm Quản lý Phòng Gym - MVC Architecture");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_MAIN);

        // 1. Sidebar (Menu bên trái)
        JPanel sidebar = createSidebar();

        // 2. Khu vực bên phải (Gồm Header và Content)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UITheme.BG_MAIN);

        JPanel header = createHeader();

        // Content Container với CardLayout
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.BG_MAIN);

        // Khởi tạo các Views
        dashboardView = new DashboardView();
        memberView = new MemberView();
        packageView = new PackageView();
        subscriptionView = new SubscriptionView();
        paymentView = new PaymentView();
        checkinView = new CheckinView();
        trainerView = new TrainerView();
        ptSessionView = new PTSessionView();
        staffView = new StaffView();
        areaView = new AreaView();
        equipmentView = new EquipmentView();
        maintenanceView = new MaintenanceView();
        areaCleaningView = new AreaCleaningView();

        // Đưa các Views vào CardLayout
        contentContainer.add(dashboardView, CARD_DASHBOARD);
        contentContainer.add(memberView, CARD_MEMBER);
        contentContainer.add(packageView, CARD_PACKAGE);
        contentContainer.add(subscriptionView, CARD_SUBSCRIPTION);
        contentContainer.add(paymentView, CARD_PAYMENT);
        contentContainer.add(checkinView, CARD_CHECKIN);
        contentContainer.add(trainerView, CARD_TRAINER);
        contentContainer.add(ptSessionView, CARD_PTSESSION);
        contentContainer.add(staffView, CARD_STAFF);
        contentContainer.add(areaView, CARD_AREA);
        contentContainer.add(equipmentView, CARD_EQUIPMENT);
        contentContainer.add(maintenanceView, CARD_MAINTENANCE);
        contentContainer.add(areaCleaningView, CARD_CLEANING);

        rightPanel.add(header, BorderLayout.NORTH);
        rightPanel.add(contentContainer, BorderLayout.CENTER);

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(UITheme.BG_SIDEBAR);

        // Logo thương hiệu trên Sidebar
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.BG_SIDEBAR);
        brandPanel.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel lblBrand = new JLabel("GYM FITNESS PRO");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrand.setForeground(UITheme.TEXT_DARK);

        JLabel lblSub = new JLabel("Hệ Thống Quản Lý MVC");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        brandPanel.add(lblBrand);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(lblSub);

        // Danh sách các nút chức năng
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UITheme.BG_SIDEBAR);
        menuPanel.setBorder(new EmptyBorder(4, 10, 10, 10));

        addMenuItem(menuPanel, "1. Tổng quan Dashboard", CARD_DASHBOARD);
        addMenuItem(menuPanel, "2. Quản lý Hội viên", CARD_MEMBER);
        addMenuItem(menuPanel, "3. Quản lý Gói tập", CARD_PACKAGE);
        addMenuItem(menuPanel, "4. Đăng ký Gói tập", CARD_SUBSCRIPTION);
        addMenuItem(menuPanel, "5. Quản lý Thanh toán", CARD_PAYMENT);
        addMenuItem(menuPanel, "6. Quản lý Check-in", CARD_CHECKIN);
        addMenuItem(menuPanel, "7. Huấn luyện viên PT", CARD_TRAINER);
        addMenuItem(menuPanel, "8. Lịch tập PT", CARD_PTSESSION);
        addMenuItem(menuPanel, "9. Quản lý Nhân viên", CARD_STAFF);
        addMenuItem(menuPanel, "10. Quản lý Khu vực", CARD_AREA);
        addMenuItem(menuPanel, "11. Quản lý Thiết bị", CARD_EQUIPMENT);
        addMenuItem(menuPanel, "12. Bảo trì Thiết bị", CARD_MAINTENANCE);
        addMenuItem(menuPanel, "13. Phân công Vệ sinh", CARD_CLEANING);

        JScrollPane scrollMenu = new JScrollPane(menuPanel);
        scrollMenu.setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.getViewport().setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.setBorder(BorderFactory.createEmptyBorder());
        scrollMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Footer Sidebar (Nút Đăng xuất)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UITheme.BG_SIDEBAR);
        footerPanel.setBorder(new EmptyBorder(13, 14, 14, 14));

        btnLogout = new JButton("Đăng xuất hệ thống");
        btnLogout.setFont(UITheme.FONT_BOLD);
        btnLogout.setForeground(UITheme.DANGER);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setRolloverEnabled(false);
        btnLogout.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerPanel.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(scrollMenu, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        // Chọn mặc định Dashboard
        updateActiveButton(CARD_DASHBOARD);

        return sidebar;
    }

    private void addMenuItem(JPanel container, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_REGULAR);
        btn.setForeground(UITheme.TEXT_DARK);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sidebarButtons.put(cardName, btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 62));
        header.setBorder(new EmptyBorder(15, 24, 15, 24));

        lblScreenTitle = new JLabel("TỔNG QUAN HỆ THỐNG");
        lblScreenTitle.setFont(UITheme.FONT_TITLE);
        lblScreenTitle.setForeground(UITheme.TEXT_DARK);

        // User info chip
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        JLabel lblRole = new JLabel("Quản trị viên (Admin)");
        lblRole.setFont(UITheme.FONT_BOLD);
        lblRole.setForeground(UITheme.PRIMARY);

        JLabel lblStatus = new JLabel("● Trực tuyến");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.SUCCESS);

        userPanel.add(lblRole);
        userPanel.add(lblStatus);

        header.add(lblScreenTitle, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    public void showScreen(String cardName, String title) {
        cardLayout.show(contentContainer, cardName);
        lblScreenTitle.setText(title);
        updateActiveButton(cardName);
    }

    private void updateActiveButton(String activeCard) {
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeCard)) {
                btn.setBackground(UITheme.SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setFont(UITheme.FONT_BOLD);
            } else {
                btn.setBackground(UITheme.BG_SIDEBAR);
                btn.setForeground(UITheme.TEXT_DARK);
                btn.setFont(UITheme.FONT_REGULAR);
            }
        }
    }

    // Getters
    public Map<String, JButton> getSidebarButtons() { return sidebarButtons; }
    public JButton getBtnLogout() { return btnLogout; }
    public DashboardView getDashboardView() { return dashboardView; }
    public MemberView getMemberView() { return memberView; }
    public PackageView getPackageView() { return packageView; }
    public SubscriptionView getSubscriptionView() { return subscriptionView; }
    public PaymentView getPaymentView() { return paymentView; }
    public CheckinView getCheckinView() { return checkinView; }
    public TrainerView getTrainerView() { return trainerView; }
    public PTSessionView getPtSessionView() { return ptSessionView; }
    public StaffView getStaffView() { return staffView; }
    public AreaView getAreaView() { return areaView; }
    public EquipmentView getEquipmentView() { return equipmentView; }
    public MaintenanceView getMaintenanceView() { return maintenanceView; }
    public AreaCleaningView getAreaCleaningView() { return areaCleaningView; }
}
