# Hướng dẫn cấu hình Supabase cho Hệ thống Chat

## Bước 1: Tạo Project trên Supabase

1. Truy cập https://supabase.com và đăng nhập
2. Click **New Project**
3. Điền thông tin:
   - **Name**: Tên project (ví dụ: instant-messaging-system)
   - **Database Password**: Mật khẩu mạnh (lưu lại để dùng sau)
   - **Region**: Chọn gần nhất (ví dụ: Southeast Asia)
4. Click **Create new project** và chờ vài phút

## Bước 2: Lấy thông tin kết nối

1. Trong Supabase Dashboard, click vào **Settings** (biểu tượng bánh răng)
2. Chọn **Database**
3. Cuộn xuống phần **Connection string** → **URI**
4. Copy connection string có dạng:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxxxxx.supabase.co:5432/postgres
   ```

## Bước 3: Cấu hình file config.properties

1. Mở file `release/config.properties`
2. Thay thế các giá trị:
   - **YOUR_PROJECT_REF**: Lấy từ connection string (phần `xxxxxxxxx` trong `db.xxxxxxxxx.supabase.co`)
   - **YOUR_PASSWORD**: Database password bạn đã tạo ở Bước 1

Ví dụ:
```properties
db.host=db.abcdefghijklmno.supabase.co
db.port=5432
db.name=postgres
db.username=postgres
db.password=YourStrongPassword123!

db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://db.abcdefghijklmno.supabase.co:5432/postgres?sslmode=require
```

## Bước 4: Tạo Database Schema

1. Trong Supabase Dashboard, click vào **SQL Editor**
2. Click **New query**
3. Copy toàn bộ nội dung file `script/database/create_database_supabase.sql`
4. Paste vào SQL Editor
5. Click **Run** để tạo các bảng

## Bước 5: Test kết nối

Chạy lệnh để test kết nối:
```bash
javac -d bin -cp "lib/*:src" src/user/service/DatabaseConnection.java
java -cp "bin:lib/*" user.service.DatabaseConnection
```

Nếu thành công sẽ thấy:
```
PostgreSQL Driver loaded!
Config loaded - URL: jdbc:postgresql://...
Testing Supabase connection...
SUCCESS! Connected to Supabase!
```

## Bước 6: Import dữ liệu mẫu (Optional)

Nếu muốn thêm dữ liệu mẫu:
1. Mở file `script/data/sample_data.sql`
2. Chỉnh sửa syntax cho PostgreSQL nếu cần
3. Chạy trong Supabase SQL Editor

## Lưu ý quan trọng

### Bảo mật
- **KHÔNG** commit file `config.properties` với password thật lên Git
- Thêm vào `.gitignore`:
  ```
  release/config.properties
  **/config.properties
  ```

### Khác biệt MySQL vs PostgreSQL
- `AUTO_INCREMENT` → `SERIAL`
- `ENUM('value1', 'value2')` → `VARCHAR CHECK (column IN ('value1', 'value2'))`
- `JSON` → `JSONB` (hiệu suất tốt hơn)
- `NOW()` → `CURRENT_TIMESTAMP`
- `DATE_SUB(NOW(), INTERVAL 1 DAY)` → `CURRENT_TIMESTAMP - INTERVAL '1 day'`

### Row Level Security (RLS)
Supabase mặc định bật RLS. Nếu gặp lỗi permission:
1. Vào **Authentication** → **Policies**
2. Tạm thời disable RLS cho development:
   ```sql
   ALTER TABLE users DISABLE ROW LEVEL SECURITY;
   ALTER TABLE login_history DISABLE ROW LEVEL SECURITY;
   -- ... các bảng khác
   ```

### Connection Pooling
Supabase có giới hạn kết nối. Nên dùng connection pooling:
- Free tier: 60 connections
- URL có pooler: `db.xxx.supabase.co:6543` (port 6543)

## Kiểm tra ứng dụng

Sau khi cấu hình xong:

1. **Test Login**:
   ```bash
   ./run_login.sh
   ```

2. **Test Admin Panel**:
   ```bash
   ./run_admin.sh
   ```

3. **Test User Interface**:
   ```bash
   ./run_user.sh
   ```

## Troubleshooting

### Lỗi: "FATAL: password authentication failed"
- Kiểm tra lại password trong `config.properties`
- Reset password trong Supabase Settings → Database

### Lỗi: "SSL error" hoặc "Connection refused"
- Đảm bảo có `?sslmode=require` trong connection URL
- Kiểm tra firewall/network

### Lỗi: "org.postgresql.Driver not found"
- Kiểm tra file `lib/postgresql-42.7.1.jar` có tồn tại
- Đảm bảo classpath đúng khi compile

### Lỗi: "relation does not exist"
- Chưa chạy script tạo database
- Chạy lại `create_database_supabase.sql` trong SQL Editor

## Hỗ trợ

- Supabase Docs: https://supabase.com/docs
- PostgreSQL Docs: https://www.postgresql.org/docs/
