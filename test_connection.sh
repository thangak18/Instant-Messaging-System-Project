#!/bin/bash

# Script để test kết nối database

echo "=========================================="
echo "Testing Database Connection..."
echo "=========================================="
echo ""

# Kiểm tra file config
if [ ! -f "release/config.properties" ]; then
    echo "❌ File config.properties không tồn tại!"
    exit 1
fi

# Kiểm tra password có placeholder không
if grep -q "\[YOUR_PASSWORD\]" release/config.properties || grep -q "YOUR_PASSWORD" release/config.properties; then
    echo "⚠️  Cảnh báo: Password vẫn là placeholder!"
    echo "Vui lòng điền Database Password thực tế vào file release/config.properties"
    echo ""
    echo "Cách lấy password:"
    echo "1. Vào Supabase Dashboard -> Settings -> Database"
    echo "2. Click 'Reset database password'"
    echo "3. Copy password và thay vào file config.properties"
    echo ""
    exit 1
fi

# Test connection
echo "Đang test kết nối..."
java -cp "bin:lib/*" admin.service.DatabaseConnection

echo ""
echo "=========================================="

