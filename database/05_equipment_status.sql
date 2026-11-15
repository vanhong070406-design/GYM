/* Đổi trạng thái thiết bị sang 3 giá trị: Hoạt động tốt, Cần thay thế linh kiện, Tới hạn bảo trì định kỳ.
   Dữ liệu cũ: 'Tốt' -> 'Hoạt động tốt'; 'Đang bảo trì' -> 'Tới hạn bảo trì định kỳ' (thiết bị đó vẫn còn phiếu bảo trì đang mở).
   Chạy lại nhiều lần không sao: database đã đổi sẽ được bỏ qua. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Equipment_Status' AND definition LIKE N'%Hoạt động tốt%')
BEGIN
    PRINT N'Trạng thái thiết bị đã ở bản mới, không cần nâng cấp.';
    SET NOEXEC ON;
END
GO
BEGIN TRANSACTION;
IF OBJECT_ID('CK_Equipment_Status') IS NOT NULL
    ALTER TABLE Equipment DROP CONSTRAINT CK_Equipment_Status;
UPDATE Equipment SET Status = N'Hoạt động tốt' WHERE Status = N'Tốt';
UPDATE Equipment SET Status = N'Tới hạn bảo trì định kỳ' WHERE Status = N'Đang bảo trì';
ALTER TABLE Equipment ADD CONSTRAINT CK_Equipment_Status
    CHECK (Status IN (N'Hoạt động tốt', N'Cần thay thế linh kiện', N'Tới hạn bảo trì định kỳ'));
COMMIT;
PRINT N'Đã đổi trạng thái thiết bị sang bản mới.';
GO
SET NOEXEC OFF;
GO
