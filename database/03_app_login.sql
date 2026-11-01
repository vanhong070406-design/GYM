/* Tài khoản ứng dụng chỉ có quyền đọc/ghi dữ liệu trong GymManagementDB.
   Mật khẩu truyền qua biến sqlcmd: -v GYM_APP_PASSWORD="..." */
USE master;
IF SUSER_ID(N'gym_app') IS NULL
    CREATE LOGIN gym_app WITH PASSWORD = N'$(GYM_APP_PASSWORD)', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF,
                              DEFAULT_DATABASE = GymManagementDB;
ELSE
    ALTER LOGIN gym_app WITH PASSWORD = N'$(GYM_APP_PASSWORD)', DEFAULT_DATABASE = GymManagementDB;
GO
USE GymManagementDB;
IF USER_ID(N'gym_app') IS NULL
    CREATE USER gym_app FOR LOGIN gym_app;
ALTER ROLE db_datareader ADD MEMBER gym_app;
ALTER ROLE db_datawriter ADD MEMBER gym_app;
GO
