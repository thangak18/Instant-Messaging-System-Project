# Hướng dẫn tạo cơ sở dữ liệu

## Yêu cầu hệ thống
- MySQL 5.7+ hoặc MySQL 8.0+
- Java 8+
- JDBC Driver cho MySQL

## Các bước thực hiện

### 1. Cài đặt MySQL
```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# macOS (với Homebrew)
brew install mysql

# Windows
# Tải và cài đặt từ https://dev.mysql.com/downloads/mysql/
```

### 2. Khởi động MySQL
```bash
# Ubuntu/Debian
sudo systemctl start mysql
sudo systemctl enable mysql

# macOS
brew services start mysql

# Windows
# Khởi động MySQL Service từ Services
```

### 3. Tạo database và user
```sql
-- Đăng nhập MySQL với quyền root
mysql -u root -p

-- Tạo user mới (tùy chọn)
CREATE USER 'chat_user'@'localhost' IDENTIFIED BY 'chat_password';
GRANT ALL PRIVILEGES ON chat_system.* TO 'chat_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Chạy script tạo database
```bash
# Chạy script tạo cấu trúc database
mysql -u root -p < create_database.sql

# Chạy script chèn dữ liệu mẫu (tùy chọn)
mysql -u root -p < ../data/sample_data.sql
```

### 5. Kiểm tra kết quả
```sql
-- Kiểm tra các bảng đã được tạo
USE chat_system;
SHOW TABLES;

-- Kiểm tra dữ liệu mẫu
SELECT * FROM users;
SELECT * FROM friendships;
SELECT * FROM chat_groups;
```

## Cấu trúc database

### Các bảng chính:
- **users**: Thông tin người dùng
- **login_history**: Lịch sử đăng nhập
- **friendships**: Quan hệ bạn bè
- **chat_groups**: Nhóm chat
- **group_members**: Thành viên nhóm
- **private_messages**: Tin nhắn riêng
- **group_messages**: Tin nhắn nhóm
- **spam_reports**: Báo cáo spam
- **user_activities**: Hoạt động người dùng

### Quan hệ giữa các bảng:
- users ↔ login_history (1:N)
- users ↔ friendships (N:N)
- users ↔ chat_groups (1:N)
- chat_groups ↔ group_members (1:N)
- users ↔ private_messages (1:N)
- chat_groups ↔ group_messages (1:N)
- users ↔ spam_reports (1:N)
- users ↔ user_activities (1:N)

## Lưu ý
- Đảm bảo MySQL đang chạy trước khi chạy script
- Kiểm tra quyền truy cập database
- Backup database trước khi chạy script trên dữ liệu thật
- Cập nhật thông tin kết nối trong file `config.properties`