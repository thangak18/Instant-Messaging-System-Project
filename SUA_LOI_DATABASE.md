# 🔧 Sửa lỗi "The connection attempt failed"

## ❌ Lỗi hiện tại:
```
Lỗi load dữ liệu: The connection attempt failed.
```

## 🔍 Nguyên nhân:
File `release/config.properties` vẫn còn các giá trị placeholder chưa được thay thế:
- `YOUR_PROJECT_REF` - Chưa có Project Reference thực tế
- `YOUR_PASSWORD_HERE` - Chưa có Database Password thực tế

## ✅ Giải pháp:

### Cách 1: Sử dụng Script tự động (Khuyến nghị)

```bash
./configure_db.sh
```

Script sẽ hỏi bạn:
1. **Project Reference**: Lấy từ Supabase Dashboard
2. **Database Password**: Password bạn đã tạo khi tạo project

### Cách 2: Cấu hình thủ công

1. **Lấy thông tin từ Supabase:**
   - Đăng nhập [Supabase Dashboard](https://supabase.com/dashboard)
   - Chọn project → **Settings** → **Database**
   - Copy **Connection string (URI)** hoặc lấy:
     - **Project Reference**: Phần `xxxxx` trong `db.xxxxx.supabase.co`
     - **Password**: Database password bạn đã tạo

2. **Sửa file `release/config.properties`:**
   ```properties
   # Thay YOUR_PROJECT_REF bằng project reference thực tế
   db.host=db.abcdefghijklmno.supabase.co
   db.url=jdbc:postgresql://db.abcdefghijklmno.supabase.co:5432/postgres?sslmode=require
   
   # Thay YOUR_PASSWORD_HERE bằng password thực tế
   db.password=YourActualPassword123!
   
   # Cập nhật Supabase URL
   supabase.url=https://abcdefghijklmno.supabase.co
   ```

3. **Test kết nối:**
   ```bash
   java -cp "bin:lib/*" admin.service.DatabaseConnection
   ```

## 📋 Checklist:

- [ ] Đã có Supabase project
- [ ] Đã lấy Project Reference từ Supabase Dashboard
- [ ] Đã lấy Database Password
- [ ] Đã cập nhật file `release/config.properties`
- [ ] Đã test kết nối thành công
- [ ] Admin panel có thể load dữ liệu

## 🚨 Lưu ý:

- File `config.properties` **KHÔNG được commit** lên GitHub (đã có trong `.gitignore`)
- File này chỉ lưu ở local để app chạy
- Nếu project Supabase bị pause, cần resume trong Dashboard

## 💡 Sau khi cấu hình:

1. **Compile lại code:**
   ```bash
   ./run_admin.sh
   ```

2. **Hoặc compile thủ công:**
   ```bash
   javac -d bin -cp "lib/*:src" src/admin/**/*.java
   java -cp "bin:lib/*" admin.gui.AdminMainFrame
   ```

3. **Kiểm tra:**
   - Admin panel mở được
   - Không còn lỗi "connection attempt failed"
   - Có thể load dữ liệu từ database

