@echo off
echo ========================================
echo   COMPILE WITH EMAIL SUPPORT
echo ========================================
echo.

echo Compiling Java files with JavaMail and Activation...
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar;lib\activation.jar" -d bin src\user\service\*.java src\user\gui\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   COMPILE SUCCESS!
    echo ========================================
    echo.
    echo Ready to run with:
    echo   run_with_email.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   COMPILE FAILED!
    echo ========================================
    echo.
    pause
)
