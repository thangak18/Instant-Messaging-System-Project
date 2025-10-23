-- Script tối ưu hóa cơ sở dữ liệu
-- Chạy định kỳ để tối ưu hiệu suất

USE chat_system;

-- Tối ưu hóa các bảng
OPTIMIZE TABLE users;
OPTIMIZE TABLE login_history;
OPTIMIZE TABLE friendships;
OPTIMIZE TABLE chat_groups;
OPTIMIZE TABLE group_members;
OPTIMIZE TABLE private_messages;
OPTIMIZE TABLE group_messages;
OPTIMIZE TABLE spam_reports;
OPTIMIZE TABLE user_activities;

-- Phân tích thống kê bảng
ANALYZE TABLE users;
ANALYZE TABLE login_history;
ANALYZE TABLE friendships;
ANALYZE TABLE chat_groups;
ANALYZE TABLE group_members;
ANALYZE TABLE private_messages;
ANALYZE TABLE group_messages;
ANALYZE TABLE spam_reports;
ANALYZE TABLE user_activities;

-- Kiểm tra kích thước database
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'chat_system'
ORDER BY (data_length + index_length) DESC;

-- Kiểm tra các index
SHOW INDEX FROM users;
SHOW INDEX FROM private_messages;
SHOW INDEX FROM group_messages;

-- Xóa dữ liệu cũ (tùy chọn - cẩn thận!)
-- Xóa lịch sử đăng nhập cũ hơn 1 năm
-- DELETE FROM login_history WHERE login_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Xóa tin nhắn cũ hơn 2 năm
-- DELETE FROM private_messages WHERE sent_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);
-- DELETE FROM group_messages WHERE sent_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);

-- Xóa hoạt động cũ hơn 6 tháng
-- DELETE FROM user_activities WHERE created_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
