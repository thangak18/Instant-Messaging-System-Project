#!/bin/bash
# Script chạy giao diện đăng nhập

echo "Đang khởi động giao diện đăng nhập..."
cd /Users/nguyenphunghoangnhi/Downloads/HeThongChat
javac src/user/gui/*.java
java -cp src/user/gui LoginFrame
