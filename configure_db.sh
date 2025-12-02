#!/bin/bash

# Script để cấu hình database connection cho Supabase
# Hướng dẫn: https://supabase.com/dashboard/project/_/settings/database

echo "=========================================="
echo "Cấu hình Database Connection"
echo "=========================================="
echo ""
echo "Bạn cần lấy thông tin từ Supabase Dashboard:"
echo "1. Vào Settings > Database"
echo "2. Copy Connection string (URI)"
echo "3. Hoặc lấy Project Reference và Password"
echo ""

# Kiểm tra file config.properties
CONFIG_FILE="release/config.properties"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Tạo file config.properties từ template..."
    cp release/config.properties.example "$CONFIG_FILE"
fi

# Nhập Project Reference
echo "Nhập Supabase Project Reference:"
echo "(Ví dụ: nếu URL là db.abcdefghijklmno.supabase.co thì nhập: abcdefghijklmno)"
read -p "Project Reference: " PROJECT_REF

if [ -z "$PROJECT_REF" ]; then
    echo "❌ Project Reference không được để trống!"
    exit 1
fi

# Nhập Password
echo ""
echo "Nhập Database Password:"
read -s -p "Password: " DB_PASSWORD
echo ""

if [ -z "$DB_PASSWORD" ]; then
    echo "❌ Password không được để trống!"
    exit 1
fi

# Cập nhật file config.properties
echo ""
echo "Đang cập nhật file config.properties..."

# Backup file cũ
cp "$CONFIG_FILE" "$CONFIG_FILE.backup"

# Escape special characters trong password
ESCAPED_PASSWORD=$(echo "$DB_PASSWORD" | sed 's/[[\.*^$()+?{|]/\\&/g')

# Cập nhật các giá trị (macOS compatible)
sed -i '' "s/db.host=.*/db.host=db.${PROJECT_REF}.supabase.co/" "$CONFIG_FILE"
sed -i '' "s|db.url=.*|db.url=jdbc:postgresql://db.${PROJECT_REF}.supabase.co:5432/postgres?sslmode=require|" "$CONFIG_FILE"
sed -i '' "s|db.password=.*|db.password=${ESCAPED_PASSWORD}|" "$CONFIG_FILE"
sed -i '' "s|supabase.url=.*|supabase.url=https://${PROJECT_REF}.supabase.co|" "$CONFIG_FILE"

echo "✅ Đã cập nhật file config.properties!"
echo ""
echo "Thông tin đã cấu hình:"
echo "  - Host: db.${PROJECT_REF}.supabase.co"
echo "  - Username: postgres"
echo "  - Password: [đã ẩn]"
echo ""
echo "Bạn có muốn test kết nối ngay không? (y/n)"
read -p "Test connection: " TEST_CONN

if [ "$TEST_CONN" = "y" ] || [ "$TEST_CONN" = "Y" ]; then
    echo ""
    echo "Đang test kết nối..."
    java -cp "bin:lib/*" admin.service.DatabaseConnection 2>&1
fi

echo ""
echo "=========================================="
echo "Hoàn tất!"
echo "=========================================="
echo ""
echo "Lưu ý:"
echo "- File config.properties đã được backup tại: ${CONFIG_FILE}.backup"
echo "- File này KHÔNG được commit lên GitHub (đã có trong .gitignore)"
echo "- Nếu cần thay đổi, chạy lại script này hoặc sửa trực tiếp file"

