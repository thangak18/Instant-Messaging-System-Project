-- ========================================
-- SEED DATA FOR ADMIN TESTING
-- Generate ~100 records for each feature
-- SAFE MODE: Only adds new data, does NOT delete existing data
-- ========================================

-- ========================================
-- 1. USERS - 100 users
-- Note: Uses ON CONFLICT to skip if username already exists
-- ========================================
INSERT INTO users (username, password, full_name, email, address, dob, gender, status, created_at) VALUES
('user001', 'password123', 'Nguyễn Văn A', 'nguyenvana001@email.com', 'Hà Nội', '1995-01-15', 'Nam', 'active', '2024-01-15 08:30:00'),
('user002', 'password123', 'Trần Thị B', 'tranthib002@email.com', 'TP.HCM', '1996-02-20', 'Nữ', 'active', '2024-01-16 09:00:00'),
('user003', 'password123', 'Lê Văn C', 'levanc003@email.com', 'Đà Nẵng', '1997-03-25', 'Nam', 'active', '2024-01-17 10:15:00'),
('user004', 'password123', 'Phạm Thị D', 'phamthid004@email.com', 'Hải Phòng', '1998-04-30', 'Nữ', 'locked', '2024-01-18 11:20:00'),
('user005', 'password123', 'Hoàng Văn E', 'hoangvane005@email.com', 'Cần Thơ', '1999-05-10', 'Nam', 'active', '2024-01-19 12:30:00'),
('user006', 'password123', 'Vũ Thị F', 'vuthif006@email.com', 'Hà Nội', '2000-06-15', 'Nữ', 'active', '2024-02-01 08:00:00'),
('user007', 'password123', 'Đỗ Văn G', 'dovang007@email.com', 'TP.HCM', '1995-07-20', 'Nam', 'active', '2024-02-02 09:30:00'),
('user008', 'password123', 'Ngô Thị H', 'ngothih008@email.com', 'Đà Nẵng', '1996-08-25', 'Nữ', 'active', '2024-02-03 10:00:00'),
('user009', 'password123', 'Bùi Văn I', 'buivani009@email.com', 'Huế', '1997-09-30', 'Nam', 'active', '2024-02-04 11:15:00'),
('user010', 'password123', 'Đinh Thị K', 'dinhthik010@email.com', 'Nha Trang', '1998-10-05', 'Nữ', 'active', '2024-02-05 12:45:00')
ON CONFLICT (username) DO NOTHING;

-- Generate remaining 90 users
DO $$
DECLARE
    i INT;
    random_name TEXT;
    random_email TEXT;
    random_status TEXT;
    random_gender TEXT;
    random_date DATE;
BEGIN
    FOR i IN 11..100 LOOP
        random_name := 'User ' || LPAD(i::TEXT, 3, '0');
        random_email := 'user' || LPAD(i::TEXT, 3, '0') || '@test.com';
        random_status := CASE WHEN random() < 0.9 THEN 'active' WHEN random() < 0.95 THEN 'locked' ELSE 'deleted' END;
        random_gender := CASE WHEN random() < 0.5 THEN 'Nam' ELSE 'Nữ' END;
        random_date := CURRENT_DATE - (random() * 365 * 2)::INT;
        
        INSERT INTO users (username, password, full_name, email, address, dob, gender, status, created_at)
        VALUES (
            'user' || LPAD(i::TEXT, 3, '0'),
            'password123',
            random_name,
            random_email,
            CASE (i % 5)
                WHEN 0 THEN 'Hà Nội'
                WHEN 1 THEN 'TP.HCM'
                WHEN 2 THEN 'Đà Nẵng'
                WHEN 3 THEN 'Hải Phòng'
                ELSE 'Cần Thơ'
            END,
            '1990-01-01'::DATE + (random() * 365 * 25)::INT,
            random_gender,
            random_status,
            random_date
        );
    END LOOP;
END $$;

-- ========================================
-- 2. LOGIN HISTORY - Multiple logins per user
-- ========================================
DO $$
DECLARE
    user_rec RECORD;
    login_count INT;
    login_date TIMESTAMP;
BEGIN
    FOR user_rec IN SELECT user_id, created_at FROM users WHERE status IN ('active', 'locked') LOOP
        login_count := (random() * 20)::INT + 5; -- 5-25 logins per user
        
        FOR i IN 1..login_count LOOP
            login_date := user_rec.created_at + (random() * EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - user_rec.created_at)))::INT * INTERVAL '1 second';
            
            INSERT INTO login_history (user_id, login_time)
            VALUES (user_rec.user_id, login_date);
        END LOOP;
    END LOOP;
END $$;

-- ========================================
-- 3. FRIENDS - Create friend relationships
-- ========================================
DO $$
DECLARE
    user1_id INT;
    user2_id INT;
    friend_count INT := 0;
BEGIN
    -- Create random friendships
    WHILE friend_count < 200 LOOP
        user1_id := (SELECT user_id FROM users WHERE status = 'active' ORDER BY random() LIMIT 1);
        user2_id := (SELECT user_id FROM users WHERE status = 'active' AND user_id != user1_id ORDER BY random() LIMIT 1);
        
        -- Check if friendship already exists
        IF NOT EXISTS (
            SELECT 1 FROM friends 
            WHERE (user_id = user1_id AND friend_id = user2_id) 
               OR (user_id = user2_id AND friend_id = user1_id)
        ) THEN
            INSERT INTO friends (user_id, friend_id, status, created_at)
            VALUES (user1_id, user2_id, 'accepted', CURRENT_TIMESTAMP - (random() * 180)::INT * INTERVAL '1 day');
            
            friend_count := friend_count + 1;
        END IF;
    END LOOP;
END $$;

-- ========================================
-- 4. GROUPS - Create 50 groups
-- ========================================
DO $$
DECLARE
    i INT;
    creator_id INT;
    group_name TEXT;
BEGIN
    FOR i IN 1..50 LOOP
        creator_id := (SELECT user_id FROM users WHERE status = 'active' ORDER BY random() LIMIT 1);
        group_name := 'Nhóm Chat ' || LPAD(i::TEXT, 2, '0');
        
        INSERT INTO groups (group_name, admin_id, created_at)
        VALUES (
            group_name,
            creator_id,
            CURRENT_TIMESTAMP - (random() * 180)::INT * INTERVAL '1 day'
        );
    END LOOP;
END $$;

-- ========================================
-- 5. GROUP MEMBERS - Add members to groups
-- ========================================
DO $$
DECLARE
    group_rec RECORD;
    member_count INT;
    random_user_id INT;
BEGIN
    FOR group_rec IN SELECT group_id, admin_id FROM groups LOOP
        -- Add creator as admin
        INSERT INTO group_members (group_id, user_id, joined_at)
        VALUES (group_rec.group_id, group_rec.admin_id, CURRENT_TIMESTAMP - (random() * 180)::INT * INTERVAL '1 day')
        ON CONFLICT DO NOTHING;
        
        -- Add 5-15 random members
        member_count := (random() * 10)::INT + 5;
        
        FOR i IN 1..member_count LOOP
            random_user_id := (SELECT user_id FROM users WHERE status = 'active' AND user_id != group_rec.admin_id ORDER BY random() LIMIT 1);
            
            INSERT INTO group_members (group_id, user_id, joined_at)
            VALUES (
                group_rec.group_id,
                random_user_id,
                CURRENT_TIMESTAMP - (random() * 160)::INT * INTERVAL '1 day'
            )
            ON CONFLICT DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- ========================================
-- 6. PRIVATE MESSAGES - Create 500 messages
-- ========================================
DO $$
DECLARE
    i INT;
    sender_id INT;
    receiver_id INT;
    msg_content TEXT;
    msg_array TEXT[] := ARRAY[
        'Chào bạn!',
        'Hôm nay bạn thế nào?',
        'Tối nay rảnh không?',
        'Đi ăn tối nhé',
        'OK, hẹn gặp lại',
        'Cảm ơn bạn!',
        'Chúc bạn một ngày tốt lành',
        'Gặp lại sau nhé',
        'Bye bye!',
        'Tuyệt vời!'
    ];
BEGIN
    FOR i IN 1..500 LOOP
        sender_id := (SELECT user_id FROM users WHERE status = 'active' ORDER BY random() LIMIT 1);
        receiver_id := (SELECT user_id FROM users WHERE status = 'active' AND user_id != sender_id ORDER BY random() LIMIT 1);
        msg_content := msg_array[(random() * 9)::INT + 1];
        
        INSERT INTO messages (sender_id, receiver_id, content, created_at, is_read)
        VALUES (
            sender_id,
            receiver_id,
            msg_content,
            CURRENT_TIMESTAMP - (random() * 90)::INT * INTERVAL '1 day' - (random() * 24)::INT * INTERVAL '1 hour',
            random() < 0.7
        );
    END LOOP;
END $$;

-- ========================================
-- 7. GROUP MESSAGES - Create 800 group messages
-- ========================================
DO $$
DECLARE
    i INT;
    random_group_id INT;
    random_sender_id INT;
    msg_content TEXT;
    msg_array TEXT[] := ARRAY[
        'Hello mọi người!',
        'Hôm nay ai rảnh không?',
        'Meeting lúc 3h chiều nhé',
        'OK nhé!',
        'Cảm ơn mọi người',
        'Chúc mừng sinh nhật!',
        'Tuyệt vời!',
        'Hẹn gặp lại',
        'Có ai online không?',
        'Đồng ý rồi!'
    ];
BEGIN
    FOR i IN 1..800 LOOP
        random_group_id := (SELECT group_id FROM groups ORDER BY random() LIMIT 1);
        random_sender_id := (SELECT user_id FROM group_members WHERE group_id = random_group_id ORDER BY random() LIMIT 1);
        msg_content := msg_array[(random() * 9)::INT + 1];
        
        INSERT INTO group_messages (group_id, sender_id, message_text)
        VALUES (
            random_group_id,
            random_sender_id,
            msg_content
        );
    END LOOP;
END $$;

-- ========================================
-- 8. SPAM REPORTS - Create 100 spam reports
-- ========================================
DO $$
DECLARE
    i INT;
    reporter_id INT;
    reported_id INT;
    report_content TEXT;
    report_array TEXT[] := ARRAY[
        'Gửi tin nhắn spam quảng cáo',
        'Lừa đảo',
        'Nội dung không phù hợp',
        'Quấy rối',
        'Giả mạo',
        'Spam tin nhắn liên tục',
        'Nội dung độc hại',
        'Vi phạm quy định cộng đồng',
        'Gửi link lạ',
        'Mạo danh người khác'
    ];
    report_status TEXT;
BEGIN
    FOR i IN 1..100 LOOP
        reporter_id := (SELECT user_id FROM users WHERE status = 'active' ORDER BY random() LIMIT 1);
        reported_id := (SELECT user_id FROM users WHERE status = 'active' AND user_id != reporter_id ORDER BY random() LIMIT 1);
        report_content := report_array[(random() * 9)::INT + 1];
        report_status := CASE WHEN random() < 0.7 THEN 'pending' ELSE 'processed' END;
        
        INSERT INTO spam_reports (reporter_id, reported_user_id, reason, status, report_time)
        VALUES (
            reporter_id,
            reported_id,
            report_content,
            report_status,
            CURRENT_TIMESTAMP - (random() * 60)::INT * INTERVAL '1 day'
        );
    END LOOP;
END $$;

-- ========================================
-- SUMMARY
-- ========================================
SELECT 
    'USERS' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'LOGIN_HISTORY', COUNT(*) FROM login_history
UNION ALL
SELECT 'FRIENDS', COUNT(*) FROM friends
UNION ALL
SELECT 'GROUPS', COUNT(*) FROM groups
UNION ALL
SELECT 'GROUP_MEMBERS', COUNT(*) FROM group_members
UNION ALL
SELECT 'PRIVATE_MESSAGES', COUNT(*) FROM messages
UNION ALL
SELECT 'GROUP_MESSAGES', COUNT(*) FROM group_messages
UNION ALL
SELECT 'SPAM_REPORTS', COUNT(*) FROM spam_reports
ORDER BY table_name;
