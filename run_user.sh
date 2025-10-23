#!/bin/bash
# Script chạy ứng dụng người dùng

echo "Đang khởi động ứng dụng người dùng..."
cd /Users/nguyenphunghoangnhi/Downloads/HeThongChat
javac src/user/gui/*.java
java -cp src/user/gui UserMainFrame
