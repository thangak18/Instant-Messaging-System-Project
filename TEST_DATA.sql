-- ============================================
-- TEST DATA CHO HỆ THỐNG ADMIN
-- Chạy script này trong Supabase SQL Editor
-- ============================================

-- 1. THÊM USERS (15 users mới)
-- Password: 123456 (đã hash đơn giản - bạn có thể thay đổi)
INSERT INTO users (username, password, full_name, email, address, dob, gender, status, created_at, last_login)
VALUES 
-- Users đăng ký tháng 11/2025
('nguyenvana', '123456', 'Nguyễn Văn A', 'vana@gmail.com', 'Hà Nội', '1995-03-15', 'Nam', 'active', '2025-11-01 08:00:00', '2025-11-26 10:30:00'),
('tranthib', '123456', 'Trần Thị B', 'thib@gmail.com', 'TP.HCM', '1998-07-22', 'Nữ', 'active', '2025-11-02 09:15:00', '2025-11-25 14:20:00'),
('levanc', '123456', 'Lê Văn C', 'vanc@gmail.com', 'Đà Nẵng', '1992-11-10', 'Nam', 'active', '2025-11-03 10:30:00', '2025-11-26 08:45:00'),
('phamthid', '123456', 'Phạm Thị D', 'thid@gmail.com', 'Hải Phòng', '2000-01-05', 'Nữ', 'locked', '2025-11-04 11:45:00', '2025-11-20 16:00:00'),
('hoangvane', '123456', 'Hoàng Văn E', 'vane@gmail.com', 'Cần Thơ', '1997-09-18', 'Nam', 'active', '2025-11-05 13:00:00', '2025-11-26 09:10:00'),

-- Users đăng ký tháng 10/2025
('vuthif', '123456', 'Vũ Thị F', 'thif@gmail.com', 'Huế', '1999-04-25', 'Nữ', 'active', '2025-10-10 08:30:00', '2025-11-24 11:30:00'),
('dangvang', '123456', 'Đặng Văn G', 'vang@gmail.com', 'Nha Trang', '1994-12-08', 'Nam', 'active', '2025-10-15 14:00:00', '2025-11-23 15:45:00'),
('buithih', '123456', 'Bùi Thị H', 'thih@gmail.com', 'Vũng Tàu', '1996-06-30', 'Nữ', 'banned', '2025-10-20 16:30:00', '2025-11-10 09:00:00'),

-- Users đăng ký tháng 9/2025
('ngothii', '123456', 'Ngô Thị I', 'thii@gmail.com', 'Biên Hòa', '2001-02-14', 'Nữ', 'active', '2025-09-05 09:00:00', '2025-11-25 17:20:00'),
('dovanк', '123456', 'Đỗ Văn K', 'vank@gmail.com', 'Thủ Đức', '1993-08-20', 'Nam', 'active', '2025-09-12 10:15:00', '2025-11-26 07:30:00'),

-- Users đăng ký tháng trước đó
('lythil', '123456', 'Lý Thị L', 'thil@gmail.com', 'Bình Dương', '1998-05-12', 'Nữ', 'active', '2025-08-01 08:00:00', '2025-11-22 12:00:00'),
('truongvanm', '123456', 'Trương Văn M', 'vanm@gmail.com', 'Long An', '1995-10-28', 'Nam', 'active', '2025-07-15 11:30:00', '2025-11-21 14:30:00'),
('hothın', '123456', 'Hồ Thị N', 'thin@gmail.com', 'Đồng Nai', '2000-03-07', 'Nữ', 'active', '2025-06-20 13:45:00', '2025-11-20 16:15:00'),
('maivano', '123456', 'Mai Văn O', 'vano@gmail.com', 'Bắc Ninh', '1991-07-19', 'Nam', 'locked', '2025-05-10 15:00:00', '2025-11-15 10:00:00'),
('caothip', '123456', 'Cao Thị P', 'thip@gmail.com', 'Quảng Ninh', '1999-09-03', 'Nữ', 'active', '2025-04-05 09:30:00', '2025-11-26 08:00:00')
ON CONFLICT (username) DO NOTHING;

-- 2. THÊM LOGIN HISTORY (50 records)
-- Lấy user_id từ users đã tạo
INSERT INTO login_history (user_id, login_time, ip_address, device_info)
SELECT u.user_id, 
       timestamp '2025-11-26 08:00:00' - (random() * interval '30 days'),
       '192.168.1.' || floor(random() * 255 + 1)::text,
       CASE floor(random() * 4)::int
           WHEN 0 THEN 'Chrome/Windows 10'
           WHEN 1 THEN 'Safari/macOS'
           WHEN 2 THEN 'Firefox/Ubuntu'
           ELSE 'Mobile/Android'
       END
FROM users u, generate_series(1, 5) s
WHERE u.status = 'active'
ON CONFLICT DO NOTHING;

-- 3. THÊM FRIENDSHIPS (Quan hệ bạn bè)
-- Tạo quan hệ bạn bè giữa các users
INSERT INTO friends (user_id, friend_id, status, created_at, updated_at)
SELECT 
    u1.user_id,
    u2.user_id,
    CASE WHEN random() < 0.8 THEN 'accepted' ELSE 'pending' END,
    NOW() - (random() * interval '60 days'),
    NOW() - (random() * interval '30 days')
FROM users u1
CROSS JOIN users u2
WHERE u1.user_id < u2.user_id 
  AND random() < 0.3  -- 30% chance of friendship
  AND u1.status = 'active' 
  AND u2.status = 'active'
ON CONFLICT DO NOTHING;

-- 4. THÊM GROUPS (5 nhóm mới)
INSERT INTO groups (group_name, admin_id, created_at, encrypted)
SELECT 
    group_name,
    (SELECT user_id FROM users WHERE status = 'active' ORDER BY random() LIMIT 1),
    NOW() - (random() * interval '90 days'),
    random() < 0.2
FROM (VALUES 
    ('Nhóm Học Tập Java'),
    ('CLB Lập Trình Web'),
    ('Nhóm Chat Vui Vẻ'),
    ('Cộng Đồng IT Việt Nam'),
    ('Nhóm Hỗ Trợ Kỹ Thuật')
) AS t(group_name)
ON CONFLICT DO NOTHING;

-- 5. THÊM GROUP MEMBERS
INSERT INTO group_members (group_id, user_id, joined_at)
SELECT 
    g.group_id,
    u.user_id,
    g.created_at + (random() * interval '30 days')
FROM groups g
CROSS JOIN users u
WHERE u.status = 'active' 
  AND random() < 0.4  -- 40% users join each group
  AND u.user_id != g.admin_id
ON CONFLICT DO NOTHING;

-- Thêm admin vào group của họ
INSERT INTO group_members (group_id, user_id, joined_at)
SELECT g.group_id, g.admin_id, g.created_at
FROM groups g
ON CONFLICT DO NOTHING;

-- 6. THÊM MESSAGES (Tin nhắn riêng)
INSERT INTO messages (sender_id, receiver_id, content, created_at, is_read)
SELECT 
    f.user_id,
    f.friend_id,
    CASE floor(random() * 5)::int
        WHEN 0 THEN 'Chào bạn! Khỏe không?'
        WHEN 1 THEN 'Hôm nay thời tiết đẹp quá!'
        WHEN 2 THEN 'Bạn đang làm gì vậy?'
        WHEN 3 THEN 'Tối nay rảnh không? Đi cafe nhé!'
        ELSE 'OK, hẹn gặp lại sau nhé!'
    END,
    NOW() - (random() * interval '14 days'),
    random() < 0.7
FROM friends f
CROSS JOIN generate_series(1, 3)
WHERE f.status = 'accepted'
ON CONFLICT DO NOTHING;

-- 7. THÊM GROUP MESSAGES (Tin nhắn nhóm)
INSERT INTO group_messages (group_id, sender_id, message_text, sent_time)
SELECT 
    gm.group_id,
    gm.user_id,
    CASE floor(random() * 5)::int
        WHEN 0 THEN 'Xin chào mọi người!'
        WHEN 1 THEN 'Ai biết cách fix bug này không?'
        WHEN 2 THEN 'Cảm ơn bạn đã giúp đỡ!'
        WHEN 3 THEN 'Hôm nay học được gì mới không?'
        ELSE 'Tuyệt vời! Cảm ơn team!'
    END,
    NOW() - (random() * interval '7 days')
FROM group_members gm
CROSS JOIN generate_series(1, 2)
ON CONFLICT DO NOTHING;

-- 8. THÊM SPAM REPORTS (10 báo cáo spam)
INSERT INTO spam_reports (reporter_id, reported_user_id, reason, report_time, status)
SELECT 
    u1.user_id,
    u2.user_id,
    CASE floor(random() * 4)::int
        WHEN 0 THEN 'Gửi tin nhắn spam quảng cáo'
        WHEN 1 THEN 'Ngôn ngữ không phù hợp'
        WHEN 2 THEN 'Giả mạo danh tính'
        ELSE 'Quấy rối người dùng khác'
    END,
    NOW() - (random() * interval '30 days'),
    CASE floor(random() * 3)::int
        WHEN 0 THEN 'pending'
        WHEN 1 THEN 'resolved'
        ELSE 'rejected'
    END
FROM users u1
CROSS JOIN users u2
WHERE u1.user_id != u2.user_id
  AND random() < 0.05  -- 5% chance
LIMIT 10
ON CONFLICT DO NOTHING;

-- ============================================
-- KIỂM TRA DỮ LIỆU ĐÃ THÊM
-- ============================================

SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'login_history', COUNT(*) FROM login_history
UNION ALL
SELECT 'friends', COUNT(*) FROM friends
UNION ALL
SELECT 'groups', COUNT(*) FROM groups
UNION ALL
SELECT 'group_members', COUNT(*) FROM group_members
UNION ALL
SELECT 'messages', COUNT(*) FROM messages
UNION ALL
SELECT 'group_messages', COUNT(*) FROM group_messages
UNION ALL
SELECT 'spam_reports', COUNT(*) FROM spam_reports;

