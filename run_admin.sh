#!/bin/bash
# Script chạy ứng dụng quản trị

echo "Đang khởi động ứng dụng quản trị..."

# Lấy đường dẫn thư mục hiện tại của script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Tạo thư mục bin nếu chưa có
mkdir -p bin/admin/gui

# Compile với classpath bao gồm các thư viện
echo "Đang compile..."
javac -d bin -cp "lib/*:src" src/admin/gui/*.java

# Chạy ứng dụng
echo "Khởi chạy AdminMainFrame..."
java -cp "bin:lib/*" admin.gui.AdminMainFrame
