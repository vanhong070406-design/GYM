/* Chỉ Quản lý và Lễ tân được đăng nhập; kỹ thuật viên, tạp vụ, huấn luyện viên không có tài khoản.
   - Thêm cột Position.CanLogin (QL, LT = 1) để quy tắc nằm trong dữ liệu, không viết cứng trong code.
   - Xóa tài khoản HLV, tài khoản và quyền của các chức vụ không đăng nhập được; bỏ mức quyền OWN.
   - Bỏ cột UserAccount.TrainerID: tài khoản luôn thuộc về một nhân viên (StaffID bắt buộc).
   - Trigger chặn cấp tài khoản cho chức vụ không đăng nhập được, chặn đổi chức vụ khi còn tài khoản.
   Toàn bộ chạy trong một transaction: lỗi ở bước nào thì database giữ nguyên như cũ.
   Chạy lại nhiều lần không sao: database đã nâng cấp sẽ được bỏ qua. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
IF COL_LENGTH('Position', 'CanLogin') IS NOT NULL
BEGIN
    PRINT N'Database đã có quy tắc chức vụ đăng nhập, không cần nâng cấp.';
    SET NOEXEC ON;
END
GO
-- Câu lệnh dùng cột mới thêm hoặc tạo trigger phải chạy qua EXEC vì cả khối là một batch
BEGIN TRANSACTION;

ALTER TABLE Position ADD CanLogin BIT NOT NULL CONSTRAINT DF_Position_CanLogin DEFAULT 0;
EXEC(N'UPDATE Position SET CanLogin = 1 WHERE PositionID IN (''QL'', ''LT'');');

-- Dọn tài khoản và quyền không còn hợp lệ
DELETE FROM UserAccount WHERE StaffID IS NULL;
EXEC(N'DELETE a FROM UserAccount a
       JOIN Staff s ON s.StaffID = a.StaffID
       JOIN Position p ON p.PositionID = s.PositionID
       WHERE p.CanLogin = 0;');
EXEC(N'DELETE pp FROM PositionPermission pp
       JOIN Position p ON p.PositionID = pp.PositionID
       WHERE p.CanLogin = 0;');
DELETE FROM PositionPermission WHERE AccessLevel = 'OWN';
ALTER TABLE PositionPermission DROP CONSTRAINT CK_PositionPermission_Level;
ALTER TABLE PositionPermission ADD CONSTRAINT CK_PositionPermission_Level CHECK (AccessLevel IN ('VIEW', 'MANAGE'));

-- Tài khoản chỉ thuộc về nhân viên
ALTER TABLE UserAccount DROP CONSTRAINT CK_UserAccount_OneOwner;
DROP INDEX UQ_UserAccount_Trainer ON UserAccount;
ALTER TABLE UserAccount DROP CONSTRAINT FK_UserAccount_Trainer;
ALTER TABLE UserAccount DROP COLUMN TrainerID;
DROP INDEX UQ_UserAccount_Staff ON UserAccount;
ALTER TABLE UserAccount DROP CONSTRAINT FK_UserAccount_Staff;
ALTER TABLE UserAccount ALTER COLUMN StaffID VARCHAR(10) NOT NULL;
ALTER TABLE UserAccount ADD
    CONSTRAINT FK_UserAccount_Staff FOREIGN KEY (StaffID) REFERENCES Staff(StaffID) ON DELETE CASCADE,
    CONSTRAINT UQ_UserAccount_Staff UNIQUE (StaffID);

EXEC(N'CREATE TRIGGER TR_UserAccount_CanLogin ON UserAccount AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i
               JOIN Staff s ON s.StaffID = i.StaffID
               JOIN Position p ON p.PositionID = s.PositionID
               WHERE p.CanLogin = 0)
        THROW 50001, N''Chức vụ của nhân viên này không được cấp tài khoản đăng nhập.'', 1;
END');

EXEC(N'CREATE TRIGGER TR_Staff_CanLogin ON Staff AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(PositionID) AND EXISTS (SELECT 1 FROM inserted i
               JOIN UserAccount a ON a.StaffID = i.StaffID
               JOIN Position p ON p.PositionID = i.PositionID
               WHERE p.CanLogin = 0)
        THROW 50002, N''Nhân viên đang có tài khoản đăng nhập nên không chuyển được sang chức vụ này. Hãy xóa tài khoản trước.'', 1;
END');

EXEC(N'CREATE TRIGGER TR_Position_CanLogin ON Position AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(CanLogin) AND EXISTS (SELECT 1 FROM inserted i
               JOIN Staff s ON s.PositionID = i.PositionID
               JOIN UserAccount a ON a.StaffID = s.StaffID
               WHERE i.CanLogin = 0)
        THROW 50003, N''Còn nhân viên thuộc chức vụ này đang có tài khoản. Hãy xóa các tài khoản đó trước khi tắt quyền đăng nhập.'', 1;
END');

COMMIT;
PRINT N'Đã áp dụng quy tắc: chỉ Quản lý và Lễ tân có tài khoản đăng nhập.';
GO
SET NOEXEC OFF;
GO
