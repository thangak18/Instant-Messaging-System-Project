-- Script thiết lập bảo mật cho cơ sở dữ liệu
-- Chạy sau khi tạo database để tăng cường bảo mật

USE chat_system;

-- 1. Tạo user riêng cho ứng dụng (thay vì dùng root)
-- Lưu ý: Thay đổi password mạnh hơn trong môi trường thật
CREATE USER IF NOT EXISTS 'chat_app'@'localhost' IDENTIFIED BY 'chat_app_password_2024!';
CREATE USER IF NOT EXISTS 'chat_app'@'%' IDENTIFIED BY 'chat_app_password_2024!';

-- 2. Cấp quyền cho user ứng dụng
GRANT SELECT, INSERT, UPDATE, DELETE ON chat_system.* TO 'chat_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON chat_system.* TO 'chat_app'@'%';

-- 3. Tạo user chỉ đọc cho báo cáo
CREATE USER IF NOT EXISTS 'chat_readonly'@'localhost' IDENTIFIED BY 'readonly_password_2024!';
GRANT SELECT ON chat_system.* TO 'chat_readonly'@'localhost';

-- 4. Tạo user backup
CREATE USER IF NOT EXISTS 'chat_backup'@'localhost' IDENTIFIED BY 'backup_password_2024!';
GRANT SELECT, LOCK TABLES ON chat_system.* TO 'chat_backup'@'localhost';

-- 5. Cập nhật quyền
FLUSH PRIVILEGES;

-- 6. Tạo view cho báo cáo (ẩn thông tin nhạy cảm)
CREATE VIEW user_report_view AS
SELECT 
    id,
    username,
    full_name,
    email,
    address,
    birth_date,
    gender,
    status,
    created_at
FROM users;

-- 7. Tạo view cho thống kê
CREATE VIEW message_stats_view AS
SELECT 
    DATE(sent_at) AS message_date,
    COUNT(*) AS total_messages,
    COUNT(DISTINCT sender_id) AS unique_senders,
    COUNT(DISTINCT receiver_id) AS unique_receivers
FROM private_messages
GROUP BY DATE(sent_at);

-- 8. Tạo view cho hoạt động người dùng
CREATE VIEW user_activity_view AS
SELECT 
    u.username,
    u.full_name,
    COUNT(DISTINCT DATE(ua.created_at)) AS active_days,
    COUNT(ua.id) AS total_activities,
    MAX(ua.created_at) AS last_activity
FROM users u
LEFT JOIN user_activities ua ON u.id = ua.user_id
WHERE ua.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY u.id, u.username, u.full_name;

-- 9. Tạo stored procedure để thay đổi mật khẩu an toàn
DELIMITER //
CREATE PROCEDURE ChangePassword(IN user_id INT, IN new_password VARCHAR(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Kiểm tra user có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = user_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found';
    END IF;
    
    -- Cập nhật mật khẩu
    UPDATE users SET password = new_password WHERE id = user_id;
    
    -- Ghi log hoạt động
    INSERT INTO user_activities (user_id, activity_type, activity_data)
    VALUES (user_id, 'password_change', JSON_OBJECT('changed_at', NOW()));
    
    COMMIT;
END //
DELIMITER ;

-- 10. Tạo stored procedure để khóa/mở khóa tài khoản
DELIMITER //
CREATE PROCEDURE ToggleUserStatus(IN user_id INT, IN new_status ENUM('active', 'locked'))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Kiểm tra user có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = user_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found';
    END IF;
    
    -- Cập nhật trạng thái
    UPDATE users SET status = new_status WHERE id = user_id;
    
    -- Ghi log hoạt động
    INSERT INTO user_activities (user_id, activity_type, activity_data)
    VALUES (user_id, 'status_change', JSON_OBJECT('new_status', new_status, 'changed_at', NOW()));
    
    COMMIT;
END //
DELIMITER ;

-- 11. Tạo trigger để ghi log thay đổi
DELIMITER //
CREATE TRIGGER user_update_trigger
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO user_activities (user_id, activity_type, activity_data)
        VALUES (NEW.id, 'status_change', JSON_OBJECT('old_status', OLD.status, 'new_status', NEW.status, 'changed_at', NOW()));
    END IF;
END //
DELIMITER ;

-- 12. Tạo trigger để ghi log đăng nhập
DELIMITER //
CREATE TRIGGER login_trigger
AFTER INSERT ON login_history
FOR EACH ROW
BEGIN
    INSERT INTO user_activities (user_id, activity_type, activity_data)
    VALUES (NEW.user_id, 'login', JSON_OBJECT('ip_address', NEW.ip_address, 'login_time', NEW.login_time));
END //
DELIMITER ;

-- 13. Tạo function để kiểm tra mật khẩu mạnh
DELIMITER //
CREATE FUNCTION IsStrongPassword(password VARCHAR(255)) RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE result BOOLEAN DEFAULT FALSE;
    
    -- Kiểm tra độ dài tối thiểu 8 ký tự
    IF LENGTH(password) >= 8 THEN
        -- Kiểm tra có ít nhất 1 chữ hoa, 1 chữ thường, 1 số
        IF password REGEXP '[A-Z]' AND password REGEXP '[a-z]' AND password REGEXP '[0-9]' THEN
            SET result = TRUE;
        END IF;
    END IF;
    
    RETURN result;
END //
DELIMITER ;

-- 14. Tạo event để dọn dẹp dữ liệu cũ
DELIMITER //
CREATE EVENT IF NOT EXISTS cleanup_old_data
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    -- Xóa lịch sử đăng nhập cũ hơn 1 năm
    DELETE FROM login_history WHERE login_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);
    
    -- Xóa hoạt động cũ hơn 6 tháng
    DELETE FROM user_activities WHERE created_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
    
    -- Xóa báo cáo spam đã xử lý cũ hơn 1 năm
    DELETE FROM spam_reports WHERE status = 'resolved' AND created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);
END //
DELIMITER ;

-- 15. Bật event scheduler
SET GLOBAL event_scheduler = ON;

-- 16. Tạo bảng audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(50) NOT NULL,
    operation ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- 17. Tạo index cho audit log
CREATE INDEX idx_audit_log_table ON audit_log(table_name);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);

-- 18. Tạo trigger audit cho bảng users
DELIMITER //
CREATE TRIGGER users_audit_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, operation, new_values, user_id)
    VALUES ('users', 'INSERT', JSON_OBJECT('id', NEW.id, 'username', NEW.username, 'full_name', NEW.full_name), NEW.id);
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER users_audit_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, operation, old_values, new_values, user_id)
    VALUES ('users', 'UPDATE', 
            JSON_OBJECT('id', OLD.id, 'username', OLD.username, 'full_name', OLD.full_name, 'status', OLD.status),
            JSON_OBJECT('id', NEW.id, 'username', NEW.username, 'full_name', NEW.full_name, 'status', NEW.status),
            NEW.id);
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER users_audit_delete
AFTER DELETE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, operation, old_values, user_id)
    VALUES ('users', 'DELETE', 
            JSON_OBJECT('id', OLD.id, 'username', OLD.username, 'full_name', OLD.full_name, 'status', OLD.status),
            OLD.id);
END //
DELIMITER ;

-- 19. Tạo view cho audit log
CREATE VIEW audit_summary AS
SELECT 
    table_name,
    operation,
    COUNT(*) AS operation_count,
    DATE(timestamp) AS audit_date
FROM audit_log
GROUP BY table_name, operation, DATE(timestamp)
ORDER BY audit_date DESC, operation_count DESC;

-- 20. Cấu hình bảo mật MySQL
-- Lưu ý: Các cấu hình này cần được thêm vào my.cnf hoặc my.ini
-- SET GLOBAL local_infile = 0;
-- SET GLOBAL sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';
-- SET GLOBAL max_connections = 100;
-- SET GLOBAL connect_timeout = 10;
-- SET GLOBAL wait_timeout = 28800;
-- SET GLOBAL interactive_timeout = 28800;
