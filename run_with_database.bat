@echo off
chcp 65001 >nul
color 0A
cls

echo ╔══════════════════════════════════════════════════════╗
echo ║  COMPILE VÀ CHẠY ỨNG DỤNG CHAT - VỚI DATABASE       ║
echo ╚══════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [1/3] Compiling với MySQL Driver...
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar" -d bin src\user\service\*.java src\user\gui\*.java

if errorlevel 1 (
    echo.
    echo ❌ COMPILE FAILED!
    pause
    exit /b 1
)

echo ✅ Compile thành công!
echo.
echo [2/3] Kiểm tra kết nối database...
java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar" user.service.TestDatabaseConnection

echo.
echo [3/3] Khởi động ứng dụng...
echo.
pause

start "Chat System" java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar" user.gui.Main

echo.
echo ✅ Ứng dụng đã khởi động!
timeout /t 2 >nul
