package com.gymmanagement.view;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Cửa sổ chính của phần mềm Quản lý Phòng Gym
 * Bố cục:
 * - Sidebar bên trái: Menu điều hướng (bấm GYM FITNESS PRO để xem Dashboard)
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
    /** Mã quyền của lịch tập PT (không còn card riêng, nằm trong trang chi tiết của màn hình Quản lý Huấn luyện viên). */
    public static final String CARD_PTSESSION = "PTSESSION";
    public static final String CARD_STAFF = "STAFF";
    public static final String CARD_EQUIPMENT = "EQUIPMENT";
    /** Mã quyền của tab Bảo trì (không còn card riêng, nằm trong màn hình Thiết bị). */
    public static final String CARD_MAINTENANCE = "MAINTENANCE";

    private CardLayout cardLayout;
    private JPanel contentContainer;
    private JLabel lblScreenTitle;
    private JButton btnLogout;
    private JButton btnChangePassword;
    private JLabel lblRole;
    private JPanel brandPanel;

    // Các màn hình con
    private DashboardView dashboardView;
    private MemberView memberView;
    private PackageView packageView;
    private SubscriptionView subscriptionView;
    private PaymentView paymentView;
    private CheckinView checkinView;
    private TrainerView trainerView;
    private StaffView staffView;
    private EquipmentView equipmentView;
    private MaintenanceView maintenanceView;
    private JTabbedPane equipmentTabs;

    // Lưu danh sách nút Sidebar để cập nhật giao diện khi chọn
    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private final Map<String, Component> sidebarGaps = new HashMap<>();

    public MainView() {
        setTitle("Phần mềm Quản lý Phòng Gym - Nutri Boost ProMax");
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
        trainerView = new TrainerView(); // gồm cả lịch tập PT trong trang chi tiết
        staffView = new StaffView();
        equipmentView = new EquipmentView();
        maintenanceView = new MaintenanceView();

        // Màn hình Thiết bị gồm 2 tab: danh sách thiết bị và phân công bảo trì
        equipmentTabs = new JTabbedPane();
        equipmentTabs.setFont(UITheme.FONT_BOLD);
        equipmentTabs.addTab("Danh sách thiết bị", equipmentView);
        equipmentTabs.addTab("Bảo trì thiết bị", maintenanceView); 

        // Đưa các Views vào CardLayout
        contentContainer.add(dashboardView, CARD_DASHBOARD);
        contentContainer.add(memberView, CARD_MEMBER);
        contentContainer.add(packageView, CARD_PACKAGE);
        contentContainer.add(subscriptionView, CARD_SUBSCRIPTION);
        contentContainer.add(paymentView, CARD_PAYMENT);
        contentContainer.add(checkinView, CARD_CHECKIN);
        contentContainer.add(trainerView, CARD_TRAINER);
        contentContainer.add(staffView, CARD_STAFF);
        contentContainer.add(equipmentTabs, CARD_EQUIPMENT);

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

        // Logo thương hiệu trên Sidebar (bấm vào để mở Dashboard)
        brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.BG_SIDEBAR);
        brandPanel.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel lblBrand = new JLabel("GYM FITNESS PRO");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrand.setForeground(UITheme.TEXT_LIGHT);

        JLabel lblSub = new JLabel("Nutri Boost ProMax");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.SIDEBAR_TEXT_MUTED);

        brandPanel.add(lblBrand);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(lblSub);

        // Danh sách các nút chức năng
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UITheme.BG_SIDEBAR);
        menuPanel.setBorder(new EmptyBorder(4, 10, 10, 10));

        addMenuItem(menuPanel, "Quản lý Hội viên", CARD_MEMBER);
        addMenuItem(menuPanel, "Đăng ký Gói tập", CARD_SUBSCRIPTION);
        addMenuItem(menuPanel, "Quản lý Thanh toán", CARD_PAYMENT);
        addMenuItem(menuPanel, "Check in / Check out", CARD_CHECKIN);
        addMenuItem(menuPanel, "Quản lý Huấn luyện viên", CARD_TRAINER);
        addMenuItem(menuPanel, "Quản lý Nhân viên", CARD_STAFF);
        addMenuItem(menuPanel, "Quản lý Thiết bị", CARD_EQUIPMENT);
        addMenuItem(menuPanel, "Quản lý Gói tập", CARD_PACKAGE);

        JScrollPane scrollMenu = new JScrollPane(menuPanel);
        scrollMenu.setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.getViewport().setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.setBorder(BorderFactory.createEmptyBorder());
        scrollMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Footer Sidebar (Nút Đăng xuất)
        JPanel footerPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        footerPanel.setBackground(UITheme.BG_SIDEBAR);
        footerPanel.setBorder(new EmptyBorder(13, 14, 14, 14));

        btnLogout = createFooterButton("Đăng xuất hệ thống", UITheme.DANGER);
        btnChangePassword = createFooterButton("Đổi mật khẩu", UITheme.TEXT_DARK);
        footerPanel.add(btnChangePassword);
        footerPanel.add(btnLogout);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(scrollMenu, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createFooterButton(String text, Color textColor) {
        JButton btn = new JButton(text);
        UITheme.styleButton(btn, textColor, Color.WHITE, new Color(241, 245, 249), UITheme.BORDER, null);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private void addMenuItem(JPanel container, String text, String cardName) {
        JButton btn = new JButton(text);
        UITheme.styleSidebarButton(btn, false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        sidebarButtons.put(cardName, btn);
        container.add(btn);
        Component gap = Box.createRigidArea(new Dimension(0, 4));
        sidebarGaps.put(cardName, gap);
        container.add(gap);
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

        lblRole = new JLabel(" ");
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

    /** Hiện tên và chức vụ người đang đăng nhập trên header. */
    public void setCurrentUser(String text) {
        lblRole.setText(text);
    }

    /** Ẩn mục menu của màn hình mà tài khoản không có quyền. */
    public void hideMenuItem(String cardName) {
        sidebarButtons.get(cardName).setVisible(false);
        sidebarGaps.get(cardName).setVisible(false);
    }

    public void showScreen(String cardName, String title) {
        cardLayout.show(contentContainer, cardName);
        lblScreenTitle.setText(title);
        updateActiveButton(cardName);
    }

    private void updateActiveButton(String activeCard) {
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            UITheme.styleSidebarButton(entry.getValue(), entry.getKey().equals(activeCard));
        }
    }

    // Getters
    public Map<String, JButton> getSidebarButtons() { return sidebarButtons; }
    public JPanel getBrandPanel() { return brandPanel; }
    public JButton getBtnLogout() { return btnLogout; }
    public JButton getBtnChangePassword() { return btnChangePassword; }
    public DashboardView getDashboardView() { return dashboardView; }
    public MemberView getMemberView() { return memberView; }
    public PackageView getPackageView() { return packageView; }
    public SubscriptionView getSubscriptionView() { return subscriptionView; }
    public PaymentView getPaymentView() { return paymentView; }
    public CheckinView getCheckinView() { return checkinView; }
    public TrainerView getTrainerView() { return trainerView; }
    public StaffView getStaffView() { return staffView; }
    public EquipmentView getEquipmentView() { return equipmentView; }
    public MaintenanceView getMaintenanceView() { return maintenanceView; }
    public JTabbedPane getEquipmentTabs() { return equipmentTabs; }
}