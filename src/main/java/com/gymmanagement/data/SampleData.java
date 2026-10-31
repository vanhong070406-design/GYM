package com.gymmanagement.data;

import com.gymmanagement.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp chứa dữ liệu mẫu phục vụ hiển thị và kiểm thử giao diện
 * Không sử dụng cơ sở dữ liệu hoặc JDBC ở giai đoạn này.
 */
public class SampleData {
    private static SampleData instance;

    private List<Member> members;
    private List<PackageModel> packages;
    private List<Subscription> subscriptions;
    private List<Payment> payments;
    private List<Checkin> checkins;
    private List<Trainer> trainers;
    private List<PTSession> ptSessions;
    private List<Staff> staffList;
    private List<Area> areas;
    private List<Equipment> equipmentList;
    private List<MaintenanceAssignment> maintenanceAssignments;
    private List<AreaCleaning> areaCleanings;

    private SampleData() {
        initSampleData();
    }

    public static synchronized SampleData getInstance() {
        if (instance == null) {
            instance = new SampleData();
        }
        return instance;
    }

    private void initSampleData() {
        // 1. Dữ liệu Hội viên (Members)
        members = new ArrayList<>();
        members.add(new Member("MB001", "Nguyễn Văn An", "an.nguyen@gmail.com", "0901234567", "2024-01-15", "123 Lê Lợi, Q.1, TP.HCM", "1995-05-12", "Nam"));
        members.add(new Member("MB002", "Trần Thị Bích", "bich.tran@gmail.com", "0912345678", "2024-02-10", "456 Nguyễn Huệ, Q.1, TP.HCM", "1998-08-20", "Nữ"));
        members.add(new Member("MB003", "Lê Hoàng Cường", "cuong.le@yahoo.com", "0987654321", "2024-03-01", "78 Hai Bà Trưng, Q.3, TP.HCM", "1992-11-03", "Nam"));
        members.add(new Member("MB004", "Phạm Diệu Linh", "linh.pham@gmail.com", "0933445566", "2024-03-20", "15 Nam Kỳ Khởi Nghĩa, Q.3, TP.HCM", "2000-01-25", "Nữ"));
        members.add(new Member("MB005", "Võ Minh Đức", "duc.vo@outlook.com", "0977889900", "2024-04-05", "204 Điện Biên Phủ, Bình Thạnh", "1996-09-17", "Nam"));
        members.add(new Member("MB006", "Đặng Thùy Dương", "duong.dang@gmail.com", "0944556677", "2024-05-12", "88 Xô Viết Nghệ Tĩnh, Bình Thạnh", "1999-04-14", "Nữ"));

        // 2. Dữ liệu Gói tập (Packages)
        packages = new ArrayList<>();
        packages.add(new PackageModel("PK01", "Gói Cơ Bản (1 Tháng)", 500000, 30, "Sử dụng toàn bộ máy tập thông thường"));
        packages.add(new PackageModel("PK02", "Gói Tiêu Chuẩn (3 Tháng)", 1350000, 90, "Tặng 1 buổi PT định hướng và tủ đồ"));
        packages.add(new PackageModel("PK03", "Gói Nâng Cao (6 Tháng)", 2500000, 180, "Tập full thời gian, phòng xông hơi, bể sục"));
        packages.add(new PackageModel("PK04", "Gói VIP (12 Tháng)", 4500000, 365, "Tập không giới hạn, khăn tắm, 5 buổi PT miễn phí"));
        packages.add(new PackageModel("PK05", "Gói Yoga & Pilates (1 Tháng)", 800000, 30, "Tham gia các lớp Yoga và thảm tập chuyên biệt"));

        // 3. Dữ liệu Đăng ký gói tập (Subscriptions)
        subscriptions = new ArrayList<>();
        subscriptions.add(new Subscription("SUB001", "2024-01-15", "2025-01-15", "Đang hoạt động", "MB001", "PK04"));
        subscriptions.add(new Subscription("SUB002", "2024-02-10", "2024-05-10", "Đang hoạt động", "MB002", "PK02"));
        subscriptions.add(new Subscription("SUB003", "2024-03-01", "2024-04-01", "Hết hạn", "MB003", "PK01"));
        subscriptions.add(new Subscription("SUB004", "2024-03-20", "2024-09-20", "Đang hoạt động", "MB004", "PK03"));
        subscriptions.add(new Subscription("SUB005", "2024-04-05", "2024-05-05", "Đang hoạt động", "MB005", "PK05"));
        subscriptions.add(new Subscription("SUB006", "2024-05-12", "2024-08-12", "Đang hoạt động", "MB006", "PK02"));

        // 4. Dữ liệu Nhân viên (Staff)
        staffList = new ArrayList<>();
        staffList.add(new Staff("ST001", "Hoàng Văn Quản Lý", "0908889999", 25000000, "Ca hành chính", "2022-01-10", "None"));
        staffList.add(new Staff("ST002", "Nguyễn Lễ Tân 1", "0901112222", 9000000, "Ca sáng (06:00 - 14:00)", "2023-03-15", "ST001"));
        staffList.add(new Staff("ST003", "Trần Lễ Tân 2", "0902223333", 9000000, "Ca chiều (14:00 - 22:00)", "2023-04-01", "ST001"));
        staffList.add(new Staff("ST004", "Phạm Kỹ Thuật", "0903334444", 12000000, "Ca sáng (06:00 - 14:00)", "2022-08-20", "ST001"));
        staffList.add(new Staff("ST005", "Lê Tạp Vụ", "0904445555", 7500000, "Ca sáng (06:00 - 14:00)", "2023-06-01", "ST001"));

        // 5. Dữ liệu Thanh toán (Payments)
        payments = new ArrayList<>();
        payments.add(new Payment("PM001", "2024-01-15", "Chuyển khoản", 4500000, "SUB001", "ST002"));
        payments.add(new Payment("PM002", "2024-02-10", "Tiền mặt", 1350000, "SUB002", "ST002"));
        payments.add(new Payment("PM003", "2024-03-01", "Thẻ tín dụng", 500000, "SUB003", "ST003"));
        payments.add(new Payment("PM004", "2024-03-20", "Chuyển khoản", 2500000, "SUB004", "ST003"));
        payments.add(new Payment("PM005", "2024-04-05", "Tiền mặt", 800000, "SUB005", "ST002"));
        payments.add(new Payment("PM006", "2024-05-12", "Chuyển khoản", 1350000, "SUB006", "ST003"));

        // 6. Dữ liệu Check-in (Checkin)
        checkins = new ArrayList<>();
        checkins.add(new Checkin("CK001", "2024-05-15 07:15", "2024-05-15 08:45", "MB001", "ST002"));
        checkins.add(new Checkin("CK002", "2024-05-15 08:30", "2024-05-15 10:00", "MB002", "ST002"));
        checkins.add(new Checkin("CK003", "2024-05-15 09:00", "2024-05-15 10:30", "MB004", "ST002"));
        checkins.add(new Checkin("CK004", "2024-05-15 15:20", "2024-05-15 17:00", "MB005", "ST003"));
        checkins.add(new Checkin("CK005", "2024-05-15 17:40", "2024-05-15 19:15", "MB006", "ST003"));
        checkins.add(new Checkin("CK006", "2024-05-15 18:00", "--:--", "MB001", "ST003"));

        // 7. Dữ liệu Huấn luyện viên (Trainers)
        trainers = new ArrayList<>();
        trainers.add(new Trainer("TR001", "Nguyễn Tuấn Dũng", "0911223344", "6 năm", "Thể hình chuyên nghiệp & Tăng cơ"));
        trainers.add(new Trainer("TR002", "Trần Mai Anh", "0922334455", "4 năm", "Giảm mỡ, Pilates & Yoga"));
        trainers.add(new Trainer("TR003", "Lê Văn Hùng", "0933445566", "5 năm", "CrossFit, Kickboxing"));
        trainers.add(new Trainer("TR004", "Đặng Thị Thảo", "0944556677", "3 năm", "Cardio và phục hồi chức năng"));

        // 8. Dữ liệu Khu vực (Areas)
        areas = new ArrayList<>();
        areas.add(new Area("AR01", "Khu Cardio & Chạy bộ", "Tầng 1", "Khu máy chạy, xe đạp, trượt tuyết"));
        areas.add(new Area("AR02", "Khu Tạ Tự Do (Free Weights)", "Tầng 1", "Tạ đơn, tạ đòn, ghế đẩy tạ"));
        areas.add(new Area("AR03", "Khu Máy Khối (Pin Loaded)", "Tầng 2", "Các dòng máy tập từng nhóm cơ cô lập"));
        areas.add(new Area("AR04", "Phòng Studio Yoga & Nhóm", "Tầng 2", "Sàn gỗ chuyên dụng, gương toàn phần"));
        areas.add(new Area("AR05", "Khu Giãn Cơ & Phục Hồi", "Tầng 2", "Thảm, con lăn foam roller, xông hơi"));

        // 9. Dữ liệu Lịch tập PT (PTSessions)
        ptSessions = new ArrayList<>();
        ptSessions.add(new PTSession("SES001", "2024-05-16 08:00", "2024-05-16 09:30", "Đã đặt", "TR001", "AR02"));
        ptSessions.add(new PTSession("SES002", "2024-05-16 10:00", "2024-05-16 11:30", "Đang diễn ra", "TR002", "AR04"));
        ptSessions.add(new PTSession("SES003", "2024-05-16 14:00", "2024-05-16 15:30", "Hoàn thành", "TR003", "AR01"));
        ptSessions.add(new PTSession("SES004", "2024-05-16 16:30", "2024-05-16 18:00", "Đã đặt", "TR004", "AR05"));
        ptSessions.add(new PTSession("SES005", "2024-05-16 18:30", "2024-05-16 20:00", "Đã đặt", "TR001", "AR02"));

        // 10. Dữ liệu Thiết bị (Equipment)
        equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment("EQ01", "Máy chạy bộ Technogym Skillrun", 85000000, "2023-01-10", "Tốt", "AR01"));
        equipmentList.add(new Equipment("EQ02", "Xe đạp tập LifeFitness IC7", 45000000, "2023-01-15", "Tốt", "AR01"));
        equipmentList.add(new Equipment("EQ03", "Khung gánh tạ Smith Machine", 38000000, "2023-02-01", "Tốt", "AR02"));
        equipmentList.add(new Equipment("EQ04", "Ghế đẩy ngực bằng Impulse", 18000000, "2023-02-10", "Đang bảo trì", "AR02"));
        equipmentList.add(new Equipment("EQ05", "Máy kéo xô đôi Cable Crossover", 55000000, "2023-03-05", "Tốt", "AR03"));
        equipmentList.add(new Equipment("EQ06", "Máy đạp đùi Leg Press 45 độ", 42000000, "2023-03-12", "Cần thay thế linh kiện", "AR03"));

        // 11. Dữ liệu Bảo trì thiết bị (MaintenanceAssignments)
        maintenanceAssignments = new ArrayList<>();
        maintenanceAssignments.add(new MaintenanceAssignment("ST004", "EQ04", "2024-05-10", "Thay đệm bọc ghế và tra dầu khớp trượt", "Đang bảo trì"));
        maintenanceAssignments.add(new MaintenanceAssignment("ST004", "EQ06", "2024-05-14", "Kiểm tra cáp chịu tải và chốt hãm", "Chờ linh kiện"));
        maintenanceAssignments.add(new MaintenanceAssignment("ST004", "EQ01", "2024-04-20", "Bảo dưỡng động cơ và cân chỉnh băng chuyền", "Đã hoàn thành"));

        // 12. Dữ liệu Phân công vệ sinh (AreaCleanings)
        areaCleanings = new ArrayList<>();
        areaCleanings.add(new AreaCleaning("ST005", "AR01", "2024-05-15", "Ca sáng", "Đã hoàn thành"));
        areaCleanings.add(new AreaCleaning("ST005", "AR02", "2024-05-15", "Ca sáng", "Đã hoàn thành"));
        areaCleanings.add(new AreaCleaning("ST005", "AR04", "2024-05-15", "Ca chiều", "Đang thực hiện"));
        areaCleanings.add(new AreaCleaning("ST005", "AR05", "2024-05-15", "Ca chiều", "Chưa làm"));
    }

    // Getters
    public List<Member> getMembers() { return members; }
    public List<PackageModel> getPackages() { return packages; }
    public List<Subscription> getSubscriptions() { return subscriptions; }
    public List<Payment> getPayments() { return payments; }
    public List<Checkin> getCheckins() { return checkins; }
    public List<Trainer> getTrainers() { return trainers; }
    public List<PTSession> getPtSessions() { return ptSessions; }
    public List<Staff> getStaffList() { return staffList; }
    public List<Area> getAreas() { return areas; }
    public List<Equipment> getEquipmentList() { return equipmentList; }
    public List<MaintenanceAssignment> getMaintenanceAssignments() { return maintenanceAssignments; }
    public List<AreaCleaning> getAreaCleanings() { return areaCleanings; }
}
