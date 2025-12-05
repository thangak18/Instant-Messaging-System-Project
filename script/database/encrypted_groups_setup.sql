-- Script thêm hỗ trợ mã hóa đầu cuối (E2E Encryption) cho nhóm chat
-- Chạy script này trong Supabase SQL Editor

-- Thêm cột is_encrypted và encryption_key vào bảng groups
-- QUAN TRỌNG: encryption_key chỉ dùng để chia sẻ khóa với thành viên mới
-- Trong môi trường production thực sự, khóa nên được trao đổi qua kênh an toàn hơn

-- Kiểm tra và thêm cột is_encrypted nếu chưa có
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='groups' AND column_name='is_encrypted') THEN
        ALTER TABLE groups ADD COLUMN is_encrypted BOOLEAN DEFAULT FALSE;
        RAISE NOTICE 'Đã thêm cột is_encrypted vào bảng groups';
    ELSE
        RAISE NOTICE 'Cột is_encrypted đã tồn tại';
    END IF;
END $$;

-- Thêm cột encryption_key để lưu khóa mã hóa (Base64)
-- Lưu ý: Trong E2E thực sự, khóa này chỉ là khóa công khai hoặc khóa đã được wrap
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='groups' AND column_name='encryption_key') THEN
        ALTER TABLE groups ADD COLUMN encryption_key TEXT;
        RAISE NOTICE 'Đã thêm cột encryption_key vào bảng groups';
    ELSE
        RAISE NOTICE 'Cột encryption_key đã tồn tại';
    END IF;
END $$;

-- Tạo index cho việc lọc nhóm mã hóa
CREATE INDEX IF NOT EXISTS idx_groups_is_encrypted ON groups(is_encrypted);

-- Tạo view cho nhóm mã hóa
CREATE OR REPLACE VIEW encrypted_groups AS
SELECT 
    g.group_id,
    g.group_name,
    g.admin_id,
    g.is_encrypted,
    g.created_at,
    u.username as admin_username,
    COUNT(DISTINCT gm.user_id) as member_count
FROM groups g
JOIN users u ON g.admin_id = u.user_id
LEFT JOIN group_members gm ON g.group_id = gm.group_id
WHERE g.is_encrypted = TRUE
GROUP BY g.group_id, g.group_name, g.admin_id, g.is_encrypted, g.created_at, u.username;

-- Comment giải thích
COMMENT ON COLUMN groups.is_encrypted IS 'TRUE nếu nhóm sử dụng mã hóa đầu cuối (E2E)';
COMMENT ON COLUMN groups.encryption_key IS 'Khóa mã hóa nhóm dạng Base64 - chỉ để chia sẻ với thành viên mới';

-- Hiển thị kết quả
SELECT 
    column_name, 
    data_type, 
    column_default,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'groups'
ORDER BY ordinal_position;
