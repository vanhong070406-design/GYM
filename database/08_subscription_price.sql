/* Thêm cột Subscription.Price: giá gói chốt lúc đăng ký.
   Trước đây số tiền phải thu lấy theo giá hiện tại của gói (GymPackage.Price), nên khi gói đổi giá
   thì các đăng ký cũ, kể cả đã trả đủ hoặc đã hết hạn, bị tính nợ lại.
   Đăng ký đã có lấy giá hiện tại của gói (database không lưu giá cũ để khôi phục):
   nếu đã lỡ đổi giá gói khi chạy thử, hãy trả giá gói về như cũ TRƯỚC khi chạy script này.
   Chạy lại nhiều lần không sao: database đã có cột sẽ được bỏ qua. */
USE GymManagementDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
IF COL_LENGTH('Subscription', 'Price') IS NOT NULL
BEGIN
    PRINT N'Bảng Subscription đã có cột Price, không cần nâng cấp.';
    SET NOEXEC ON;
END
GO
BEGIN TRANSACTION;
ALTER TABLE Subscription ADD Price DECIMAL(15,2) NULL;
-- Cột vừa thêm nên các lệnh dùng nó phải chạy qua EXEC (biên dịch riêng sau khi cột đã có)
EXEC(N'UPDATE s SET Price = g.Price FROM Subscription s JOIN GymPackage g ON g.PackageID = s.PackageID;');
EXEC(N'ALTER TABLE Subscription ALTER COLUMN Price DECIMAL(15,2) NOT NULL;');
EXEC(N'ALTER TABLE Subscription ADD CONSTRAINT CK_Subscription_Price CHECK (Price >= 0);');
COMMIT;
PRINT N'Đã thêm cột Price vào bảng Subscription.';
GO
SET NOEXEC OFF;
GO
