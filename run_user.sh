#!/bin/bash
# Script chạy ứng dụng người dùng

echo "Đang khởi động ứng dụng người dùng..."

# Lấy đường dẫn thư mục hiện tại của script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Tạo thư mục bin nếu chưa có
mkdir -p bin/user/gui

# Compile với classpath bao gồm các thư viện
echo "Đang compile..."
javac -d bin -cp "lib/*:src" src/user/gui/*.java src/user/service/*.java src/user/socket/*.java

# Chạy ứng dụng
echo "Khởi chạy ZaloMainFrame..."
java -cp "bin:lib/*" user.gui.ZaloMainFrame
