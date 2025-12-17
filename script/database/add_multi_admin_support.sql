-- Script thêm hỗ trợ nhiều admin cho nhóm
-- Chạy script này trong Supabase SQL Editor

-- Thêm cột is_admin vào bảng group_members
ALTER TABLE group_members 
ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Cập nhật is_admin = true cho admin chính (admin_id trong bảng groups)
UPDATE group_members gm
SET is_admin = TRUE
WHERE EXISTS (
    SELECT 1 FROM groups g 
    WHERE g.group_id = gm.group_id 
    AND g.admin_id = gm.user_id
);

-- Tạo index để tối ưu truy vấn
CREATE INDEX IF NOT EXISTS idx_group_members_is_admin 
ON group_members(group_id, is_admin) 
WHERE is_admin = TRUE;

-- Kiểm tra kết quả
SELECT 
    gm.group_id, 
    g.group_name,
    u.username, 
    gm.is_admin,
    CASE WHEN g.admin_id = gm.user_id THEN 'Main Admin' ELSE 'Co-Admin' END as admin_type
FROM group_members gm
JOIN groups g ON gm.group_id = g.group_id
JOIN users u ON gm.user_id = u.user_id
WHERE gm.is_admin = TRUE OR g.admin_id = gm.user_id
ORDER BY gm.group_id, gm.is_admin DESC;
