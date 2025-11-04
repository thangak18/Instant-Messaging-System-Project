-- TEST FRIEND REQUESTS
-- Script để test hệ thống friend request

-- 1. Kiểm tra users hiện tại
SELECT user_id, username, full_name FROM users WHERE status = 'active';

-- 2. Xem tất cả friend requests
SELECT 
    f.friendship_id,
    u1.username as sender,
    u2.username as receiver,
    f.status,
    f.created_at
FROM friends f
JOIN users u1 ON f.user_id = u1.user_id
JOIN users u2 ON f.friend_id = u2.user_id
ORDER BY f.created_at DESC;

-- 3. Đếm lời mời pending của từng user
SELECT 
    u.username,
    COUNT(*) as pending_requests
FROM friends f
JOIN users u ON f.friend_id = u.user_id
WHERE f.status = 'pending'
GROUP BY u.username;

-- 4. Test: Thêm friend request (nếu cần)
-- INSERT INTO friends (user_id, friend_id, status, created_at)
-- VALUES (
--     (SELECT user_id FROM users WHERE username = 'Hung'),
--     (SELECT user_id FROM users WHERE username = 'admin'),
--     'pending',
--     CURRENT_TIMESTAMP
-- );

-- 5. Xóa tất cả pending requests (reset)
-- DELETE FROM friends WHERE status = 'pending';
