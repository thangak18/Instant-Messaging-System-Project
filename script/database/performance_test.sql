-- Script test hiệu suất database
-- Sử dụng để kiểm tra hiệu suất của các truy vấn

USE chat_system;

-- 1. Test hiệu suất truy vấn cơ bản
SELECT 'Test hiệu suất truy vấn cơ bản' AS test_name;

-- Test SELECT đơn giản
SET @start_time = NOW(6);
SELECT COUNT(*) FROM users;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test SELECT với WHERE
SET @start_time = NOW(6);
SELECT * FROM users WHERE status = 'active';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test SELECT với JOIN
SET @start_time = NOW(6);
SELECT u.username, u.full_name, COUNT(f.id) AS friend_count
FROM users u
LEFT JOIN friendships f ON u.id = f.user1_id AND f.status = 'accepted'
GROUP BY u.id, u.username, u.full_name;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 2. Test hiệu suất truy vấn phức tạp
SELECT 'Test hiệu suất truy vấn phức tạp' AS test_name;

-- Test truy vấn với nhiều JOIN
SET @start_time = NOW(6);
SELECT u.username, u.full_name, cg.group_name, gm.role
FROM users u
JOIN group_members gm ON u.id = gm.user_id
JOIN chat_groups cg ON gm.group_id = cg.id
WHERE gm.role = 'admin';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test truy vấn với subquery
SET @start_time = NOW(6);
SELECT u.username, u.full_name,
       (SELECT COUNT(*) FROM private_messages pm WHERE pm.sender_id = u.id) AS sent_messages,
       (SELECT COUNT(*) FROM private_messages pm WHERE pm.receiver_id = u.id) AS received_messages
FROM users u;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 3. Test hiệu suất INSERT
SELECT 'Test hiệu suất INSERT' AS test_name;

-- Test INSERT đơn giản
SET @start_time = NOW(6);
INSERT INTO users (username, password, full_name, email, gender, status) 
VALUES ('test_user', 'password123', 'Test User', 'test@example.com', 'Nam', 'active');
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test INSERT với nhiều dòng
SET @start_time = NOW(6);
INSERT INTO login_history (user_id, ip_address) 
SELECT id, '192.168.1.100' FROM users WHERE username = 'test_user';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 4. Test hiệu suất UPDATE
SELECT 'Test hiệu suất UPDATE' AS test_name;

-- Test UPDATE đơn giản
SET @start_time = NOW(6);
UPDATE users SET full_name = 'Updated Test User' WHERE username = 'test_user';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test UPDATE với JOIN
SET @start_time = NOW(6);
UPDATE users u
JOIN friendships f ON u.id = f.user1_id
SET u.status = 'active'
WHERE f.status = 'accepted';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 5. Test hiệu suất DELETE
SELECT 'Test hiệu suất DELETE' AS test_name;

-- Test DELETE đơn giản
SET @start_time = NOW(6);
DELETE FROM login_history WHERE user_id = (SELECT id FROM users WHERE username = 'test_user');
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test DELETE với nhiều điều kiện
SET @start_time = NOW(6);
DELETE FROM users WHERE username = 'test_user' AND status = 'active';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 6. Test hiệu suất với dữ liệu lớn
SELECT 'Test hiệu suất với dữ liệu lớn' AS test_name;

-- Tạo dữ liệu test lớn
SET @start_time = NOW(6);
INSERT INTO private_messages (sender_id, receiver_id, message, sent_at)
SELECT 
    (SELECT id FROM users ORDER BY RAND() LIMIT 1),
    (SELECT id FROM users ORDER BY RAND() LIMIT 1),
    CONCAT('Test message ', RAND()),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
FROM information_schema.tables t1, information_schema.tables t2
LIMIT 1000;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- Test truy vấn với dữ liệu lớn
SET @start_time = NOW(6);
SELECT u.username, COUNT(pm.id) AS message_count
FROM users u
LEFT JOIN private_messages pm ON u.id = pm.sender_id
GROUP BY u.id, u.username
ORDER BY message_count DESC
LIMIT 10;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 7. Kiểm tra hiệu suất index
SELECT 'Kiểm tra hiệu suất index' AS test_name;

-- Kiểm tra sử dụng index
EXPLAIN SELECT * FROM users WHERE username = 'user1';
EXPLAIN SELECT * FROM users WHERE email = 'user1@email.com';
EXPLAIN SELECT * FROM private_messages WHERE sender_id = 1;
EXPLAIN SELECT * FROM private_messages WHERE sent_at > DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 8. Test hiệu suất với các truy vấn thống kê
SELECT 'Test hiệu suất truy vấn thống kê' AS test_name;

-- Test truy vấn thống kê phức tạp
SET @start_time = NOW(6);
SELECT 
    DATE(sent_at) AS message_date,
    COUNT(*) AS message_count,
    COUNT(DISTINCT sender_id) AS unique_senders,
    COUNT(DISTINCT receiver_id) AS unique_receivers
FROM private_messages
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(sent_at)
ORDER BY message_date DESC;
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) AS execution_time_microseconds;

-- 9. Dọn dẹp dữ liệu test
SELECT 'Dọn dẹp dữ liệu test' AS test_name;

-- Xóa dữ liệu test
DELETE FROM private_messages WHERE message LIKE 'Test message%';
DELETE FROM users WHERE username = 'test_user';

-- 10. Tóm tắt kết quả
SELECT 'Tóm tắt kết quả test hiệu suất' AS test_name;

-- Hiển thị thông tin về database
SELECT 
    'Database size' AS metric,
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS value_mb
FROM information_schema.tables
WHERE table_schema = 'chat_system';

-- Hiển thị số lượng bản ghi trong các bảng
SELECT 'users' AS table_name, COUNT(*) AS record_count FROM users
UNION ALL
SELECT 'friendships', COUNT(*) FROM friendships
UNION ALL
SELECT 'private_messages', COUNT(*) FROM private_messages
UNION ALL
SELECT 'group_messages', COUNT(*) FROM group_messages
UNION ALL
SELECT 'chat_groups', COUNT(*) FROM chat_groups;
