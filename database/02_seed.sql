/* Dữ liệu mẫu chuyển từ SampleData.java cũ. Các ngày giao dịch được tính tương đối
   theo ngày chạy script để Dashboard (check-in hôm nay, doanh thu 6 tháng) có số liệu. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @today DATE = CAST(GETDATE() AS DATE);
DECLARE @now DATETIME2(0) = CAST(@today AS DATETIME2(0));   -- 00:00 hôm nay, mốc tính các giờ mẫu
-- Check-in hôm nay không được ở tương lai, nếu không sẽ không check-out được
DECLARE @current DATETIME2(0) = CAST(SYSDATETIME() AS DATETIME2(0));
DECLARE @in1 DATETIME2(0) = DATEADD(MINUTE, -40, @current);
DECLARE @in2 DATETIME2(0) = DATEADD(MINUTE, -10, @current);
IF @in1 < @now SET @in1 = @now;
IF @in2 < @now SET @in2 = @now;

-- Chỉ Quản lý và Lễ tân đăng nhập được; các chức vụ khác chỉ là hồ sơ nhân sự
INSERT INTO Position (PositionID, PositionName, CanLogin) VALUES
 ('QL',  N'Quản lý',         1),
 ('LT',  N'Lễ tân',          1),
 ('KT',  N'Kỹ thuật viên',   0),
 ('TV',  N'Tạp vụ',          0),
 ('HLV', N'Huấn luyện viên', 0);

INSERT INTO PositionPermission (PositionID, ScreenCode, AccessLevel) VALUES
 ('QL', 'DASHBOARD', 'MANAGE'),
 ('QL', 'MEMBER', 'MANAGE'),
 ('QL', 'PACKAGE', 'MANAGE'),
 ('QL', 'SUBSCRIPTION', 'MANAGE'),
 ('QL', 'PAYMENT', 'MANAGE'),
 ('QL', 'CHECKIN', 'MANAGE'),
 ('QL', 'TRAINER', 'MANAGE'),
 ('QL', 'PTSESSION', 'MANAGE'),
 ('QL', 'STAFF', 'MANAGE'),
 ('QL', 'EQUIPMENT', 'MANAGE'),
 ('QL', 'MAINTENANCE', 'MANAGE'),
 ('LT', 'MEMBER', 'MANAGE'),
 ('LT', 'SUBSCRIPTION', 'MANAGE'),
 ('LT', 'PAYMENT', 'MANAGE'),
 ('LT', 'CHECKIN', 'MANAGE'),
 ('LT', 'TRAINER', 'VIEW'),
 ('LT', 'PTSESSION', 'MANAGE');

INSERT INTO Shift (ShiftID, ShiftName, StartTime, EndTime) VALUES
 ('SANG',  N'Ca sáng',       '06:00', '14:00'),
 ('CHIEU', N'Ca chiều',      '14:00', '22:00'),
 ('HC',    N'Ca hành chính', '08:00', '17:00'),
 ('DEM',   N'Ca đêm',        '22:00', '06:00');

-- Hệ thống chỉ có 1 quản lý (chức vụ QL), không lưu ai quản lý ai
INSERT INTO Staff (StaffID, FullName, Phone, Salary, ShiftID, HireDate, PositionID) VALUES
 ('ST001', N'Hoàng Văn Quản Lý', '0908889999', 25000000, 'HC',    '2022-01-10', 'QL'),
 ('ST002', N'Nguyễn Lễ Tân 1',   '0901112222',  9000000, 'SANG',  '2023-03-15', 'LT'),
 ('ST003', N'Trần Lễ Tân 2',     '0902223333',  9000000, 'CHIEU', '2023-04-01', 'LT'),
 ('ST004', N'Phạm Kỹ Thuật',     '0903334444', 12000000, 'SANG',  '2022-08-20', 'KT'),
 ('ST005', N'Lê Tạp Vụ',         '0904445555',  7500000, 'SANG',  '2023-06-01', 'TV');

INSERT INTO Member (MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender) VALUES
 ('MB001', N'Nguyễn Văn An',   'an.nguyen@gmail.com',  '0901234567', '2024-01-15', N'123 Lê Lợi, Q.1, TP.HCM',            '1995-05-12', N'Nam'),
 ('MB002', N'Trần Thị Bích',   'bich.tran@gmail.com',  '0912345678', '2024-02-10', N'456 Nguyễn Huệ, Q.1, TP.HCM',        '1998-08-20', N'Nữ'),
 ('MB003', N'Lê Hoàng Cường',  'cuong.le@yahoo.com',   '0987654321', '2024-03-01', N'78 Hai Bà Trưng, Q.3, TP.HCM',       '1992-11-03', N'Nam'),
 ('MB004', N'Phạm Diệu Linh',  'linh.pham@gmail.com',  '0933445566', '2024-03-20', N'15 Nam Kỳ Khởi Nghĩa, Q.3, TP.HCM',  '2000-01-25', N'Nữ'),
 ('MB005', N'Võ Minh Đức',     'duc.vo@outlook.com',   '0977889900', '2024-04-05', N'204 Điện Biên Phủ, Bình Thạnh',      '1996-09-17', N'Nam'),
 ('MB006', N'Đặng Thùy Dương', 'duong.dang@gmail.com', '0944556677', '2024-05-12', N'88 Xô Viết Nghệ Tĩnh, Bình Thạnh',   '1999-04-14', N'Nữ');

INSERT INTO GymPackage (PackageID, PackageName, Price, DurationDays, Description) VALUES
 ('PK01', N'Gói Cơ Bản (1 Tháng)',         500000,  30, N'Sử dụng toàn bộ máy tập thông thường'),
 ('PK02', N'Gói Tiêu Chuẩn (3 Tháng)',    1350000,  90, N'Tặng 1 buổi PT định hướng và tủ đồ'),
 ('PK03', N'Gói Nâng Cao (6 Tháng)',      2500000, 180, N'Tập full thời gian, phòng xông hơi, bể sục'),
 ('PK04', N'Gói VIP (12 Tháng)',          4500000, 365, N'Tập không giới hạn, khăn tắm, 5 buổi PT miễn phí'),
 ('PK05', N'Gói Yoga & Pilates (1 Tháng)', 800000,  30, N'Tham gia các lớp Yoga và thảm tập chuyên biệt');

INSERT INTO Subscription (SubscriptionID, MemberID, PackageID, StartDate, EndDate, Status) VALUES
 ('SUB001', 'MB001', 'PK04', DATEADD(DAY,-150,@today), DATEADD(DAY,215,@today), N'Đang hoạt động'),
 ('SUB002', 'MB002', 'PK02', DATEADD(DAY, -60,@today), DATEADD(DAY, 30,@today), N'Đang hoạt động'),
 ('SUB003', 'MB003', 'PK01', DATEADD(DAY,-120,@today), DATEADD(DAY,-90,@today), N'Hết hạn'),
 ('SUB004', 'MB004', 'PK03', DATEADD(DAY, -30,@today), DATEADD(DAY,150,@today), N'Đang hoạt động'),
 ('SUB005', 'MB005', 'PK05', DATEADD(DAY, -90,@today), DATEADD(DAY,-60,@today), N'Hết hạn'),
 ('SUB006', 'MB006', 'PK02', DATEADD(DAY,  -1,@today), DATEADD(DAY, 89,@today), N'Đang hoạt động');

INSERT INTO Payment (PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID) VALUES
 ('PM001', DATEADD(DAY,-150,@today), N'Chuyển khoản', 4500000, 'SUB001', 'ST002'),
 ('PM002', DATEADD(DAY, -60,@today), N'Tiền mặt',     1350000, 'SUB002', 'ST002'),
 ('PM003', DATEADD(DAY,-120,@today), N'Thẻ tín dụng',  500000, 'SUB003', 'ST003'),
 ('PM004', DATEADD(DAY, -30,@today), N'Chuyển khoản', 2500000, 'SUB004', 'ST003'),
 ('PM005', DATEADD(DAY, -90,@today), N'Tiền mặt',      800000, 'SUB005', 'ST002'),
 ('PM006', DATEADD(DAY,  -1,@today), N'Chuyển khoản', 1350000, 'SUB006', 'ST003');

INSERT INTO Checkin (CheckinID, CheckinTime, CheckoutTime, MemberID, ReceptionistID) VALUES
 ('CK001', DATEADD(MINUTE, -1005,@now), DATEADD(MINUTE, -915,@now), 'MB001', 'ST002'),
 ('CK002', DATEADD(MINUTE,  -930,@now), DATEADD(MINUTE, -840,@now), 'MB002', 'ST002'),
 ('CK003', DATEADD(MINUTE,  -900,@now), DATEADD(MINUTE, -810,@now), 'MB004', 'ST002'),
 ('CK004', DATEADD(MINUTE,  -520,@now), DATEADD(MINUTE, -420,@now), 'MB006', 'ST003'),
 ('CK005', @in1,                       @current,                   'MB002', 'ST002'),
 ('CK006', @in2,                       NULL,                       'MB001', 'ST002');

INSERT INTO Trainer (TrainerID, FullName, Phone, ExperienceYears) VALUES
 ('TR001', N'Nguyễn Tuấn Dũng', '0911223344', 6),
 ('TR002', N'Trần Mai Anh',     '0922334455', 4),
 ('TR003', N'Lê Văn Hùng',      '0933445566', 5),
 ('TR004', N'Đặng Thị Thảo',    '0944556677', 3);

-- Tài khoản mẫu (chỉ Quản lý và Lễ tân), mật khẩu đều là 123456 (PBKDF2-HMAC-SHA256, 120000 vòng, salt riêng)
INSERT INTO UserAccount (Username, PasswordHash, PasswordSalt, Iterations, MustChangePassword, StaffID) VALUES
 ('admin', 0x8a5ff77e83d040a614e207d0270d1faefdfb87678a7511930a7e8082085348ed, 0xb074204d299b25065f00f16f2354fae3, 120000, 0, 'ST001'),
 ('letan1', 0x6bcca1df932473bb33b41543d1cacff0eed6ba2f44db4013f6d90b75db3c430b, 0x5514c96d11cd90f23032dad3a9e9c3f0, 120000, 0, 'ST002'),
 ('letan2', 0xbd40bb2e10cce8d5689b9c348179fa24b3330aec87392457bb1783db8f875b8c, 0xf59d8dfa67f84ceafb4d0fcf04c87d19, 120000, 0, 'ST003');

INSERT INTO Specialty (SpecialtyName) VALUES
 (N'Thể hình'), (N'Tăng cơ'), (N'Giảm mỡ'), (N'Pilates'), (N'Yoga'),
 (N'CrossFit'), (N'Kickboxing'), (N'Cardio'), (N'Phục hồi chức năng');

INSERT INTO TrainerSpecialty (TrainerID, SpecialtyID)
SELECT v.TrainerID, s.SpecialtyID
FROM (VALUES
 ('TR001', N'Thể hình'), ('TR001', N'Tăng cơ'),
 ('TR002', N'Giảm mỡ'),  ('TR002', N'Pilates'), ('TR002', N'Yoga'),
 ('TR003', N'CrossFit'), ('TR003', N'Kickboxing'),
 ('TR004', N'Cardio'),   ('TR004', N'Phục hồi chức năng')
) v(TrainerID, SpecialtyName)
JOIN Specialty s ON s.SpecialtyName = v.SpecialtyName;


INSERT INTO PTSession (SessionID, StartTime, EndTime, Status, TrainerID, MemberID) VALUES
 ('SES001', DATEADD(HOUR,  8,@now), DATEADD(MINUTE,  570,@now), N'Đã đặt',       'TR001', 'MB001'),
 ('SES002', DATEADD(HOUR, 10,@now), DATEADD(MINUTE,  690,@now), N'Đang diễn ra', 'TR002', 'MB002'),
 ('SES003', DATEADD(HOUR, 14,@now), DATEADD(MINUTE,  930,@now), N'Hoàn thành',   'TR003', 'MB003'),
 ('SES004', DATEADD(MINUTE, 990,@now), DATEADD(HOUR, 18,@now),  N'Đã đặt',       'TR004', 'MB004'),
 ('SES005', DATEADD(MINUTE,1110,@now), DATEADD(HOUR, 20,@now),  N'Đã đặt',       'TR001', 'MB005');

INSERT INTO Equipment (EquipmentID, EquipmentName, PurchaseValue, PurchaseDate, Status) VALUES
 ('EQ01', N'Máy chạy bộ Technogym Skillrun', 85000000, '2023-01-10', N'Hoạt động tốt'),
 ('EQ02', N'Xe đạp tập LifeFitness IC7',     45000000, '2023-01-15', N'Hoạt động tốt'),
 ('EQ03', N'Khung gánh tạ Smith Machine',    38000000, '2023-02-01', N'Hoạt động tốt'),
 ('EQ04', N'Ghế đẩy ngực bằng Impulse',      18000000, '2023-02-10', N'Tới hạn bảo trì định kỳ'),
 ('EQ05', N'Máy kéo xô đôi Cable Crossover', 55000000, '2023-03-05', N'Hoạt động tốt'),
 ('EQ06', N'Máy đạp đùi Leg Press 45 độ',    42000000, '2023-03-12', N'Cần thay thế linh kiện');

INSERT INTO MaintenanceAssignment (StaffID, EquipmentID, MaintenanceDate, Note, Status) VALUES
 ('ST004', 'EQ04', DATEADD(DAY, -5,@today), N'Thay đệm bọc ghế và tra dầu khớp trượt',      N'Đang bảo trì'),
 ('ST004', 'EQ06', DATEADD(DAY, -1,@today), N'Kiểm tra cáp chịu tải và chốt hãm',           N'Chờ linh kiện'),
 ('ST004', 'EQ01', DATEADD(DAY,-25,@today), N'Bảo dưỡng động cơ và cân chỉnh băng chuyền',  N'Đã hoàn thành');

COMMIT;
GO
