-- Script backup cơ sở dữ liệu
-- Sử dụng để sao lưu dữ liệu trước khi cập nhật hoặc thay đổi

-- Tạo bản sao lưu toàn bộ database
-- mysqldump -u root -p chat_system > backup_chat_system_$(date +%Y%m%d_%H%M%S).sql

-- Tạo bản sao lưu chỉ cấu trúc
-- mysqldump -u root -p --no-data chat_system > backup_structure_$(date +%Y%m%d_%H%M%S).sql

-- Tạo bản sao lưu chỉ dữ liệu
-- mysqldump -u root -p --no-create-info chat_system > backup_data_$(date +%Y%m%d_%H%M%S).sql

-- Khôi phục từ bản sao lưu
-- mysql -u root -p chat_system < backup_chat_system_YYYYMMDD_HHMMSS.sql

-- Lưu ý: Thay thế YYYYMMDD_HHMMSS bằng ngày giờ thực tế khi tạo backup
