# Hướng dẫn sử dụng hệ thống Chat

## Yêu cầu hệ thống
- Java 8 hoặc cao hơn
- MySQL 5.7+ hoặc MySQL 8.0+
- RAM tối thiểu: 2GB
- Dung lượng ổ cứng: 500MB

## Cài đặt và chạy ứng dụng

### 1. Chuẩn bị môi trường
```bash
# Kiểm tra phiên bản Java
java -version

# Nếu chưa có Java, cài đặt:
# Ubuntu/Debian: sudo apt-get install openjdk-8-jdk
# macOS: brew install openjdk@8
# Windows: Tải từ https://adoptopenjdk.net/
```

### 2. Thiết lập cơ sở dữ liệu
```bash
# Chạy script tạo database
cd script/database
mysql -u root -p < create_database.sql

# Chèn dữ liệu mẫu (tùy chọn)
mysql -u root -p < ../data/sample_data.sql
```

### 3. Cấu hình kết nối database
Chỉnh sửa file `release/config.properties`:
```properties
db.host=localhost
db.port=3306
db.name=chat_system
db.username=root
db.password=your_password
```

### 4. Chạy ứng dụng

#### Phiên bản 1 (Giao diện)
```bash
# Chạy ứng dụng quản trị
java -jar release/admin-app.jar

# Chạy ứng dụng người dùng
java -jar release/user-app.jar
```

#### Phiên bản 2 (Đầy đủ chức năng)
```bash
# Chạy server trước
java -jar release/chat-server.jar

# Sau đó chạy các ứng dụng client
java -jar release/admin-app.jar
java -jar release/user-app.jar
```

## Hướng dẫn sử dụng

### Phân hệ quản trị

#### 1. Đăng nhập
- Tên đăng nhập: `admin`
- Mật khẩu: `admin123`

#### 2. Quản lý người dùng
- **Xem danh sách**: Menu "Quản lý người dùng" → "Danh sách người dùng"
- **Thêm người dùng**: Nút "Thêm" → Điền thông tin → "Lưu"
- **Sửa thông tin**: Chọn người dùng → Nút "Sửa" → Chỉnh sửa → "Lưu"
- **Khóa tài khoản**: Chọn người dùng → Nút "Khóa"
- **Xem lịch sử đăng nhập**: Menu "Quản lý người dùng" → "Lịch sử đăng nhập"

#### 3. Quản lý nhóm chat
- **Xem danh sách nhóm**: Menu "Quản lý nhóm" → "Danh sách nhóm chat"
- **Xem thành viên**: Chọn nhóm → "Xem thành viên"
- **Xem admin nhóm**: Chọn nhóm → "Xem admin"

#### 4. Báo cáo và thống kê
- **Báo cáo spam**: Menu "Báo cáo" → "Báo cáo spam"
- **Người dùng mới**: Menu "Báo cáo" → "Người dùng mới"
- **Biểu đồ thống kê**: Menu "Thống kê" → Chọn loại biểu đồ

### Phân hệ người dùng

#### 1. Đăng ký tài khoản
- Mở ứng dụng người dùng
- Chọn "Đăng ký"
- Điền thông tin: tên đăng nhập, mật khẩu, họ tên, email
- Nhấn "Đăng ký"

#### 2. Đăng nhập
- Tên đăng nhập: `user1`, `user2`, `user3`, `user4`
- Mật khẩu: `user123`

#### 3. Quản lý bạn bè
- **Xem danh sách bạn bè**: Menu "Bạn bè" → "Danh sách bạn bè"
- **Tìm kiếm bạn bè**: Menu "Bạn bè" → "Tìm kiếm bạn bè"
- **Yêu cầu kết bạn**: Tìm người dùng → "Kết bạn"
- **Chấp nhận/từ chối**: Menu "Bạn bè" → "Yêu cầu kết bạn"

#### 4. Chat
- **Chat riêng**: Menu "Chat" → "Chat riêng" → Chọn người dùng
- **Chat nhóm**: Menu "Chat" → "Nhóm chat" → Chọn nhóm
- **Lịch sử chat**: Menu "Chat" → "Lịch sử chat"
- **Tìm kiếm tin nhắn**: Trong cửa sổ chat → Nút "Tìm kiếm"

#### 5. Quản lý nhóm
- **Tạo nhóm**: Menu "Nhóm" → "Tạo nhóm" → Điền tên nhóm → Thêm thành viên
- **Quản lý nhóm**: Menu "Nhóm" → "Quản lý nhóm" → Chọn nhóm
- **Thêm/xóa thành viên**: Chỉ admin mới có quyền

## Xử lý sự cố

### Lỗi kết nối database
1. Kiểm tra MySQL đang chạy: `sudo systemctl status mysql`
2. Kiểm tra thông tin kết nối trong `config.properties`
3. Kiểm tra quyền truy cập database

### Lỗi ứng dụng không khởi động
1. Kiểm tra phiên bản Java: `java -version`
2. Kiểm tra file jar có đầy đủ không
3. Kiểm tra log file trong thư mục `logs/`

### Lỗi giao diện
1. Khởi động lại ứng dụng
2. Kiểm tra cấu hình màn hình
3. Cập nhật driver Java nếu cần

## Liên hệ hỗ trợ
- Email: support@chatsystem.com
- Hotline: 0123-456-789
- Website: https://chatsystem.com
