@echo off
echo ========================================
echo   RUN INSTANT CHAT WITH EMAIL
echo ========================================
echo.

echo Starting application...
java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar;lib\activation.jar" user.gui.Main

pause
