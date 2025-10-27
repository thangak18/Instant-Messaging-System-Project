# 🚀 HƯỚNG DẪN CÀI ĐẶT GỬI EMAIL THẬT

## ✅ ĐÃ HOÀN THÀNH

1. ✅ Download JavaMail API (`lib\javax.mail.jar`)
2. ✅ Cập nhật EmailService để gửi email qua Gmail SMTP
3. ✅ Email template HTML đẹp
4. ✅ Tạo script compile và run

---

## 🔧 BƯỚC TIẾP THEO - CẦN BẠN LÀM

### Bước 1: Tạo Gmail App Password

**Link nhanh:** https://myaccount.google.com/apppasswords

**Chi tiết:**

1. **Truy cập Google Account:**
   - Vào Gmail
   - Click vào avatar (góc trên bên phải)
   - Chọn "Manage your Google Account"

2. **Vào Security:**
   - Click tab "Security" (bên trái)
   - Cuộn xuống tìm "2-Step Verification"

3. **Bật 2-Step Verification** (nếu chưa có):
   - Click "2-Step Verification"
   - Click "Get Started"
   - Nhập số điện thoại
   - Nhận mã OTP và xác thực

4. **Tạo App Password:**
   - Sau khi bật 2-Step, quay lại Security
   - Tìm "App passwords" (có thể phải scroll xuống)
   - Click vào
   - Nhập password Gmail để confirm
   - Chọn app: "Mail"
   - Chọn device: "Windows Computer"  
   - Click "Generate"
   - **LƯU MẬT KHẨU 16 KÝ TỰ!** (ví dụ: `abcd efgh ijkl mnop`)

**Quan trọng:** Mật khẩu này chỉ hiển thị 1 lần!

---

### Bước 2: Cập Nhật config.properties

Mở file: `release\config.properties`

Tìm phần Email Configuration và thay đổi:

```properties
# Email Configuration
email.host=smtp.gmail.com
email.port=587
email.username=fitehcmus@gmail.com           # <-- THAY BẰNG EMAIL CỦA BẠN
email.password=abcd efgh ijkl mnop           # <-- THAY BẰNG APP PASSWORD VỪA TẠO
email.from=fitehcmus@gmail.com               # <-- THAY BẰNG EMAIL CỦA BẠN
email.from.name=InstantChat System
```

**Ví dụ thực tế:**

Giả sử email của bạn là `fitehcmus@gmail.com` và App Password là `xyzw abcd 1234 5678`:

```properties
email.username=fitehcmus@gmail.com
email.password=xyzw abcd 1234 5678
email.from=fitehcmus@gmail.com
```

**LƯU Ý:**
- App Password có thể có dấu cách (không sao, JavaMail sẽ xử lý)
- Không dùng mật khẩu Gmail thường, phải dùng App Password
- email.from phải giống email.username

---

### Bước 3: Compile

Chạy file batch:

```bash
compile_with_email.bat
```

Hoặc chạy lệnh:

```bash
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar" -d bin src\user\service\*.java src\user\gui\*.java
```

---

### Bước 4: Chạy Ứng Dụng

Chạy file batch:

```bash
run_with_email.bat
```

Hoặc chạy lệnh:

```bash
java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar;lib\javax.mail.jar" user.gui.Main
```

---

### Bước 5: Test Gửi Email

1. **Click "Quên mật khẩu?"** trong LoginFrame

2. **Nhập email đã đăng ký:**
   - Ví dụ: `tmhung23@clc.fitus.edu.vn`

3. **Kiểm tra console:**
   ```
   📧 Đang gửi email đến: tmhung23@clc.fitus.edu.vn...
   ✅ Email đã được gửi thành công!
   ```

4. **Kiểm tra hộp thư email:**
   - Mở Gmail
   - Tìm email từ "InstantChat System"
   - Nếu không thấy, check Spam/Junk

5. **Email sẽ có dạng:**
   ```
   Subject: [InstantChat] Khôi Phục Mật Khẩu
   From: InstantChat System <fitehcmus@gmail.com>
   
   Xin chào Trần Mạnh Hùng,
   
   Mật khẩu tạm thời của bạn:
   iEf*5edi&lQ8
   
   ⚠️ LƯU Ý BẢO MẬT:
   • Đổi mật khẩu ngay sau khi đăng nhập
   • Không chia sẻ với ai
   ```

6. **Copy mật khẩu tạm** từ email

7. **Trong ResetPasswordFrame:**
   - Ô 1: Nhập mật khẩu tạm
   - Ô 2: Nhập mật khẩu mới
   - Ô 3: Xác nhận
   - Click "CẬP NHẬT MẬT KHẨU"

8. **Đăng nhập lại** với mật khẩu mới

---

## 🐛 Khắc Phục Lỗi

### Lỗi: "Email chưa được cấu hình"

**Console hiển thị:**
```
❌ Email chưa được cấu hình!
   Vui lòng cập nhật email.username và email.password trong config.properties
```

**Giải pháp:**
- Kiểm tra file `release\config.properties`
- Đảm bảo đã thay `YOUR_GMAIL_HERE` và `YOUR_APP_PASSWORD_HERE`

---

### Lỗi: "Authentication failed"

**Console hiển thị:**
```
❌ Lỗi khi gửi email: Authentication failed
javax.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Nguyên nhân:**
- Sai App Password
- Chưa bật 2-Step Verification
- Dùng mật khẩu Gmail thường thay vì App Password

**Giải pháp:**
1. Kiểm tra lại App Password trong config.properties
2. Đảm bảo đã bật 2-Step Verification
3. Tạo lại App Password mới
4. Copy chính xác (kể cả dấu cách)

---

### Lỗi: "javax.mail không tìm thấy"

**Console hiển thị:**
```
error: package javax.mail does not exist
```

**Nguyên nhân:** Chưa thêm javax.mail.jar vào classpath

**Giải pháp:**
```bash
# Kiểm tra file có tồn tại
dir lib\javax.mail.jar

# Compile với đầy đủ classpath
compile_with_email.bat
```

---

### Email không nhận được sau 5 phút

**Kiểm tra:**

1. **Console có lỗi không?**
   - Xem log trong console
   - Có thông báo "✅ Email đã được gửi thành công!" không?

2. **Check Spam folder:**
   - Mở Gmail
   - Click "Spam" (bên trái)
   - Tìm email từ InstantChat

3. **Thử email khác:**
   - Test với email khác (Yahoo, Outlook...)
   - Xem có nhận được không

4. **Kiểm tra internet:**
   - Ping google.com
   - Đảm bảo có kết nối

---

## 📧 Tính Năng Email Verification (Sắp Làm)

**Flow:**

1. User đăng ký với email
2. Hệ thống gửi email verification
3. Email chứa link: `http://localhost/verify?token=abc123`
4. User click link → Kích hoạt tài khoản
5. Chỉ tài khoản verified mới đăng nhập được

**Ưu điểm:**
- ✅ Đảm bảo email thật 100%
- ✅ Ngăn spam registration
- ✅ Tiêu chuẩn của các website lớn

**Sẽ implement sau khi test xong quên mật khẩu!**

---

## 📝 Checklist

- [ ] Tạo Gmail App Password
- [ ] Cập nhật config.properties với email và password
- [ ] Chạy compile_with_email.bat
- [ ] Chạy run_with_email.bat
- [ ] Test quên mật khẩu
- [ ] Kiểm tra email đã nhận được
- [ ] Verify có thể reset password thành công

---

## 🎯 Tóm Tắt

**Đã làm:**
✅ Download javax.mail.jar
✅ Code gửi email thật qua Gmail SMTP  
✅ Email template HTML đẹp
✅ Script compile và run

**Cần làm ngay:**
🔧 Tạo Gmail App Password (2 phút)
🔧 Cập nhật config.properties (30 giây)
🔧 Compile và test (1 phút)

**Sau đó:**
📧 Email sẽ gửi THẬT đến Gmail!
🎉 Không còn log console nữa!
