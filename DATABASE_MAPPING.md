# Mapping Cấu Trúc Database Thực Tế (Supabase PostgreSQL)

> **Lưu ý quan trọng**: Cấu trúc database thực tế trên Supabase khác với schema ban đầu.
> File này ghi lại mapping chính xác để tránh lỗi khi code.

## 📋 Tổng hợp các bảng

### 1. **users** - Bảng người dùng
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `user_id` | serial | ⚠️ Không phải `id` |
| `username` | varchar | |
| `password` | varchar | |
| `email` | varchar | |
| `full_name` | varchar | |
| `address` | varchar | |
| `dob` | date | ⚠️ Không phải `birth_date` |
| `gender` | varchar | |
| `email_verified` | bool | |
| `verification_code` | varchar | |
| `verification_code_expiry` | timestamp | |
| `status` | varchar | |
| `created_at` | timestamp | |
| `last_login` | timestamp | ⚠️ Không có `updated_at` |

### 2. **login_history** - Lịch sử đăng nhập
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `history_id` | serial | ⚠️ Không phải `id` |
| `user_id` | int4 | FK → users(user_id) |
| `login_time` | timestamp | |
| `ip_address` | varchar | |
| `device_info` | varchar | ⚠️ Không phải `user_agent` |

### 3. **friends** - Bảng bạn bè
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `friendship_id` | serial | ⚠️ Không phải `id` |
| `user_id` | int4 | ⚠️ Không phải `user1_id` |
| `friend_id` | int4 | ⚠️ Không phải `user2_id` |
| `status` | varchar | accepted/pending/blocked |
| `created_at` | timestamp | |
| `updated_at` | timestamp | |

**Tên bảng:** ⚠️ `friends` (không phải `friendships`)

### 4. **groups** - Bảng nhóm chat
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `group_id` | serial | ⚠️ Không phải `id` |
| `group_name` | varchar | |
| `admin_id` | int4 | ⚠️ Không phải `created_by`, FK → users(user_id) |
| `created_at` | timestamp | |
| `encrypted` | bool | |

**Tên bảng:** ⚠️ `groups` (không phải `chat_groups`)  
**Không có cột:** `description`, `updated_at`

### 5. **group_members** - Thành viên nhóm
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `member_id` | serial | ⚠️ Không phải `id` |
| `group_id` | int4 | FK → groups(group_id) |
| `user_id` | int4 | FK → users(user_id) |
| `joined_at` | timestamp | |

**Không có cột:** `role`, `added_by`

### 6. **spam_reports** - Báo cáo spam
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `report_id` | serial | ⚠️ Không phải `id` |
| `reporter_id` | int4 | FK → users(user_id) |
| `reported_user_id` | int4 | FK → users(user_id) |
| `reason` | text | |
| `report_time` | timestamp | ⚠️ Không phải `created_at` |
| `status` | varchar | pending/resolved/rejected |

### 7. **messages** - Tin nhắn riêng tư
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `message_id` | serial | ⚠️ Không phải `id` |
| `sender_id` | int4 | FK → users(user_id) |
| `receiver_id` | int4 | FK → users(user_id) |
| `content` | text | |
| `created_at` | timestamp | ⚠️ Không phải `sent_at` |
| `is_read` | bool | |

**Tên bảng:** ⚠️ `messages` (không phải `private_messages`)

### 8. **group_messages** - Tin nhắn nhóm
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `message_id` | serial | ⚠️ Không phải `id` |
| `group_id` | int4 | FK → groups(group_id) |
| `sender_id` | int4 | FK → users(user_id) |
| `message_text` | text | ⚠️ Không phải `content` |
| `sent_time` | timestamp | ⚠️ Không phải `sent_at` |

### 9. **deleted_messages** - Tin nhắn đã xóa
| Cột | Kiểu dữ liệu | Ghi chú |
|-----|--------------|---------|
| `message_id` | int4 | |
| `user_id` | int4 | |
| `deleted_at` | timestamp | |

## 🔧 Các điểm khác biệt quan trọng

### Tên cột ID
- ⚠️ Tất cả bảng dùng `<table_name>_id` thay vì `id`
- VD: `user_id`, `group_id`, `history_id`, `report_id`, v.v.

### Cú pháp SQL
- ⚠️ PostgreSQL không hỗ trợ `MONTH()`, `YEAR()` 
- ✅ Dùng `EXTRACT(MONTH FROM column)`, `EXTRACT(YEAR FROM column)`

### INTERVAL với parameterized query
- ⚠️ PostgreSQL không hỗ trợ `INTERVAL ? DAY` trong PreparedStatement
- ✅ Dùng `? * INTERVAL '1 day'` thay thế
- VD: `WHERE created_at >= NOW() - (? * INTERVAL '1 day')`

### Tên bảng
- `friends` (không phải `friendships`)
- `groups` (không phải `chat_groups`)
- `messages` (không phải `private_messages`)

### Tên cột timestamp
- `report_time` trong `spam_reports` (không phải `created_at`)
- `sent_time` trong `group_messages` (không phải `sent_at`)
- `device_info` trong `login_history` (không phải `user_agent`)

## 📝 Checklist khi viết SQL query mới

- [ ] Dùng đúng tên bảng (`friends`, `groups`, `messages`)
- [ ] Dùng đúng tên cột ID (`user_id`, `group_id`, etc.)
- [ ] Dùng `EXTRACT()` thay vì `MONTH()`/`YEAR()`
- [ ] Dùng `? * INTERVAL '1 day'` thay vì `INTERVAL ? DAY`
- [ ] Kiểm tra tên cột timestamp (có thể khác nhau giữa các bảng)
- [ ] Test query với PostgreSQL syntax

## 🎯 Files đã được cập nhật

✅ `UserDAO.java` - Tất cả queries về users  
✅ `StatisticsDAO.java` - Tất cả queries thống kê  
✅ `LoginHistoryDAO.java` - Queries lịch sử đăng nhập  
✅ `GroupDAO.java` - Queries về nhóm chat  
✅ `SpamReportDAO.java` - Queries báo cáo spam  

---
*Cập nhật: 27/11/2025*

