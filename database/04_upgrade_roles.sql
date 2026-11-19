/* Nâng cấp GymManagementDB bản cũ lên bản có chức vụ, phân quyền, tài khoản nhân viên/HLV.
   Giữ nguyên dữ liệu. Chạy lại nhiều lần không sao: database đã nâng cấp sẽ được bỏ qua.
   Tài khoản mẫu mật khẩu 123456 chỉ tạo cho nhân viên/HLV có mã mẫu mà chưa có tài khoản. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
IF OBJECT_ID('Position') IS NOT NULL
BEGIN
    PRINT N'Database đã ở bản mới, không cần nâng cấp.';
    SET NOEXEC ON;
END
GO
BEGIN TRANSACTION;
GO
CREATE TABLE Position (
    PositionID   VARCHAR(5)   NOT NULL CONSTRAINT PK_Position PRIMARY KEY,
    PositionName NVARCHAR(50) NOT NULL CONSTRAINT UQ_Position_Name UNIQUE
);

-- Không có dòng = không thấy màn hình. VIEW: chỉ xem; OWN: chỉ thấy và cập nhật việc của mình; MANAGE: toàn quyền
CREATE TABLE PositionPermission (
    PositionID  VARCHAR(5)  NOT NULL CONSTRAINT FK_PositionPermission_Position REFERENCES Position(PositionID),
    ScreenCode  VARCHAR(20) NOT NULL CONSTRAINT CK_PositionPermission_Screen CHECK (ScreenCode IN
                ('DASHBOARD','MEMBER','PACKAGE','SUBSCRIPTION','PAYMENT','CHECKIN','TRAINER','PTSESSION',
                 'STAFF','AREA','EQUIPMENT','MAINTENANCE','CLEANING','ACCOUNT')),
    AccessLevel VARCHAR(10) NOT NULL CONSTRAINT CK_PositionPermission_Level CHECK (AccessLevel IN ('VIEW','OWN','MANAGE')),
    CONSTRAINT PK_PositionPermission PRIMARY KEY (PositionID, ScreenCode)
);

GO
INSERT INTO Position (PositionID, PositionName) VALUES
 ('QL',  N'Quản lý'),
 ('LT',  N'Lễ tân'),
 ('KT',  N'Kỹ thuật viên'),
 ('TV',  N'Tạp vụ'),
 ('HLV', N'Huấn luyện viên');
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
 ('LT', 'PTSESSION', 'MANAGE'),

-- Suy ra chức vụ từ dữ liệu đã có
ALTER TABLE Staff ADD PositionID VARCHAR(5) NULL;
GO
UPDATE s SET PositionID = CASE
    WHEN s.ManagerID IS NULL THEN 'QL'
    WHEN EXISTS (SELECT 1 FROM MaintenanceAssignment m WHERE m.StaffID = s.StaffID) THEN 'KT'
    WHEN EXISTS (SELECT 1 FROM AreaCleaning c WHERE c.StaffID = s.StaffID) THEN 'TV'
    ELSE 'LT' END
FROM Staff s;
ALTER TABLE Staff ALTER COLUMN PositionID VARCHAR(5) NOT NULL;
ALTER TABLE Staff ADD CONSTRAINT FK_Staff_Position FOREIGN KEY (PositionID) REFERENCES Position(PositionID);
ALTER TABLE Staff ADD CONSTRAINT CK_Staff_NotTrainerPosition CHECK (PositionID <> 'HLV');
GO
-- Tài khoản: cho phép thuộc về HLV
ALTER TABLE UserAccount DROP CONSTRAINT UQ_UserAccount_Staff;
ALTER TABLE UserAccount DROP CONSTRAINT FK_UserAccount_Staff;
ALTER TABLE UserAccount ALTER COLUMN StaffID VARCHAR(10) NULL;
ALTER TABLE UserAccount ADD MustChangePassword BIT NOT NULL CONSTRAINT DF_UserAccount_MustChange DEFAULT 0,
                            TrainerID VARCHAR(10) NULL;
GO
ALTER TABLE UserAccount ADD CONSTRAINT FK_UserAccount_Staff FOREIGN KEY (StaffID) REFERENCES Staff(StaffID) ON DELETE CASCADE;
ALTER TABLE UserAccount ADD CONSTRAINT FK_UserAccount_Trainer FOREIGN KEY (TrainerID) REFERENCES Trainer(TrainerID) ON DELETE CASCADE;
ALTER TABLE UserAccount ADD CONSTRAINT CK_UserAccount_OneOwner CHECK ((StaffID IS NULL AND TrainerID IS NOT NULL) OR (StaffID IS NOT NULL AND TrainerID IS NULL));
CREATE UNIQUE INDEX UQ_UserAccount_Staff   ON UserAccount(StaffID)   WHERE StaffID IS NOT NULL;
CREATE UNIQUE INDEX UQ_UserAccount_Trainer ON UserAccount(TrainerID) WHERE TrainerID IS NOT NULL;
GO
INSERT INTO UserAccount (Username, PasswordHash, PasswordSalt, Iterations, MustChangePassword, StaffID, TrainerID)
SELECT v.* FROM (VALUES
 ('admin', 0x8a5ff77e83d040a614e207d0270d1faefdfb87678a7511930a7e8082085348ed, 0xb074204d299b25065f00f16f2354fae3, 120000, 0, 'ST001', NULL),
 ('letan1', 0x6bcca1df932473bb33b41543d1cacff0eed6ba2f44db4013f6d90b75db3c430b, 0x5514c96d11cd90f23032dad3a9e9c3f0, 120000, 0, 'ST002', NULL),
 ('letan2', 0xbd40bb2e10cce8d5689b9c348179fa24b3330aec87392457bb1783db8f875b8c, 0xf59d8dfa67f84ceafb4d0fcf04c87d19, 120000, 0, 'ST003', NULL)
) v(Username, PasswordHash, PasswordSalt, Iterations, MustChangePassword, StaffID, TrainerID)
WHERE NOT EXISTS (SELECT 1 FROM UserAccount a WHERE a.Username = v.Username OR a.StaffID = v.StaffID OR a.TrainerID = v.TrainerID)
  AND (v.StaffID IS NULL OR EXISTS (SELECT 1 FROM Staff s WHERE s.StaffID = v.StaffID))
  AND (v.TrainerID IS NULL OR EXISTS (SELECT 1 FROM Trainer t WHERE t.TrainerID = v.TrainerID));
GO
-- Buổi PT: gán hội viên cho các buổi mẫu; buổi khác (nếu có) tạm gán hội viên đầu tiên, cần kiểm tra lại trên màn hình Lịch PT
ALTER TABLE PTSession ADD MemberID VARCHAR(10) NULL;
GO
UPDATE p SET MemberID = COALESCE(
    (SELECT m.MemberID FROM Member m WHERE m.MemberID = 'MB00' + RIGHT(p.SessionID, 1) AND p.SessionID IN ('SES001','SES002','SES003','SES004','SES005')),
    (SELECT MIN(MemberID) FROM Member))
FROM PTSession p;
ALTER TABLE PTSession ALTER COLUMN MemberID VARCHAR(10) NOT NULL;
ALTER TABLE PTSession ADD CONSTRAINT FK_PTSession_Member FOREIGN KEY (MemberID) REFERENCES Member(MemberID);
ALTER TABLE PTSession ADD CONSTRAINT UQ_PTSession_Member_Start UNIQUE (MemberID, StartTime);
GO
COMMIT;
PRINT N'Nâng cấp thành công.';
GO
SET NOEXEC OFF;
GO
