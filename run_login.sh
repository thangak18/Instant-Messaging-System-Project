#!/bin/bash
# Script chạy giao diện đăng nhập

echo "Đang khởi động giao diện đăng nhập..."

# Lấy đường dẫn thư mục hiện tại của script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Tạo thư mục bin nếu chưa có
mkdir -p bin/user/gui
mkdir -p bin/user/service

# Compile với classpath bao gồm các thư viện
echo "Đang compile..."
javac -d bin -cp "lib/*:src" src/user/gui/*.java src/user/service/*.java

# Chạy ứng dụng
echo "Khởi chạy LoginFrame..."
java -cp "bin:lib/*" user.gui.LoginFrame
