#!/bin/bash
# Script khởi động ChatServer

echo "Đang khởi động ChatServer..."

# Lấy đường dẫn thư mục hiện tại của script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Tạo thư mục bin nếu chưa có
mkdir -p bin

# Compile ChatServer và các dependencies
echo "Đang compile ChatServer..."
javac -encoding UTF-8 -d bin -sourcepath src -cp "lib/*" src/user/socket/*.java src/user/gui/Main.java

# Chạy ChatServer
echo "Khởi chạy ChatServer..."
java -cp "bin:lib/*" user.gui.Main
