-- Script kiểm tra và test các truy vấn
-- Sử dụng để kiểm tra hiệu suất và tính đúng đắn của database

USE chat_system;

-- 1. Kiểm tra dữ liệu cơ bản
SELECT 'Kiểm tra dữ liệu cơ bản' AS test_name;

-- Đếm số lượng người dùng
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS active_users FROM users WHERE status = 'active';
SELECT COUNT(*) AS locked_users FROM users WHERE status = 'locked';

-- Đếm số lượng bạn bè
SELECT COUNT(*) AS total_friendships FROM friendships;
SELECT COUNT(*) AS accepted_friendships FROM friendships WHERE status = 'accepted';
SELECT COUNT(*) AS pending_friendships FROM friendships WHERE status = 'pending';

-- Đếm số lượng nhóm
SELECT COUNT(*) AS total_groups FROM chat_groups;
SELECT COUNT(*) AS total_group_members FROM group_members;

-- Đếm số lượng tin nhắn
SELECT COUNT(*) AS total_private_messages FROM private_messages;
SELECT COUNT(*) AS total_group_messages FROM group_messages;

-- 2. Test các truy vấn phức tạp
SELECT 'Test truy vấn phức tạp' AS test_name;

-- Tìm người dùng có nhiều bạn bè nhất
SELECT u.username, u.full_name, COUNT(f.id) AS friend_count
FROM users u
LEFT JOIN friendships f ON (u.id = f.user1_id OR u.id = f.user2_id) AND f.status = 'accepted'
GROUP BY u.id, u.username, u.full_name
ORDER BY friend_count DESC
LIMIT 5;

-- Tìm nhóm có nhiều thành viên nhất
SELECT cg.group_name, COUNT(gm.id) AS member_count
FROM chat_groups cg
LEFT JOIN group_members gm ON cg.id = gm.group_id
GROUP BY cg.id, cg.group_name
ORDER BY member_count DESC
LIMIT 5;

-- Tìm người dùng hoạt động nhiều nhất (dựa trên tin nhắn)
SELECT u.username, u.full_name, 
       COUNT(pm.id) AS private_message_count,
       COUNT(gm.id) AS group_message_count,
       (COUNT(pm.id) + COUNT(gm.id)) AS total_messages
FROM users u
LEFT JOIN private_messages pm ON u.id = pm.sender_id
LEFT JOIN group_messages gm ON u.id = gm.sender_id
GROUP BY u.id, u.username, u.full_name
ORDER BY total_messages DESC
LIMIT 5;

-- 3. Test các truy vấn thống kê
SELECT 'Test truy vấn thống kê' AS test_name;

-- Thống kê đăng ký theo tháng
SELECT 
    YEAR(created_at) AS year,
    MONTH(created_at) AS month,
    COUNT(*) AS new_users
FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
GROUP BY YEAR(created_at), MONTH(created_at)
ORDER BY year DESC, month DESC;

-- Thống kê đăng nhập theo ngày
SELECT 
    DATE(login_time) AS login_date,
    COUNT(*) AS login_count
FROM login_history
WHERE login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(login_time)
ORDER BY login_date DESC;

-- Thống kê tin nhắn theo ngày
SELECT 
    DATE(sent_at) AS message_date,
    COUNT(*) AS message_count
FROM private_messages
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(sent_at)
ORDER BY message_date DESC;

-- 4. Test các truy vấn tìm kiếm
SELECT 'Test truy vấn tìm kiếm' AS test_name;

-- Tìm kiếm người dùng theo tên
SELECT id, username, full_name, email
FROM users
WHERE full_name LIKE '%Nguyễn%' OR username LIKE '%user%'
ORDER BY full_name;

-- Tìm kiếm tin nhắn chứa từ khóa
SELECT pm.id, u1.username AS sender, u2.username AS receiver, pm.message, pm.sent_at
FROM private_messages pm
JOIN users u1 ON pm.sender_id = u1.id
JOIN users u2 ON pm.receiver_id = u2.id
WHERE pm.message LIKE '%chào%'
ORDER BY pm.sent_at DESC;

-- 5. Test các truy vấn báo cáo
SELECT 'Test truy vấn báo cáo' AS test_name;

-- Báo cáo spam
SELECT sr.id, u1.username AS reporter, u2.username AS reported_user, 
       sr.reason, sr.status, sr.created_at
FROM spam_reports sr
JOIN users u1 ON sr.reporter_id = u1.id
JOIN users u2 ON sr.reported_user_id = u2.id
ORDER BY sr.created_at DESC;

-- Báo cáo hoạt động người dùng
SELECT u.username, u.full_name,
       COUNT(DISTINCT DATE(ua.created_at)) AS active_days,
       COUNT(ua.id) AS total_activities
FROM users u
LEFT JOIN user_activities ua ON u.id = ua.user_id
WHERE ua.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY u.id, u.username, u.full_name
ORDER BY active_days DESC, total_activities DESC;

-- 6. Kiểm tra tính toàn vẹn dữ liệu
SELECT 'Kiểm tra tính toàn vẹn dữ liệu' AS test_name;

-- Kiểm tra foreign key constraints
SELECT 'Kiểm tra foreign key constraints' AS check_name;

-- Kiểm tra friendships có user_id hợp lệ
SELECT COUNT(*) AS invalid_friendships
FROM friendships f
LEFT JOIN users u1 ON f.user1_id = u1.id
LEFT JOIN users u2 ON f.user2_id = u2.id
WHERE u1.id IS NULL OR u2.id IS NULL;

-- Kiểm tra private_messages có sender_id và receiver_id hợp lệ
SELECT COUNT(*) AS invalid_private_messages
FROM private_messages pm
LEFT JOIN users u1 ON pm.sender_id = u1.id
LEFT JOIN users u2 ON pm.receiver_id = u2.id
WHERE u1.id IS NULL OR u2.id IS NULL;

-- Kiểm tra group_messages có group_id và sender_id hợp lệ
SELECT COUNT(*) AS invalid_group_messages
FROM group_messages gm
LEFT JOIN chat_groups cg ON gm.group_id = cg.id
LEFT JOIN users u ON gm.sender_id = u.id
WHERE cg.id IS NULL OR u.id IS NULL;
