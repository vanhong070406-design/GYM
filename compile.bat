@echo off
setlocal
chcp 65001 >nul
echo ========================================================
echo   BIEN DICH DU AN QUAN LY PHONG GYM (JAVA SWING MVC)
echo ========================================================

cd /d "%~dp0"
if not exist bin mkdir bin

echo [1/2] Dang tim kiem va tong hop cac file ma nguon .java...
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src/main/java | Resolve-Path -Relative | Out-File -Encoding ASCII sources.txt"

echo [2/2] Dang bien dich ma nguon vao thu muc bin...
javac -encoding UTF-8 -d bin @sources.txt

if %errorlevel% neq 0 (
    echo [LOI] Bien dich that bai! Vui long kiem tra JDK.
    if exist sources.txt del sources.txt
    if [%1]==[] pause
    exit /b %errorlevel%
)

if exist sources.txt del sources.txt
echo [THANH CONG] Toan bo ma nguon da duoc bien dich thanh cong vao thu muc 'bin'!
echo Ban co the chay ung dung bang cach chay file 'run.bat'.
if [%1]==[] pause
