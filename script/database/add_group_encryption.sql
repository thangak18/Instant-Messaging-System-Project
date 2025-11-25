-- =====================================================
-- THÊM CHỨC NĂNG MÃ HÓA NHÓM (GROUP ENCRYPTION)
-- =====================================================

-- Thêm cột encrypted vào bảng groups
ALTER TABLE groups 
ADD COLUMN IF NOT EXISTS encrypted BOOLEAN DEFAULT FALSE;

-- Cập nhật comment cho bảng
COMMENT ON COLUMN groups.encrypted IS 'Trạng thái mã hóa end-to-end cho nhóm';

-- Kiểm tra kết quả
SELECT group_id, group_name, encrypted 
FROM groups 
LIMIT 5;

-- Test: Bật mã hóa cho một nhóm
-- UPDATE groups SET encrypted = TRUE WHERE group_id = 1;

-- Test: Kiểm tra nhóm nào đã bật mã hóa
-- SELECT group_id, group_name, encrypted 
-- FROM groups 
-- WHERE encrypted = TRUE;

PRINT '✅ Đã thêm cột encrypted vào bảng groups';
