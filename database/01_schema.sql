/* =====================================================================
   GymManagementDB - Lược đồ chuẩn hóa BCNF (SQL Server 2017+)
   Chạy bằng database/setup_database.ps1, hoặc mở trong SSMS.
   Giải thích phụ thuộc hàm từng bảng: docs/DATABASE_DESIGN.md
   ===================================================================== */
SET NOCOUNT ON;
GO
IF DB_ID(N'GymManagementDB') IS NOT NULL
BEGIN
    RAISERROR(N'GymManagementDB đã tồn tại. Xóa trước (setup_database.ps1 -Reset) nếu muốn tạo lại.', 16, 1);
    SET NOEXEC ON;
END
GO
CREATE DATABASE GymManagementDB COLLATE Vietnamese_100_CI_AS;
GO
USE GymManagementDB;
GO

/* ---------- Nhân sự & phân quyền ---------- */
CREATE TABLE Position (
    PositionID   VARCHAR(5)   NOT NULL CONSTRAINT PK_Position PRIMARY KEY,
    PositionName NVARCHAR(50) NOT NULL CONSTRAINT UQ_Position_Name UNIQUE,
    CanLogin     BIT          NOT NULL CONSTRAINT DF_Position_CanLogin DEFAULT 0   -- 1: nhân viên chức vụ này được cấp tài khoản
);

-- Không có dòng = không thấy màn hình. VIEW: chỉ xem; MANAGE: toàn quyền
CREATE TABLE PositionPermission (
    PositionID  VARCHAR(5)  NOT NULL CONSTRAINT FK_PositionPermission_Position REFERENCES Position(PositionID),
    ScreenCode  VARCHAR(20) NOT NULL CONSTRAINT CK_PositionPermission_Screen CHECK (ScreenCode IN
                ('DASHBOARD','MEMBER','PACKAGE','SUBSCRIPTION','PAYMENT','CHECKIN','TRAINER','PTSESSION',
                 'STAFF','EQUIPMENT','MAINTENANCE')),
    AccessLevel VARCHAR(10) NOT NULL CONSTRAINT CK_PositionPermission_Level CHECK (AccessLevel IN ('VIEW','MANAGE')),
    CONSTRAINT PK_PositionPermission PRIMARY KEY (PositionID, ScreenCode)
);

CREATE TABLE Shift (
    ShiftID    VARCHAR(10)  NOT NULL CONSTRAINT PK_Shift PRIMARY KEY,
    ShiftName  NVARCHAR(50) NOT NULL CONSTRAINT UQ_Shift_Name UNIQUE,
    StartTime  TIME(0)      NOT NULL,
    EndTime    TIME(0)      NOT NULL   -- Ca đêm kết thúc sau nửa đêm nên không ràng buộc End > Start
);

CREATE TABLE Staff (
    StaffID    VARCHAR(10)   NOT NULL CONSTRAINT PK_Staff PRIMARY KEY,
    FullName   NVARCHAR(100) NOT NULL,
    Phone      VARCHAR(15)   NOT NULL CONSTRAINT UQ_Staff_Phone UNIQUE,
    Salary     DECIMAL(15,2) NOT NULL CONSTRAINT CK_Staff_Salary CHECK (Salary >= 0),
    ShiftID    VARCHAR(10)   NOT NULL CONSTRAINT FK_Staff_Shift REFERENCES Shift(ShiftID),
    HireDate   DATE          NOT NULL,
    PositionID VARCHAR(5)    NOT NULL CONSTRAINT FK_Staff_Position REFERENCES Position(PositionID),
    CONSTRAINT CK_Staff_NotTrainerPosition CHECK (PositionID <> 'HLV')   -- HLV nằm ở bảng Trainer
);

/* ---------- Hội viên & gói tập ---------- */
CREATE TABLE Member (
    MemberID   VARCHAR(10)   NOT NULL CONSTRAINT PK_Member PRIMARY KEY,
    FullName   NVARCHAR(100) NOT NULL,
    Email      VARCHAR(100)  NULL,
    Phone      VARCHAR(15)   NOT NULL CONSTRAINT UQ_Member_Phone UNIQUE,
    JoinDate   DATE          NOT NULL,
    Address    NVARCHAR(200) NULL,
    BirthDate  DATE          NULL,
    Gender     NVARCHAR(5)   NOT NULL CONSTRAINT CK_Member_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác'))
);
-- Email không bắt buộc nhưng nếu có thì là khóa dự tuyển
CREATE UNIQUE INDEX UQ_Member_Email ON Member(Email) WHERE Email IS NOT NULL;

CREATE TABLE GymPackage (
    PackageID    VARCHAR(10)   NOT NULL CONSTRAINT PK_GymPackage PRIMARY KEY,
    PackageName  NVARCHAR(100) NOT NULL CONSTRAINT UQ_GymPackage_Name UNIQUE,
    Price        DECIMAL(15,2) NOT NULL CONSTRAINT CK_GymPackage_Price CHECK (Price >= 0),
    DurationDays INT           NOT NULL CONSTRAINT CK_GymPackage_Duration CHECK (DurationDays > 0),
    Description  NVARCHAR(500) NULL
);

CREATE TABLE Subscription (
    SubscriptionID VARCHAR(10)  NOT NULL CONSTRAINT PK_Subscription PRIMARY KEY,
    MemberID       VARCHAR(10)  NOT NULL CONSTRAINT FK_Subscription_Member  REFERENCES Member(MemberID),
    PackageID      VARCHAR(10)  NOT NULL CONSTRAINT FK_Subscription_Package REFERENCES GymPackage(PackageID),
    Price          DECIMAL(15,2) NOT NULL CONSTRAINT CK_Subscription_Price CHECK (Price >= 0),  -- giá gói chốt lúc đăng ký; gói đổi giá sau đó không ảnh hưởng
    StartDate      DATE         NOT NULL,
    EndDate        DATE         NOT NULL,
    Status         NVARCHAR(20) NOT NULL CONSTRAINT CK_Subscription_Status
                                CHECK (Status IN (N'Đang hoạt động', N'Hết hạn', N'Tạm dừng')),
    CONSTRAINT CK_Subscription_Dates CHECK (EndDate >= StartDate),
    CONSTRAINT UQ_Subscription_Natural UNIQUE (MemberID, PackageID, StartDate)
);

CREATE TABLE Payment (
    PaymentID      VARCHAR(10)   NOT NULL CONSTRAINT PK_Payment PRIMARY KEY,
    PaymentDate    DATE          NOT NULL,
    PaymentMethod  NVARCHAR(20)  NOT NULL CONSTRAINT CK_Payment_Method
                                 CHECK (PaymentMethod IN (N'Tiền mặt', N'Chuyển khoản', N'Thẻ tín dụng')),
    Amount         DECIMAL(15,2) NOT NULL CONSTRAINT CK_Payment_Amount CHECK (Amount > 0),
    SubscriptionID VARCHAR(10)   NOT NULL CONSTRAINT FK_Payment_Subscription REFERENCES Subscription(SubscriptionID),
    ReceptionistID VARCHAR(10)   NOT NULL CONSTRAINT FK_Payment_Staff        REFERENCES Staff(StaffID)
);
CREATE INDEX IX_Payment_Subscription ON Payment(SubscriptionID);
CREATE INDEX IX_Payment_Date         ON Payment(PaymentDate);

CREATE TABLE Checkin (
    CheckinID      VARCHAR(10)  NOT NULL CONSTRAINT PK_Checkin PRIMARY KEY,
    CheckinTime    DATETIME2(0) NOT NULL,
    CheckoutTime   DATETIME2(0) NULL,     -- NULL = chưa check-out
    MemberID       VARCHAR(10)  NOT NULL CONSTRAINT FK_Checkin_Member REFERENCES Member(MemberID),
    ReceptionistID VARCHAR(10)  NOT NULL CONSTRAINT FK_Checkin_Staff  REFERENCES Staff(StaffID),
    CONSTRAINT CK_Checkin_Times CHECK (CheckoutTime IS NULL OR CheckoutTime >= CheckinTime),
    CONSTRAINT UQ_Checkin_Natural UNIQUE (MemberID, CheckinTime)
);
CREATE INDEX IX_Checkin_Time ON Checkin(CheckinTime);

/* ---------- Huấn luyện viên ---------- */
CREATE TABLE Trainer (
    TrainerID       VARCHAR(10)   NOT NULL CONSTRAINT PK_Trainer PRIMARY KEY,
    FullName        NVARCHAR(100) NOT NULL,
    Phone           VARCHAR(15)   NOT NULL CONSTRAINT UQ_Trainer_Phone UNIQUE,
    ExperienceYears INT           NOT NULL CONSTRAINT CK_Trainer_Experience CHECK (ExperienceYears >= 0)
);

CREATE TABLE Specialty (
    SpecialtyID   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Specialty PRIMARY KEY,
    SpecialtyName NVARCHAR(100)     NOT NULL CONSTRAINT UQ_Specialty_Name UNIQUE
);

CREATE TABLE TrainerSpecialty (
    TrainerID   VARCHAR(10) NOT NULL CONSTRAINT FK_TrainerSpecialty_Trainer
                            REFERENCES Trainer(TrainerID) ON DELETE CASCADE,
    SpecialtyID INT         NOT NULL CONSTRAINT FK_TrainerSpecialty_Specialty
                            REFERENCES Specialty(SpecialtyID),
    CONSTRAINT PK_TrainerSpecialty PRIMARY KEY (TrainerID, SpecialtyID)
);

/* ---------- Tài khoản: chỉ nhân viên có chức vụ CanLogin = 1 (Quản lý, Lễ tân) mới có ---------- */
CREATE TABLE UserAccount (
    Username           VARCHAR(50)   NOT NULL CONSTRAINT PK_UserAccount PRIMARY KEY,
    PasswordHash       VARBINARY(32) NOT NULL,   -- PBKDF2-HMAC-SHA256
    PasswordSalt       VARBINARY(16) NOT NULL,
    Iterations         INT           NOT NULL,
    MustChangePassword BIT           NOT NULL CONSTRAINT DF_UserAccount_MustChange DEFAULT 0,
    StaffID            VARCHAR(10)   NOT NULL CONSTRAINT FK_UserAccount_Staff REFERENCES Staff(StaffID) ON DELETE CASCADE
                                              CONSTRAINT UQ_UserAccount_Staff UNIQUE
);


CREATE TABLE PTSession (
    SessionID VARCHAR(10)  NOT NULL CONSTRAINT PK_PTSession PRIMARY KEY,
    StartTime DATETIME2(0) NOT NULL,
    EndTime   DATETIME2(0) NOT NULL,
    Status    NVARCHAR(20) NOT NULL CONSTRAINT CK_PTSession_Status
              CHECK (Status IN (N'Đã đặt', N'Đang diễn ra', N'Hoàn thành', N'Đã hủy')),
    TrainerID VARCHAR(10)  NOT NULL CONSTRAINT FK_PTSession_Trainer REFERENCES Trainer(TrainerID),
    MemberID  VARCHAR(10)  NOT NULL CONSTRAINT FK_PTSession_Member  REFERENCES Member(MemberID),
    CONSTRAINT CK_PTSession_Times CHECK (EndTime > StartTime),
    CONSTRAINT UQ_PTSession_Trainer_Start UNIQUE (TrainerID, StartTime),
    CONSTRAINT UQ_PTSession_Member_Start UNIQUE (MemberID, StartTime)
);

CREATE TABLE Equipment (
    EquipmentID   VARCHAR(10)   NOT NULL CONSTRAINT PK_Equipment PRIMARY KEY,
    EquipmentName NVARCHAR(150) NOT NULL,
    PurchaseValue DECIMAL(15,2) NOT NULL CONSTRAINT CK_Equipment_Value CHECK (PurchaseValue >= 0),
    PurchaseDate  DATE          NOT NULL,
    Status        NVARCHAR(30)  NOT NULL CONSTRAINT CK_Equipment_Status
                  CHECK (Status IN (N'Hoạt động tốt', N'Cần thay thế linh kiện', N'Tới hạn bảo trì định kỳ'))
);

CREATE TABLE MaintenanceAssignment (
    StaffID         VARCHAR(10)   NOT NULL CONSTRAINT FK_Maintenance_Staff     REFERENCES Staff(StaffID),
    EquipmentID     VARCHAR(10)   NOT NULL CONSTRAINT FK_Maintenance_Equipment REFERENCES Equipment(EquipmentID),
    MaintenanceDate DATE          NOT NULL,
    Note            NVARCHAR(300) NOT NULL,
    Status          NVARCHAR(20)  NOT NULL CONSTRAINT CK_Maintenance_Status
                    CHECK (Status IN (N'Đang bảo trì', N'Chờ linh kiện', N'Đã hoàn thành')),
    CONSTRAINT PK_MaintenanceAssignment PRIMARY KEY (StaffID, EquipmentID, MaintenanceDate)
);
GO

/* ---------- Quy tắc chức vụ đăng nhập (CHECK không tham chiếu được bảng khác nên dùng trigger) ---------- */
-- Không cấp tài khoản cho nhân viên có chức vụ CanLogin = 0
CREATE TRIGGER TR_UserAccount_CanLogin ON UserAccount AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i
               JOIN Staff s ON s.StaffID = i.StaffID
               JOIN Position p ON p.PositionID = s.PositionID
               WHERE p.CanLogin = 0)
        THROW 50001, N'Chức vụ của nhân viên này không được cấp tài khoản đăng nhập.', 1;
END
GO
-- Nhân viên còn tài khoản thì không chuyển sang chức vụ CanLogin = 0 (ứng dụng xóa tài khoản trước rồi mới đổi)
CREATE TRIGGER TR_Staff_CanLogin ON Staff AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(PositionID) AND EXISTS (SELECT 1 FROM inserted i
               JOIN UserAccount a ON a.StaffID = i.StaffID
               JOIN Position p ON p.PositionID = i.PositionID
               WHERE p.CanLogin = 0)
        THROW 50002, N'Nhân viên đang có tài khoản đăng nhập nên không chuyển được sang chức vụ này. Hãy xóa tài khoản trước.', 1;
END
GO
-- Không tắt quyền đăng nhập của một chức vụ khi còn nhân viên chức vụ đó có tài khoản
CREATE TRIGGER TR_Position_CanLogin ON Position AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(CanLogin) AND EXISTS (SELECT 1 FROM inserted i
               JOIN Staff s ON s.PositionID = i.PositionID
               JOIN UserAccount a ON a.StaffID = s.StaffID
               WHERE i.CanLogin = 0)
        THROW 50003, N'Còn nhân viên thuộc chức vụ này đang có tài khoản. Hãy xóa các tài khoản đó trước khi tắt quyền đăng nhập.', 1;
END
GO

SET NOEXEC OFF;
GO
