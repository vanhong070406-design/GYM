# HỆ THỐNG QUẢN LÝ PHÒNG GYM (GYM FITNESS PRO)
> **Mô hình kiến trúc MVC (Model – View – Controller) bằng Java Swing**

Dự án phần mềm quản lý phòng gym được xây dựng hoàn toàn bằng ngôn ngữ **Java** và thư viện giao diện chuẩn **Java Swing**, tuân thủ nghiêm ngặt mô hình kiến trúc **MVC**, thiết kế tối giản, chuyên nghiệp và đồng bộ.

---

## 1. Công nghệ và Nguyên tắc thiết kế

- **Công nghệ giao diện:** Java Swing thuần (thuộc Java SE tiêu chuẩn, không cần cài thêm thư viện hay JavaFX SDK ngoài).
- **Cách thức xây dựng:** 100% bằng mã Java (không sử dụng FXML, không sử dụng HTML/CSS bên ngoài).
- **Màu sắc & Giao diện:**
  - Nền chính: Màu trắng / xám nhạt (`#F8FAFC`, `#FFFFFF`).
  - Màu nhấn chủ đạo (Primary Accent): Xanh dương đậm / Navy (`#1E3A8A`, `#2563EB`).
  - Màu chữ: Xám than đen rõ nét (`#0F172A`).
  - Bố cục Sidebar + Header + Content cố định trên một cửa sổ duy nhất, hỗ trợ co giãn kích thước (Responsive Resizing) mượt mà bằng `CardLayout` và các `LayoutManager` chuẩn.

---

## 2. Cấu trúc thư mục dự án

```text
GymManagement/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── gymmanagement/
│                   ├── Main.java                          # Điểm khởi chạy ứng dụng
│                   ├── data/
│                   │   └── SampleData.java                # Quản lý kho dữ liệu mẫu in-memory
│                   ├── model/                             # [M] Tầng Dữ Liệu
│                   │   ├── Member.java                    # Hội viên
│                   │   ├── PackageModel.java              # Gói tập
│                   │   ├── Subscription.java              # Đăng ký gói tập
│                   │   ├── Payment.java                   # Hóa đơn thanh toán
│                   │   ├── Checkin.java                   # Lịch sử vào/ra
│                   │   ├── Trainer.java                   # Huấn luyện viên (PT)
│                   │   ├── PTSession.java                 # Lịch tập cá nhân PT
│                   │   ├── Staff.java                     # Nhân viên
│                   │   ├── Area.java                      # Khu vực phòng tập
│                   │   ├── Equipment.java                 # Thiết bị máy móc
│                   │   ├── MaintenanceAssignment.java     # Phân công bảo trì
│                   │   └── AreaCleaning.java              # Phân công vệ sinh
│                   ├── view/                              # [V] Tầng Giao Diện
│                   │   ├── UITheme.java                   # Bảng màu, phông chữ, thành phần tái sử dụng
│                   │   ├── LoginView.java                 # Màn hình đăng nhập
│                   │   ├── MainView.java                  # Khung cửa sổ chính (Sidebar + Header + Content)
│                   │   ├── DashboardView.java             # Màn hình tổng quan & biểu đồ
│                   │   ├── MemberView.java                # Quản lý hội viên
│                   │   ├── PackageView.java               # Quản lý gói tập
│                   │   ├── SubscriptionView.java          # Đăng ký gói tập
│                   │   ├── PaymentView.java               # Quản lý thanh toán
│                   │   ├── CheckinView.java               # Quản lý check-in
│                   │   ├── TrainerView.java               # Quản lý HLV
│                   │   ├── PTSessionView.java             # Quản lý lịch tập PT
│                   │   ├── StaffView.java                 # Quản lý nhân viên
│                   │   ├── AreaView.java                  # Quản lý khu vực
│                   │   ├── EquipmentView.java             # Quản lý thiết bị
│                   │   ├── MaintenanceView.java           # Phân công bảo trì
│                   │   └── AreaCleaningView.java          # Phân công vệ sinh
│                   └── controller/                        # [C] Tầng Điều Khiển
│                       ├── LoginController.java
│                       ├── MainController.java
│                       ├── DashboardController.java
│                       ├── MemberController.java
│                       ├── PackageController.java
│                       ├── SubscriptionController.java
│                       ├── PaymentController.java
│                       ├── CheckinController.java
│                       ├── TrainerController.java
│                       ├── PTSessionController.java
│                       ├── StaffController.java
│                       ├── AreaController.java
│                       ├── EquipmentController.java
│                       ├── MaintenanceController.java
│                       └── AreaCleaningController.java
├── bin/                                                   # Thư mục chứa mã bytecode sau biên dịch
├── compile.bat                                            # File script biên dịch trên Windows
├── run.bat                                                # File script chạy nhanh phần mềm
└── README.md                                              # Tài liệu dự án
```

---

## 3. Danh sách màn hình và tính năng chi tiết

### 3.1. Đăng nhập (`LoginView` / `LoginController`)
- Xác thực tài khoản đăng nhập và mật khẩu.
- Kiểm tra tính hợp lệ dữ liệu và cảnh báo lỗi bằng tiếng Việt.
- **Tài khoản mặc định:** `admin` | **Mật khẩu:** `123456`.

### 3.2. Dashboard Tổng quan (`DashboardView` / `DashboardController`)
- **Thẻ KPI tổng quan:** Tổng hội viên, Số hội viên đang hoạt động, Số gói tập, Số lượt check-in hôm nay, Tổng doanh thu tích lũy.
- **Biểu đồ doanh thu trực quan:** Biểu đồ cột minh họa doanh thu 6 tháng vẽ trực tiếp bằng `Graphics2D` với độ nét cao.
- **Bảng hoạt động mới nhất:** Lịch sử ra vào phòng gym gần đây nhất.

### 3.3. Quản lý hội viên (`MemberView` / `MemberController`)
- Các trường: `MemberID`, `FullName`, `Email`, `Phone`, `JoinDate`, `Address`, `BirthDate`, `Gender`.
- Bảng danh sách hội viên, tìm kiếm theo tên/mã/SĐT, biểu mẫu thêm mới và chỉnh sửa qua Dialog, xác nhận xóa.

### 3.4. Quản lý gói tập (`PackageView` / `PackageController`)
- Các trường: `PackageID`, `PackageName`, `Price`, `Duration`, `Description`.
- Thêm, sửa, xóa, tìm kiếm gói tập với định dạng tiền tệ rõ ràng.

### 3.5. Quản lý đăng ký gói tập (`SubscriptionView` / `SubscriptionController`)
- Các trường: `SubscriptionID`, `StartDate`, `EndDate`, `Status`, `MemberID`, `PackageID`.
- Bộ lọc theo trạng thái (Đang hoạt động, Hết hạn, Tạm dừng), biểu mẫu đăng ký liên kết thông tin Hội viên và Gói tập.

### 3.6. Quản lý thanh toán (`PaymentView` / `PaymentController`)
- Các trường: `PaymentID`, `PaymentDate`, `PaymentMethod`, `Amount`, `SubscriptionID`, `ReceptionistID`.
- Lập hóa đơn thanh toán mới, lọc theo phương thức thanh toán (Tiền mặt, Chuyển khoản, Thẻ).

### 3.7. Quản lý check-in (`CheckinView` / `CheckinController`)
- Các trường: `CheckinID`, `CheckinTime`, `CheckoutTime`, `MemberID`, `ReceptionistID`.
- Nút Check-in nhanh tự động lấy thời gian hiện tại, nút Check-out một chạm.

### 3.8. Quản lý huấn luyện viên (`TrainerView` / `TrainerController`)
- Các trường: `TrainerID`, `FullName`, `Phone`, `Experience`, `Specialty`.
- Quản lý danh sách HLV thể hình, yoga, kickboxing; thêm, sửa, xóa.

### 3.9. Quản lý lịch tập PT (`PTSessionView` / `PTSessionController`)
- Các trường: `SessionID`, `StartTime`, `EndTime`, `Status`, `TrainerID`, `AreaID`.
- Lọc lịch tập theo trạng thái, chọn HLV và khu vực phù hợp.

### 3.10. Quản lý nhân viên (`StaffView` / `StaffController`)
- Các trường: `StaffID`, `FullName`, `Phone`, `Salary`, `Shift`, `HireDate`, `ManagerID`.
- Phân chia ca làm việc (ca sáng, ca chiều, ca hành chính), quản lý tiền lương.

### 3.11. Quản lý khu vực phòng tập (`AreaView` / `AreaController`)
- Các trường: `AreaID`, `AreaName`, `Floor`, `Description`.
- Quản lý các tầng và khu vực chức năng (Cardio, Tạ tự do, Máy khối, Yoga Studio...).

### 3.12. Quản lý thiết bị (`EquipmentView` / `EquipmentController`)
- Các trường: `EquipmentID`, `EquipmentName`, `Value`, `PurchaseDate`, `Status`, `AreaID`.
- Theo dõi trạng thái máy móc (Tốt, Đang bảo trì, Cần thay thế linh kiện), khu vực đặt máy.

### 3.13. Quản lý bảo trì thiết bị (`MaintenanceView` / `MaintenanceController`)
- Các trường: `StaffID`, `EquipmentID`, `MaintenanceDate`, `Note`, `Status`.
- Phân công kỹ thuật viên phụ trách sửa chữa, bảo dưỡng thiết bị.

### 3.14. Quản lý phân công vệ sinh (`AreaCleaningView` / `AreaCleaningController`)
- Các trường: `StaffID`, `AreaID`, `CleaningDate`, `Shift`, `Status`.
- Phân công nhân viên tạp vụ phụ trách từng khu vực theo ca trực.

---

## 4. Hướng dẫn cài đặt và chạy chương trình

### Yêu cầu môi trường
- Đã cài đặt **Java JDK 11** trở lên (hỗ trợ hoàn hảo trên Java 17, 21, 26).

### Cách 1: Chạy trực tiếp bằng tập lệnh trên Windows (Đơn giản nhất)
1. **Biên dịch mã nguồn:** Nhấp đúp vào file `compile.bat`.
2. **Khởi chạy ứng dụng:** Nhấp đúp vào file `run.bat`.

### Cách 2: Chạy bằng Command Prompt / PowerShell
Từ thư mục gốc của dự án, mở Terminal và thực hiện:

```bash
# 1. Tạo thư mục bin (nếu chưa có)
mkdir bin

# 2. Biên dịch mã nguồn với bảng mã UTF-8
javac -encoding UTF-8 -d bin src/main/java/com/gymmanagement/model/*.java src/main/java/com/gymmanagement/data/*.java src/main/java/com/gymmanagement/view/*.java src/main/java/com/gymmanagement/controller/*.java src/main/java/com/gymmanagement/Main.java

# 3. Khởi chạy phần mềm
java -Dfile.encoding=UTF-8 -cp bin com.gymmanagement.Main
```

### Thông tin đăng nhập
- **Tên đăng nhập:** `admin`
- **Mật khẩu:** `123456`
#   G Y M  
 