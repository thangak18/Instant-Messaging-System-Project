# BÁO CÁO PHIÊN BẢN 1 - HỆ THỐNG CHAT

## 1. THÔNG TIN NHÓM
- **Tên dự án**: Hệ thống Chat
- **Môn học**: Lập trình ứng dụng Java
- **Đồ án cuối kỳ**
- **Ngày nộp**: [Ngày hiện tại]

## 2. DANH SÁCH CÔNG VIỆC TỪNG THÀNH VIÊN

### Thành viên 1: [Tên thành viên]
- Thiết kế giao diện phân hệ quản trị
- Tạo cấu trúc database
- Viết script tạo database
- Tạo tài liệu hướng dẫn

### Thành viên 2: [Tên thành viên]
- Thiết kế giao diện phân hệ người dùng
- Tạo dữ liệu mẫu
- Viết script backup/restore
- Tạo tài liệu báo cáo

### Thành viên 3: [Tên thành viên]
- Thiết kế giao diện chat
- Tạo script tối ưu database
- Viết tài liệu hướng dẫn sử dụng
- Tạo script giám sát

## 3. BẢNG ĐÓNG GÓP TỪNG THÀNH VIÊN (Tổng: 100%)

| Thành viên | Công việc | Đóng góp (%) |
|------------|-----------|--------------|
| Thành viên 1 | Giao diện admin, Database | 35% |
| Thành viên 2 | Giao diện user, Dữ liệu mẫu | 35% |
| Thành viên 3 | Giao diện chat, Scripts | 30% |

## 4. THIẾT KẾ CƠ SỞ DỮ LIỆU

### 4.1. Các bảng chính:

#### Bảng `users`
- `id`: Khóa chính
- `username`: Tên đăng nhập (unique)
- `password`: Mật khẩu (mã hóa)
- `full_name`: Họ tên
- `email`: Email (unique)
- `address`: Địa chỉ
- `birth_date`: Ngày sinh
- `gender`: Giới tính
- `status`: Trạng thái (active/locked)
- `created_at`: Ngày tạo
- `updated_at`: Ngày cập nhật

#### Bảng `login_history`
- `id`: Khóa chính
- `user_id`: ID người dùng (FK)
- `login_time`: Thời gian đăng nhập
- `ip_address`: Địa chỉ IP
- `user_agent`: Thông tin trình duyệt

#### Bảng `friendships`
- `id`: Khóa chính
- `user1_id`: ID người dùng 1 (FK)
- `user2_id`: ID người dùng 2 (FK)
- `status`: Trạng thái (pending/accepted/blocked)
- `created_at`: Ngày tạo
- `updated_at`: Ngày cập nhật

#### Bảng `chat_groups`
- `id`: Khóa chính
- `group_name`: Tên nhóm
- `description`: Mô tả
- `created_by`: Người tạo (FK)
- `created_at`: Ngày tạo
- `updated_at`: Ngày cập nhật

#### Bảng `group_members`
- `id`: Khóa chính
- `group_id`: ID nhóm (FK)
- `user_id`: ID người dùng (FK)
- `role`: Vai trò (admin/member)
- `joined_at`: Ngày tham gia

#### Bảng `private_messages`
- `id`: Khóa chính
- `sender_id`: ID người gửi (FK)
- `receiver_id`: ID người nhận (FK)
- `message`: Nội dung tin nhắn
- `message_type`: Loại tin nhắn (text/image/file)
- `sent_at`: Thời gian gửi
- `is_read`: Đã đọc chưa

#### Bảng `group_messages`
- `id`: Khóa chính
- `group_id`: ID nhóm (FK)
- `sender_id`: ID người gửi (FK)
- `message`: Nội dung tin nhắn
- `message_type`: Loại tin nhắn (text/image/file)
- `sent_at`: Thời gian gửi

#### Bảng `spam_reports`
- `id`: Khóa chính
- `reporter_id`: ID người báo cáo (FK)
- `reported_user_id`: ID người bị báo cáo (FK)
- `reason`: Lý do báo cáo
- `created_at`: Ngày tạo
- `status`: Trạng thái (pending/resolved/dismissed)

#### Bảng `user_activities`
- `id`: Khóa chính
- `user_id`: ID người dùng (FK)
- `activity_type`: Loại hoạt động
- `activity_data`: Dữ liệu hoạt động (JSON)
- `created_at`: Thời gian tạo

### 4.2. Quan hệ giữa các bảng:
- `users` ↔ `login_history` (1:N)
- `users` ↔ `friendships` (N:N)
- `users` ↔ `chat_groups` (1:N)
- `chat_groups` ↔ `group_members` (1:N)
- `users` ↔ `private_messages` (1:N)
- `chat_groups` ↔ `group_messages` (1:N)
- `users` ↔ `spam_reports` (1:N)
- `users` ↔ `user_activities` (1:N)

## 5. DANH SÁCH CÁC MÀN HÌNH ĐÃ THIẾT KẾ

### 5.1. Phân hệ quản trị:

#### 5.1.1. AdminMainFrame
- **Mô tả**: Giao diện chính của phân hệ quản trị
- **Chức năng**: Menu chính, điều hướng đến các chức năng khác
- **Các menu**: Quản lý người dùng, Quản lý nhóm, Báo cáo, Thống kê, Hệ thống

#### 5.1.2. UserManagementFrame
- **Mô tả**: Quản lý danh sách người dùng
- **Chức năng**: Xem, thêm, sửa, xóa, khóa/mở khóa người dùng
- **Tính năng**: Tìm kiếm, lọc, sắp xếp

#### 5.1.3. LoginHistoryFrame
- **Mô tả**: Xem lịch sử đăng nhập
- **Chức năng**: Hiển thị lịch sử đăng nhập theo thời gian
- **Tính năng**: Tìm kiếm, sắp xếp, xuất báo cáo

#### 5.1.4. GroupManagementFrame
- **Mô tả**: Quản lý nhóm chat
- **Chức năng**: Xem danh sách nhóm, thành viên, admin
- **Tính năng**: Tìm kiếm, sắp xếp

#### 5.1.5. SpamReportFrame
- **Mô tả**: Quản lý báo cáo spam
- **Chức năng**: Xem, xử lý báo cáo spam
- **Tính năng**: Tìm kiếm, lọc, khóa tài khoản

#### 5.1.6. StatisticsFrame
- **Mô tả**: Thống kê và biểu đồ
- **Chức năng**: Hiển thị biểu đồ thống kê
- **Tính năng**: Chọn năm, loại biểu đồ, xuất báo cáo

### 5.2. Phân hệ người dùng:

#### 5.2.1. UserMainFrame
- **Mô tả**: Giao diện chính của phân hệ người dùng
- **Chức năng**: Menu chính, điều hướng đến các chức năng khác
- **Các menu**: Tài khoản, Bạn bè, Chat, Nhóm, Báo cáo

#### 5.2.2. LoginFrame
- **Mô tả**: Giao diện đăng nhập
- **Chức năng**: Đăng nhập, đăng ký, quên mật khẩu
- **Tính năng**: Xác thực người dùng

#### 5.2.3. RegisterFrame
- **Mô tả**: Giao diện đăng ký
- **Chức năng**: Đăng ký tài khoản mới
- **Tính năng**: Nhập thông tin cá nhân, xác thực

#### 5.2.4. ChatFrame
- **Mô tả**: Giao diện chat riêng
- **Chức năng**: Chat với 1 người dùng
- **Tính năng**: Gửi tin nhắn, tìm kiếm, xóa lịch sử

#### 5.2.5. GroupChatFrame
- **Mô tả**: Giao diện chat nhóm
- **Chức năng**: Chat trong nhóm
- **Tính năng**: Gửi tin nhắn, quản lý thành viên

#### 5.2.6. FriendsListFrame
- **Mô tả**: Danh sách bạn bè
- **Chức năng**: Xem, quản lý bạn bè
- **Tính năng**: Tìm kiếm, chat, hủy kết bạn, block

## 6. HÌNH ẢNH CÁC MÀN HÌNH

### 6.1. Phân hệ quản trị:
- [Hình ảnh AdminMainFrame]
- [Hình ảnh UserManagementFrame]
- [Hình ảnh LoginHistoryFrame]
- [Hình ảnh GroupManagementFrame]
- [Hình ảnh SpamReportFrame]
- [Hình ảnh StatisticsFrame]

### 6.2. Phân hệ người dùng:
- [Hình ảnh UserMainFrame]
- [Hình ảnh LoginFrame]
- [Hình ảnh RegisterFrame]
- [Hình ảnh ChatFrame]
- [Hình ảnh GroupChatFrame]
- [Hình ảnh FriendsListFrame]

## 7. KẾT LUẬN

### 7.1. Những gì đã hoàn thành:
- ✅ Thiết kế giao diện cho tất cả các màn hình
- ✅ Tạo cấu trúc database hoàn chỉnh
- ✅ Viết script tạo database và dữ liệu mẫu
- ✅ Tạo tài liệu hướng dẫn sử dụng
- ✅ Chuẩn bị sẵn sàng cho phiên bản 2

### 7.2. Những gì cần làm trong phiên bản 2:
- 🔄 Lập trình logic xử lý cho các giao diện
- 🔄 Kết nối database và xử lý dữ liệu
- 🔄 Implement các chức năng chat thời gian thực
- 🔄 Tạo file JAR và cấu hình
- 🔄 Testing và debug

### 7.3. Đánh giá:
- **Giao diện**: Đẹp, thân thiện, dễ sử dụng
- **Database**: Thiết kế tốt, đầy đủ chức năng
- **Tài liệu**: Chi tiết, dễ hiểu
- **Chuẩn bị**: Sẵn sàng cho phiên bản 2

---
**Ngày hoàn thành**: [Ngày hiện tại]  
**Trạng thái**: Hoàn thành phiên bản 1  
**Tiếp theo**: Phát triển phiên bản 2
