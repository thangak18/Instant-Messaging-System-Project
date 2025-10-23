#!/bin/bash
# Script chạy ứng dụng quản trị

echo "Đang khởi động ứng dụng quản trị..."
cd /Users/nguyenphunghoangnhi/Downloads/HeThongChat
javac src/admin/gui/*.java
java -cp src/admin/gui AdminMainFrame
