-- Friend Requests Management
-- Lưu các lời mời kết bạn

-- Thêm cột last_updated vào bảng friends nếu chưa có
ALTER TABLE friends ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Index để tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_friends_user_id ON friends(user_id);
CREATE INDEX IF NOT EXISTS idx_friends_friend_id ON friends(friend_id);
CREATE INDEX IF NOT EXISTS idx_friends_status ON friends(status);

-- View để xem friend requests dễ hơn
CREATE OR REPLACE VIEW friend_requests_view AS
SELECT 
    f.friendship_id,
    f.user_id as sender_id,
    u1.username as sender_username,
    u1.full_name as sender_name,
    f.friend_id as receiver_id,
    u2.username as receiver_username,
    u2.full_name as receiver_name,
    f.status,
    f.created_at
FROM friends f
JOIN users u1 ON f.user_id = u1.user_id
JOIN users u2 ON f.friend_id = u2.user_id
WHERE f.status = 'pending';
