# Hướng dẫn cấu hình Database

## Cách 1: Sử dụng Script (Khuyến nghị)

Chạy script tự động:
```bash
./configure_db.sh
```

Script sẽ hỏi bạn:
1. **Project Reference**: Lấy từ Supabase Dashboard
2. **Database Password**: Password bạn đã tạo khi tạo project

## Cách 2: Cấu hình thủ công

### Bước 1: Lấy thông tin từ Supabase

1. Đăng nhập vào [Supabase Dashboard](https://supabase.com/dashboard)
2. Chọn project của bạn
3. Vào **Settings** (biểu tượng bánh răng) → **Database**
4. Cuộn xuống phần **Connection string** → **URI**
5. Copy connection string có dạng:
   ```
   postgresql://postgres:[PASSWORD]@db.xxxxxxxxx.supabase.co:5432/postgres
   ```

### Bước 2: Cập nhật file config.properties

Mở file `release/config.properties` và thay thế:

```properties
# Thay YOUR_PROJECT_REF bằng project reference của bạn
# Ví dụ: nếu URL là db.abcdefghijklmno.supabase.co
# thì project reference là: abcdefghijklmno

db.host=db.YOUR_PROJECT_REF.supabase.co
db.port=5432
db.name=postgres
db.username=postgres
db.password=YOUR_PASSWORD_HERE

db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require

# Supabase API (Optional)
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.anon.key=YOUR_ANON_KEY_HERE
supabase.service.key=YOUR_SERVICE_KEY_HERE
```

### Bước 3: Test kết nối

```bash
java -cp "bin:lib/*" admin.service.DatabaseConnection
```

Nếu thành công sẽ thấy:
```
✅ Connection test SUCCESSFUL!
```

## Ví dụ cấu hình đầy đủ

```properties
# Database Configuration for Supabase PostgreSQL
db.host=db.abcdefghijklmno.supabase.co
db.port=5432
db.name=postgres
db.username=postgres
db.password=MyStrongPassword123!

# JDBC Driver
db.driver=org.postgresql.Driver

# JDBC Connection URL
db.url=jdbc:postgresql://db.abcdefghijklmno.supabase.co:5432/postgres?sslmode=require

# Supabase REST API Configuration (Optional)
supabase.url=https://abcdefghijklmno.supabase.co
supabase.anon.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
supabase.service.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Lấy Supabase API Keys (Optional)

Nếu cần dùng REST API:

1. Vào **Settings** → **API**
2. Copy:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: Key công khai
   - **service_role key**: Key bảo mật (chỉ dùng server-side)

## Troubleshooting

### Lỗi: "password authentication failed"
- Kiểm tra lại password trong `config.properties`
- Reset password trong Supabase: Settings → Database → Reset database password

### Lỗi: "Connection refused" hoặc "No route to host"
- Kiểm tra Project Reference có đúng không
- Kiểm tra project có bị pause không (resume trong Supabase Dashboard)
- Kiểm tra firewall/network

### Lỗi: "SSL error"
- Đảm bảo có `?sslmode=require` trong connection URL
- Kiểm tra SSL certificate

### Lỗi: "org.postgresql.Driver not found"
- Kiểm tra file `lib/postgresql-42.7.1.jar` có tồn tại
- Đảm bảo classpath đúng khi compile/run

## Lưu ý bảo mật

⚠️ **QUAN TRỌNG:**
- File `config.properties` chứa thông tin nhạy cảm (password, API keys)
- File này **KHÔNG được commit** lên GitHub
- Đã được thêm vào `.gitignore`
- Chỉ lưu ở local để app chạy

