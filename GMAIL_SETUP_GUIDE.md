# Hướng Dẫn Cấu Hình Gmail Để Gửi Email Thật

## Bước 1: Tạo Gmail App Password

### Yêu cầu:
- Tài khoản Gmail
- Bật 2-Step Verification (xác thực 2 bước)

### Các bước:

1. **Truy cập Google Account Security:**
   - Vào: https://myaccount.google.com/security
   - Hoặc: Gmail → Click avatar → "Manage your Google Account" → "Security"

2. **Bật 2-Step Verification** (nếu chưa có):
   - Tìm mục "2-Step Verification"
   - Click "Get Started"
   - Làm theo hướng dẫn (nhập số điện thoại, xác thực OTP...)

3. **Tạo App Password:**
   - Sau khi bật 2-Step Verification
   - Quay lại trang Security
   - Tìm "App passwords" (hoặc truy cập: https://myaccount.google.com/apppasswords)
   - Click vào
   - Chọn app: **Mail**
   - Chọn device: **Windows Computer**
   - Click "Generate"
   - **Copy mật khẩu 16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)
   
   **LƯU Ý:** Mật khẩu này CHỈ hiển thị 1 LẦN, hãy lưu lại!

---

## Bước 2: Cập Nhật config.properties

Mở file `release/config.properties` và thêm:

```properties
# Email Configuration
email.host=smtp.gmail.com
email.port=587
email.username=fitehcmus@gmail.com          # <-- Thay bằng email của BẠN
email.password=abcd efgh ijkl mnop          # <-- Thay bằng App Password vừa tạo
email.from=fitehcmus@gmail.com              # <-- Thay bằng email của BẠN
email.from.name=InstantChat System
```

**Ví dụ thực tế:**
```properties
email.username=fitehcmus@gmail.com
email.password=xyzw abcd efgh ijkl
email.from=fitehcmus@gmail.com
```

---

## Bước 3: Compile và Chạy

```bash
# Compile với JavaMail
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar" -d bin src\user\service\*.java src\user\gui\*.java

# Chạy ứng dụng
java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar" user.gui.Main
```

---

## Bước 4: Test Gửi Email

### Test 1: Quên Mật Khẩu
1. Chạy ứng dụng
2. Click "Quên mật khẩu?"
3. Nhập email đã đăng ký (ví dụ: `tmhung23@clc.fitus.edu.vn`)
4. Kiểm tra hộp thư email → Sẽ nhận được email với mật khẩu tạm

### Test 2: Đăng Ký với Email Verification (sắp làm)
1. Đăng ký tài khoản mới
2. Kiểm tra email → Nhận link kích hoạt
3. Click link → Tài khoản được kích hoạt

---

## Khắc Phục Sự Cố

### Lỗi 1: Authentication failed
**Nguyên nhân:** Sai App Password hoặc chưa bật 2-Step Verification

**Giải pháp:**
- Kiểm tra lại App Password trong config.properties
- Đảm bảo đã bật 2-Step Verification
- Tạo lại App Password mới

### Lỗi 2: javax.mail không tìm thấy
**Nguyên nhân:** Chưa thêm javax.mail.jar vào classpath

**Giải pháp:**
```bash
# Kiểm tra file có tồn tại
dir lib\javax.mail.jar

# Compile lại với đầy đủ classpath
javac -cp "lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar" ...
```

### Lỗi 3: Email không gửi được (5 phút không nhận được email)
**Kiểm tra:**
1. Kiểm tra console có lỗi gì không
2. Kiểm tra email có bị vào Spam không
3. Thử gửi đến email khác
4. Kiểm tra internet connection

---

## Email HTML Sample

Email sẽ có giao diện đẹp với HTML:

```
┌─────────────────────────────────────────┐
│         💬 InstantChat                  │
│                                         │
│  Xin chào Trần Mạnh Hùng,               │
│                                         │
│  Mật khẩu tạm thời của bạn:             │
│  ┌────────────────────┐                 │
│  │  iEf*5edi&lQ8      │                 │
│  └────────────────────┘                 │
│                                         │
│  ⚠️ LƯU Ý BẢO MẬT:                     │
│  • Đổi mật khẩu ngay sau khi đăng nhập  │
│  • Không chia sẻ với ai                 │
│                                         │
│  © 2025 InstantChat System              │
└─────────────────────────────────────────┘
```

---

## Tóm Tắt

✅ **Đã làm:**
- Download javax.mail.jar
- Cập nhật EmailService để gửi email thật qua Gmail SMTP
- Email có template HTML đẹp

🔧 **Cần làm ngay:**
- Tạo Gmail App Password
- Cập nhật config.properties với email và password
- Compile và test

📧 **Email sẽ gửi thật đến Gmail!**
