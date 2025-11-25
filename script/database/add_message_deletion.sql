-- =====================================================
-- THÊM CHỨC NĂNG XÓA TIN NHẮN RIÊNG LẺ
-- =====================================================

-- Tạo bảng deleted_messages để lưu tin nhắn đã xóa bởi từng user
CREATE TABLE IF NOT EXISTS deleted_messages (
    message_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index để tăng tốc độ query
CREATE INDEX IF NOT EXISTS idx_deleted_messages_user 
ON deleted_messages(user_id);

CREATE INDEX IF NOT EXISTS idx_deleted_messages_message 
ON deleted_messages(message_id);

-- Comment
COMMENT ON TABLE deleted_messages IS 'Lưu trữ tin nhắn đã xóa bởi từng người dùng (soft delete)';
COMMENT ON COLUMN deleted_messages.message_id IS 'ID tin nhắn bị xóa';
COMMENT ON COLUMN deleted_messages.user_id IS 'ID người dùng xóa tin nhắn này';
COMMENT ON COLUMN deleted_messages.deleted_at IS 'Thời điểm xóa';

-- Kiểm tra kết quả
SELECT COUNT(*) as total_deleted_messages FROM deleted_messages;

PRINT '✅ Đã tạo bảng deleted_messages để hỗ trợ xóa tin nhắn riêng lẻ';
PRINT '📌 Chức năng:';
PRINT '   - Xóa chỉ mình tôi: Thêm vào deleted_messages (soft delete)';
PRINT '   - Thu hồi tin nhắn: Xóa khỏi bảng messages (hard delete)';
