-- Script khôi phục cơ sở dữ liệu
-- Sử dụng để khôi phục dữ liệu từ bản sao lưu

-- Xóa database cũ (nếu tồn tại)
DROP DATABASE IF EXISTS chat_system;

-- Tạo database mới
CREATE DATABASE chat_system;
USE chat_system;

-- Khôi phục từ bản sao lưu
-- mysql -u root -p chat_system < backup_chat_system_YYYYMMDD_HHMMSS.sql

-- Hoặc khôi phục từng bước:
-- 1. Khôi phục cấu trúc
-- mysql -u root -p chat_system < backup_structure_YYYYMMDD_HHMMSS.sql

-- 2. Khôi phục dữ liệu
-- mysql -u root -p chat_system < backup_data_YYYYMMDD_HHMMSS.sql

-- Kiểm tra kết quả khôi phục
SELECT 'Database restored successfully' AS status;
SHOW TABLES;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS message_count FROM private_messages;
SELECT COUNT(*) AS group_count FROM chat_groups;
