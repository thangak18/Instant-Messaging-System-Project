-- Script giám sát và theo dõi hiệu suất database
-- Chạy định kỳ để kiểm tra tình trạng database

USE chat_system;

-- 1. Kiểm tra tình trạng tổng quan
SELECT 'Kiểm tra tình trạng tổng quan' AS check_name;

-- Thông tin về database
SELECT 
    'Database Info' AS info_type,
    DATABASE() AS database_name,
    VERSION() AS mysql_version,
    NOW() AS current_time;

-- Kích thước database
SELECT 
    'Database Size' AS info_type,
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS size_mb,
    ROUND(SUM(data_length) / 1024 / 1024, 2) AS data_size_mb,
    ROUND(SUM(index_length) / 1024 / 1024, 2) AS index_size_mb
FROM information_schema.tables
WHERE table_schema = 'chat_system';

-- 2. Kiểm tra số lượng bản ghi
SELECT 'Kiểm tra số lượng bản ghi' AS check_name;

SELECT 
    'users' AS table_name,
    COUNT(*) AS record_count,
    COUNT(CASE WHEN status = 'active' THEN 1 END) AS active_count,
    COUNT(CASE WHEN status = 'locked' THEN 1 END) AS locked_count
FROM users
UNION ALL
SELECT 
    'friendships',
    COUNT(*),
    COUNT(CASE WHEN status = 'accepted' THEN 1 END),
    COUNT(CASE WHEN status = 'pending' THEN 1 END)
FROM friendships
UNION ALL
SELECT 
    'private_messages',
    COUNT(*),
    COUNT(CASE WHEN is_read = TRUE THEN 1 END),
    COUNT(CASE WHEN is_read = FALSE THEN 1 END)
FROM private_messages
UNION ALL
SELECT 
    'group_messages',
    COUNT(*),
    NULL,
    NULL
FROM group_messages
UNION ALL
SELECT 
    'chat_groups',
    COUNT(*),
    NULL,
    NULL
FROM chat_groups;

-- 3. Kiểm tra hiệu suất truy vấn
SELECT 'Kiểm tra hiệu suất truy vấn' AS check_name;

-- Thống kê về các truy vấn chậm
SELECT 
    'Slow Queries' AS info_type,
    COUNT(*) AS slow_query_count
FROM information_schema.processlist
WHERE time > 5 AND command != 'Sleep';

-- Thống kê về kết nối
SELECT 
    'Connections' AS info_type,
    COUNT(*) AS total_connections,
    COUNT(CASE WHEN command = 'Sleep' THEN 1 END) AS idle_connections,
    COUNT(CASE WHEN command != 'Sleep' THEN 1 END) AS active_connections
FROM information_schema.processlist;

-- 4. Kiểm tra index
SELECT 'Kiểm tra index' AS check_name;

-- Thống kê về index
SELECT 
    table_name,
    index_name,
    column_name,
    cardinality,
    ROUND(cardinality / COUNT(*) * 100, 2) AS selectivity_percent
FROM information_schema.statistics s
JOIN (
    SELECT table_name, COUNT(*) as total_rows
    FROM information_schema.tables
    WHERE table_schema = 'chat_system'
) t ON s.table_name = t.table_name
WHERE s.table_schema = 'chat_system'
GROUP BY table_name, index_name, column_name, cardinality
ORDER BY selectivity_percent DESC;

-- 5. Kiểm tra dữ liệu bất thường
SELECT 'Kiểm tra dữ liệu bất thường' AS check_name;

-- Người dùng có nhiều bạn bè bất thường
SELECT 
    'Users with many friends' AS anomaly_type,
    u.username,
    u.full_name,
    COUNT(f.id) AS friend_count
FROM users u
LEFT JOIN friendships f ON (u.id = f.user1_id OR u.id = f.user2_id) AND f.status = 'accepted'
GROUP BY u.id, u.username, u.full_name
HAVING friend_count > 100
ORDER BY friend_count DESC;

-- Tin nhắn spam (nhiều tin nhắn trong thời gian ngắn)
SELECT 
    'Potential spam messages' AS anomaly_type,
    sender_id,
    COUNT(*) AS message_count,
    MIN(sent_at) AS first_message,
    MAX(sent_at) AS last_message,
    TIMESTAMPDIFF(MINUTE, MIN(sent_at), MAX(sent_at)) AS time_span_minutes
FROM private_messages
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY sender_id
HAVING message_count > 50
ORDER BY message_count DESC;

-- 6. Kiểm tra bảo mật
SELECT 'Kiểm tra bảo mật' AS check_name;

-- Người dùng có mật khẩu yếu (nếu có thể kiểm tra)
SELECT 
    'Weak passwords' AS security_issue,
    username,
    full_name,
    created_at
FROM users
WHERE LENGTH(password) < 8
   OR password = username
   OR password = 'password'
   OR password = '123456';

-- Báo cáo spam chưa xử lý
SELECT 
    'Unresolved spam reports' AS security_issue,
    COUNT(*) AS unresolved_count
FROM spam_reports
WHERE status = 'pending';

-- 7. Kiểm tra hiệu suất theo thời gian
SELECT 'Kiểm tra hiệu suất theo thời gian' AS check_name;

-- Thống kê đăng nhập theo giờ
SELECT 
    HOUR(login_time) AS hour_of_day,
    COUNT(*) AS login_count
FROM login_history
WHERE login_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY HOUR(login_time)
ORDER BY hour_of_day;

-- Thống kê tin nhắn theo giờ
SELECT 
    HOUR(sent_at) AS hour_of_day,
    COUNT(*) AS message_count
FROM private_messages
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY HOUR(sent_at)
ORDER BY hour_of_day;

-- 8. Kiểm tra tài nguyên hệ thống
SELECT 'Kiểm tra tài nguyên hệ thống' AS check_name;

-- Thông tin về bộ nhớ
SELECT 
    'Memory Usage' AS resource_type,
    ROUND(@@innodb_buffer_pool_size / 1024 / 1024, 2) AS buffer_pool_size_mb,
    ROUND(@@key_buffer_size / 1024 / 1024, 2) AS key_buffer_size_mb,
    ROUND(@@max_connections, 0) AS max_connections,
    ROUND(@@thread_cache_size, 0) AS thread_cache_size;

-- 9. Kiểm tra log và lỗi
SELECT 'Kiểm tra log và lỗi' AS check_name;

-- Thống kê lỗi kết nối
SELECT 
    'Connection Errors' AS error_type,
    COUNT(*) AS error_count
FROM information_schema.processlist
WHERE command = 'Connect' AND time > 10;

-- 10. Kiểm tra backup
SELECT 'Kiểm tra backup' AS check_name;

-- Kiểm tra thời gian backup gần nhất (nếu có bảng backup_log)
SELECT 
    'Last Backup' AS backup_info,
    MAX(backup_time) AS last_backup_time
FROM (
    SELECT created_at AS backup_time FROM users WHERE username = 'admin'
    UNION ALL
    SELECT MAX(created_at) AS backup_time FROM login_history
) AS backup_check;

-- 11. Tạo báo cáo tổng hợp
SELECT 'Báo cáo tổng hợp' AS report_type;

-- Báo cáo hoạt động trong 24h
SELECT 
    '24h Activity Report' AS report_name,
    COUNT(DISTINCT lh.user_id) AS unique_logins,
    COUNT(lh.id) AS total_logins,
    COUNT(pm.id) AS total_messages,
    COUNT(DISTINCT pm.sender_id) AS unique_message_senders,
    COUNT(sr.id) AS spam_reports
FROM login_history lh
LEFT JOIN private_messages pm ON DATE(pm.sent_at) = DATE(lh.login_time)
LEFT JOIN spam_reports sr ON DATE(sr.created_at) = DATE(lh.login_time)
WHERE lh.login_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- 12. Kiểm tra tính toàn vẹn dữ liệu
SELECT 'Kiểm tra tính toàn vẹn dữ liệu' AS check_name;

-- Kiểm tra foreign key constraints
SELECT 
    'Foreign Key Issues' AS integrity_check,
    COUNT(*) AS orphaned_records
FROM friendships f
LEFT JOIN users u1 ON f.user1_id = u1.id
LEFT JOIN users u2 ON f.user2_id = u2.id
WHERE u1.id IS NULL OR u2.id IS NULL;

-- 13. Tạo alert nếu có vấn đề
SELECT 'Tạo alert nếu có vấn đề' AS check_name;

-- Alert nếu có quá nhiều kết nối
SELECT 
    CASE 
        WHEN COUNT(*) > 80 THEN 'WARNING: High connection count'
        WHEN COUNT(*) > 90 THEN 'CRITICAL: Very high connection count'
        ELSE 'OK: Connection count normal'
    END AS connection_alert
FROM information_schema.processlist;

-- Alert nếu có quá nhiều tin nhắn spam
SELECT 
    CASE 
        WHEN COUNT(*) > 100 THEN 'WARNING: High spam message count'
        WHEN COUNT(*) > 200 THEN 'CRITICAL: Very high spam message count'
        ELSE 'OK: Spam message count normal'
    END AS spam_alert
FROM private_messages
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY sender_id
HAVING COUNT(*) > 50;
