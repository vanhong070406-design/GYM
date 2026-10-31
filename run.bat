@echo off
setlocal
chcp 65001 >nul
echo ========================================================
echo   KHOI DONG PHAN MEM QUAN LY PHONG GYM (JAVA SWING MVC)
echo ========================================================

cd /d "%~dp0"

if not exist bin\com\gymmanagement\Main.class (
    echo [THONG BAO] Chua thay file bien dich. Dang tien hanh bien dich tu dong...
    call compile.bat 1
)

echo [KHOI CHAY] Dang bat giao dien ung dung...
java -Dfile.encoding=UTF-8 -cp bin com.gymmanagement.Main

if %errorlevel% neq 0 (
    echo [LOI] Khong the khoi chay chuong trinh. Vui long kiem tra lai moi truong Java.
    pause
)
