# HỆ THỐNG QUẢN LÝ PHÒNG GYM (GYM FITNESS PRO)
> **Mô hình kiến trúc MVC (Model – View – Controller) bằng Java Swing**

Dự án phần mềm quản lý phòng gym được xây dựng hoàn toàn bằng ngôn ngữ **Java** và thư viện giao diện chuẩn **Java Swing**, tuân thủ nghiêm ngặt mô hình kiến trúc **MVC**, thiết kế tối giản, chuyên nghiệp và đồng bộ.

---

## 1. Công nghệ và Nguyên tắc thiết kế

- **Công nghệ giao diện:** Java Swing thuần (thuộc Java SE tiêu chuẩn).
- **Cơ sở dữ liệu:** SQL Server, lược đồ chuẩn hóa BCNF, kết nối bằng JDBC (`lib/mssql-jdbc-12.8.1.jre11.jar`). Xem `docs/DATABASE_DESIGN.md`.
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
├── database/                                              # Script tạo CSDL và dữ liệu mẫu
├── docs/DATABASE_DESIGN.md                                # Phân tích chức năng và thiết kế BCNF
├── lib/                                                   # JDBC driver SQL Server
├── bin/                                                   # Mã bytecode do compile.bat sinh ra, không lưu trên repo
├── compile.bat                                            # File script biên dịch trên Windows
├── run.bat                                                # File script chạy nhanh phần mềm
└── README.md                                              # Tài liệu dự án
```

---

## 3. Danh sách màn hình và tính năng chi tiết

### 3.1. Đăng nhập (`LoginView` / `LoginController`)
- Xác thực tài khoản đăng nhập và mật khẩu.
- Kiểm tra tính hợp lệ dữ liệu và cảnh báo lỗi bằng tiếng Việt.
- Mỗi nhân viên và HLV đăng nhập bằng tài khoản riêng, chỉ thấy các chức năng theo chức vụ (xem mục Tài khoản mẫu).

### 3.2. Dashboard Tổng quan (`DashboardView` / `DashboardController`)
- **Thẻ KPI tổng quan:** Tổng hội viên, Số hội viên đang hoạt động, Số gói tập, Số lượt check-in hôm nay, Tổng doanh thu tích lũy.
- **Biểu đồ doanh thu trực quan:** Biểu đồ cột doanh thu thực tế 6 tháng gần nhất vẽ trực tiếp bằng `Graphics2D` với độ nét cao.
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
- Các trường: `TrainerID`, `FullName`, `Phone`, `ExperienceYears`; chuyên môn lưu ở `Specialty` và `TrainerSpecialty`.
- Quản lý danh sách HLV thể hình, yoga, kickboxing; thêm, sửa, xóa.

### 3.9. Quản lý lịch tập PT (`PTSessionView` / `PTSessionController`)
- Các trường: `SessionID`, `StartTime`, `EndTime`, `Status`, `TrainerID`, `AreaID`, `MemberID`.
- Lọc lịch tập theo trạng thái, chọn HLV và khu vực phù hợp.

### 3.10. Quản lý nhân viên (`StaffView` / `StaffController`)
- Các trường: `StaffID`, `FullName`, `Phone`, `Salary`, `ShiftID`, `HireDate`, `ManagerID`, `PositionID`; ca làm việc lưu ở `Shift`.
- Phân chia ca làm việc (ca sáng, ca chiều, ca hành chính), quản lý tiền lương.

### 3.11. Quản lý khu vực phòng tập (`AreaView` / `AreaController`)
- Các trường: `AreaID`, `AreaName`, `Floor`, `Description`.
- Quản lý các tầng và khu vực chức năng (Cardio, Tạ tự do, Máy khối, Yoga Studio...).

### 3.12. Quản lý thiết bị (`EquipmentView` / `EquipmentController`)
- Các trường: `EquipmentID`, `EquipmentName`, `PurchaseValue`, `PurchaseDate`, `Status`, `AreaID`.
- Theo dõi trạng thái máy móc (Tốt, Đang bảo trì, Cần thay thế linh kiện), khu vực đặt máy.

### 3.13. Quản lý bảo trì thiết bị (`MaintenanceView` / `MaintenanceController`)
- Các trường: `StaffID`, `EquipmentID`, `MaintenanceDate`, `Note`, `Status`.
- Phân công kỹ thuật viên phụ trách sửa chữa, bảo dưỡng thiết bị.

### 3.14. Quản lý phân công vệ sinh (`AreaCleaningView` / `AreaCleaningController`)
- Các trường: `AreaID`, `CleaningDate`, `ShiftID` (khóa chính), `StaffID`, `Status`.
- Phân công nhân viên tạp vụ phụ trách từng khu vực theo ca trực.

---

## 4. Hướng dẫn cài đặt và chạy chương trình

### Yêu cầu môi trường
- **Java JDK 17** trở lên.
- **SQL Server** (instance mặc định, bật TCP cổng 1433, bật SQL Server Authentication) và `sqlcmd`. Tài khoản Windows đang dùng cần quyền sysadmin để tạo database.

### Bước 1: Tạo database (chỉ làm một lần)
Từ thư mục gốc dự án:

```powershell
powershell -ExecutionPolicy Bypass -File database\setup_database.ps1
```

Script tạo `GymManagementDB`, nạp dữ liệu mẫu, tạo login `gym_app` (chỉ có quyền đọc/ghi dữ liệu) với mật khẩu ngẫu nhiên, rồi ghi vào `db.properties`. File này chứa mật khẩu nên không được commit (đã có trong `.gitignore`); mẫu ở `db.properties.example`.

Tham số `-Reset` **xóa toàn bộ dữ liệu hiện có** rồi tạo lại từ đầu.

### Bước 2: Chạy chương trình
- **Windows:** chạy `compile.bat`, sau đó `run.bat`.
- **IntelliJ IDEA:** chạy `com.gymmanagement.Main` với thư mục làm việc là thư mục gốc dự án (để tìm thấy `db.properties`).
- **Dòng lệnh:**

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src/main/java).FullName
java -Dfile.encoding=UTF-8 -cp "bin;lib\*" com.gymmanagement.Main
```

### Tài khoản mẫu
Tất cả dùng mật khẩu `123456`, chỉ để thử nghiệm; hãy đổi mật khẩu bằng nút **Đổi mật khẩu** trên sidebar trước khi dùng thật.

| Tên đăng nhập | Người dùng | Chức vụ | Thấy và làm được |
|---|---|---|---|
| `admin` | ST001 | Quản lý | Toàn bộ, gồm Dashboard, Nhân viên và Tài khoản |
| `letan1`, `letan2` | ST002, ST003 | Lễ tân | Hội viên, Đăng ký, Thanh toán, Check-in, Lịch PT; xem Gói tập, HLV, Khu vực |
| `kythuat` | ST004 | Kỹ thuật viên | Thiết bị; cập nhật phiếu bảo trì của mình; xem Khu vực |
| `tapvu` | ST005 | Tạp vụ | Cập nhật việc vệ sinh của mình; xem Khu vực |
| `hlv1` … `hlv4` | TR001 … TR004 | Huấn luyện viên | Cập nhật trạng thái buổi PT của mình; xem Khu vực |

Nhân viên hoặc HLV mới: Quản lý vào **Quản lý Tài khoản → Tạo tài khoản**. Ma trận quyền đầy đủ ở `docs/DATABASE_DESIGN.md`.

Nâng cấp database bản cũ: chạy lại `setup_database.ps1` (không có `-Reset`), dữ liệu được giữ nguyên.
