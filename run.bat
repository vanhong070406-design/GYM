@echo off
setlocal
chcp 65001 >nul
echo ========================================================
echo   KHOI DONG PHAN MEM QUAN LY PHONG GYM (JAVA SWING MVC)
echo ========================================================

cd /d "%~dp0"

rem bin khong luu tren repo nen luon bien dich lai de chay dung ma nguon hien tai
call compile.bat 1
if errorlevel 1 (
    pause
    exit /b 1
)

echo [KHOI CHAY] Dang bat giao dien ung dung...
java -Dfile.encoding=UTF-8 -cp "bin;lib\*" com.gymmanagement.Main

if %errorlevel% neq 0 (
    echo [LOI] Khong the khoi chay chuong trinh. Vui long kiem tra lai moi truong Java.
    pause
)
