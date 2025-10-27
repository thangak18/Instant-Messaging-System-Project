# Hướng Dẫn Xác Thực Email

## Vấn Đề: Kiểm Tra Email Có Thật Trên Gmail

Bạn muốn kiểm tra xem email có **thật sự tồn tại trên Gmail** không, chứ không chỉ kiểm tra format.

---

## Các Phương Pháp

### ❌ KHÔNG THỂ THỰC HIỆN TRỰC TIẾP
- Không thể kiểm tra trực tiếp email có tồn tại trên Gmail hay không
- Gmail không cung cấp API public để check email existence
- Đây là biện pháp bảo mật của Gmail (chống spam, phishing)

### ✅ GIẢI PHÁP 1: GỬI EMAIL XÁC THỰC (KHUYẾN NGHỊ)

**Cách hoạt động:**
1. User đăng ký với email
2. Hệ thống gửi email chứa link xác thực (verification token)
3. User phải click link trong email để kích hoạt tài khoản
4. Nếu email không tồn tại → không nhận được email → không kích hoạt được

**Ưu điểm:**
- ✅ Đảm bảo email thật 100%
- ✅ Ngăn spam registration
- ✅ Tiêu chuẩn của các website lớn (Facebook, Google, GitHub...)

**Nhược điểm:**
- Phức tạp hơn
- Cần gửi email thật qua SMTP

---

### ✅ GIẢI PHÁP 2: GỬI EMAIL THẬT QUA GMAIL SMTP

**Yêu cầu:**
1. **JavaMail API** - Thư viện gửi email
2. **Gmail App Password** - Mật khẩu ứng dụng Gmail
3. **Cấu hình SMTP** trong config.properties

**Các bước thiết lập:**

#### Bước 1: Download JavaMail API
```bash
# Download từ Maven Central hoặc
https://github.com/javaee/javamail/releases

# Giải nén vào thư mục lib/
lib/javax.mail.jar
lib/activation.jar
```

#### Bước 2: Tạo Gmail App Password
1. Truy cập: https://myaccount.google.com/security
2. Bật "2-Step Verification"
3. Tạo "App Password" cho "Mail"
4. Sao chép mật khẩu 16 ký tự (ví dụ: `abcd efgh ijkl mnop`)

#### Bước 3: Cập nhật config.properties
```properties
# Gmail SMTP Configuration
email.host=smtp.gmail.com
email.port=587
email.username=your-email@gmail.com
email.password=abcd efgh ijkl mnop
email.from=your-email@gmail.com
email.from.name=InstantChat System
```

#### Bước 4: Uncomment code trong EmailService.java
```java
// Phần code gửi email thật qua SMTP đã được comment
// Bạn cần uncomment và implement
```

---

### ✅ GIẢI PHÁP 3: DÙNG DỊCH VỤ BÊN THỨ 3

**SendGrid, Mailgun, AWS SES:**
- API đơn giản
- Miễn phí tối đa X email/tháng
- Quản lý email template
- Tracking (email delivered, opened, clicked)

---

## Hiện Trạng Hệ Thống

### ✅ ĐÃ HOÀN THÀNH

1. **Chức năng Quên Mật Khẩu:**
   - ✅ Nhập email → Kiểm tra email trong database
   - ✅ Tạo mật khẩu random (12 ký tự)
   - ✅ "Gửi" mật khẩu qua email (DEMO MODE - log ra console)
   - ✅ Mở ResetPasswordFrame để nhập mật khẩu mới
   - ✅ Cập nhật mật khẩu mới vào database

2. **Flow Hoàn Chỉnh:**
   ```
   LoginFrame 
   → Click "Quên mật khẩu?" 
   → Nhập email 
   → [Console hiển thị mật khẩu tạm] 
   → ResetPasswordFrame mở ra
   → Nhập: (1) Mật khẩu tạm, (2) Mật khẩu mới, (3) Xác nhận
   → Cập nhật database
   → Quay lại LoginFrame
   → Đăng nhập với mật khẩu mới
   ```

### 🚧 CẦN BỔ SUNG

1. **Gửi Email Thật:**
   - Cần JavaMail API
   - Cần Gmail App Password
   - Hoặc dùng SendGrid/Mailgun

2. **Email Verification Khi Đăng Ký:**
   - Tạo verification_token khi đăng ký
   - Gửi link kích hoạt đến email
   - User click link → Đánh dấu email_verified = true
   - Chỉ cho phép login nếu email đã verified

---

## Khuyến Nghị

### 🎯 PHIÊN BẢN DEMO (HIỆN TẠI)
✅ Sử dụng:
- Kiểm tra format email (regex)
- Kiểm tra email trong database
- Log mật khẩu ra console (không gửi email thật)

**Ưu điểm:** Đơn giản, test nhanh, không cần config phức tạp

### 🚀 PHIÊN BẢN PRODUCTION (TƯƠNG LAI)
✅ Nâng cấp:
1. **Gửi email thật** qua Gmail SMTP hoặc SendGrid
2. **Email verification** khi đăng ký (link kích hoạt)
3. **Email template** đẹp với HTML
4. **Rate limiting** (giới hạn số email gửi/phút)

---

## Code Mẫu: Gửi Email Thật Qua Gmail

```java
// EmailService.java - Phần implement thật (cần JavaMail)

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public boolean sendResetPasswordEmailReal(String toEmail, String fullName, String tempPassword) {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    
    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUsername, smtpPassword);
        }
    });
    
    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("[InstantChat] Khôi Phục Mật Khẩu");
        
        String htmlContent = "<html><body>" +
            "<h2>Xin chào " + fullName + ",</h2>" +
            "<p>Mật khẩu tạm thời của bạn là:</p>" +
            "<h1 style='color: #0084FF;'>" + tempPassword + "</h1>" +
            "<p>Vui lòng đổi mật khẩu sau khi đăng nhập.</p>" +
            "</body></html>";
        
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        Transport.send(message);
        return true;
        
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

---

## Testing

### Test Chức Năng Quên Mật Khẩu (Demo Mode)

1. **Chạy ứng dụng:**
   ```bash
   java -cp "bin;lib\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar" user.gui.Main
   ```

2. **Click "Quên mật khẩu?"**

3. **Nhập email đã đăng ký:**
   - `tmhung23@clc.fitus.edu.vn` (email của user `hung`)
   - Hoặc email khác đã đăng ký

4. **Kiểm tra console:**
   ```
   ===================================================================
   📧 EMAIL: Khôi phục mật khẩu
   ===================================================================
   To: tmhung23@clc.fitus.edu.vn
   Subject: [Chat System] Khôi phục mật khẩu
   
   Mật khẩu mới của bạn là: AbCd1234XyZ!
   ===================================================================
   ```

5. **Copy mật khẩu tạm** từ console

6. **Trong ResetPasswordFrame:**
   - Ô 1: Nhập mật khẩu tạm (từ console)
   - Ô 2: Nhập mật khẩu mới (ví dụ: `newpass123`)
   - Ô 3: Xác nhận mật khẩu mới (`newpass123`)
   - Click "CẬP NHẬT MẬT KHẨU"

7. **Đăng nhập với mật khẩu mới**

---

## Kết Luận

**Hiện tại:**
- ✅ Chức năng quên mật khẩu hoạt động hoàn chỉnh (demo mode)
- ✅ Kiểm tra email trong database
- ✅ Tạo và "gửi" mật khẩu random (log console)
- ✅ Cập nhật mật khẩu mới

**Để kiểm tra email thật trên Gmail:**
- Cần implement gửi email verification
- Cần JavaMail API + Gmail App Password
- Hoặc dùng SendGrid/Mailgun API

**Quyết định của bạn:**
1. Giữ nguyên demo mode (đủ để test và demo)
2. Nâng cấp lên gửi email thật (cần thêm setup)
3. Implement email verification khi đăng ký (recommended cho production)
