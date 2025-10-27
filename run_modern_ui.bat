@echo off
cls
color 0B
echo ========================================
echo   INSTANT CHAT - GIAO DIEN HIEN DAI
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] Dang compile code...
javac -d bin src/user/gui/*.java 2>nul

if errorlevel 1 (
    echo.
    echo [ERROR] Compile that bai! Xem chi tiet:
    javac -d bin src/user/gui/*.java
    pause
    exit /b 1
)

echo [2/2] Compile thanh cong!
echo.
echo Dang khoi dong Instant Chat...
start "InstantChat" java -cp bin user.gui.Main

echo.
echo ========================================
echo   DA KHOI DONG THANH CONG!
echo ========================================
timeout /t 2 >nul
