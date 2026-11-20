/* Bỏ cột ManagerID của bảng Staff: hệ thống chỉ có 1 quản lý (chức vụ QL) nên không cần lưu ai quản lý ai.
   Xóa luôn khóa ngoại FK_Staff_Manager và ràng buộc CK_Staff_NotOwnManager đi kèm cột này.
   Chạy lại nhiều lần không sao: database đã bỏ cột sẽ được bỏ qua. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
IF COL_LENGTH('Staff', 'ManagerID') IS NULL
BEGIN
    PRINT N'Bảng Staff đã bỏ cột ManagerID, không cần nâng cấp.';
    SET NOEXEC ON;
END
GO
BEGIN TRANSACTION;
IF OBJECT_ID('FK_Staff_Manager') IS NOT NULL
    ALTER TABLE Staff DROP CONSTRAINT FK_Staff_Manager;
IF OBJECT_ID('CK_Staff_NotOwnManager') IS NOT NULL
    ALTER TABLE Staff DROP CONSTRAINT CK_Staff_NotOwnManager;
ALTER TABLE Staff DROP COLUMN ManagerID;
COMMIT;
PRINT N'Đã bỏ cột ManagerID khỏi bảng Staff.';
GO
SET NOEXEC OFF;
GO
