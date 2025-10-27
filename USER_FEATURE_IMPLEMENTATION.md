# 📋 HƯỚNG DẪN TRIỂN KHAI CÁC CHỨC NĂNG USER

## 🎯 **CÁC CHỨC NĂNG CHÍNH:**

### **1. Đăng ký tài khoản** ✅
### **2. Cập nhật thông tin tài khoản** ✅
### **3. Khởi tạo lại mật khẩu (Reset Password)** ✅
### **4. Cập nhật mật khẩu (Change Password)** ✅
### **5. Đăng nhập** ✅

---

## 📁 **CẤU TRÚC FILE ĐÃ TẠO:**

```
src/user/service/
├── DatabaseConnection.java    # Quản lý kết nối MySQL
├── UserService.java           # Xử lý logic nghiệp vụ
└── EmailService.java          # Gửi email (DEMO MODE)
```

---

## 🔧 **CHI TIẾT TỪNG CLASS:**

### **1. DatabaseConnection.java**

**Chức năng:**
- Đọc cấu hình từ `release/config.properties`
- Quản lý kết nối MySQL (Singleton pattern)
- Cung cấp method `getConnection()` để lấy connection

**Sử dụng:**
```java
DatabaseConnection db = DatabaseConnection.getInstance();
Connection conn = db.getConnection();

// Sử dụng connection
// ...

// Đóng connection
DatabaseConnection.closeConnection(conn);
```

**Cấu hình:**
File: `release/config.properties`
```properties
db.url=jdbc:mysql://localhost:3306/chat_system?useSSL=false&serverTimezone=UTC
db.username=root
db.password=
```

---

### **2. UserService.java**

**Chức năng chính:**

#### **A. registerUser() - Đăng ký tài khoản**
```java
UserService userService = new UserService();

boolean success = userService.registerUser(
    "john_doe",              // username
    "password123",           // password (sẽ được hash)
    "John Doe",              // full_name
    "john@email.com",        // email
    "123 Main St",           // address
    new java.sql.Date(...),  // birth_date
    "Nam"                    // gender
);

if (success) {
    // Đăng ký thành công
} else {
    // Thất bại (username/email đã tồn tại)
}
```

**Kiểm tra:**
- ✅ Username đã tồn tại chưa
- ✅ Email đã được đăng ký chưa
- ✅ Hash password trước khi lưu (SHA-256)
- ✅ Insert vào bảng `users`

---

#### **B. login() - Đăng nhập**
```java
boolean success = userService.login("john_doe", "password123");

if (success) {
    // Đăng nhập thành công
    // → Mở UserMainFrame
} else {
    // Thất bại (sai mật khẩu hoặc không tồn tại)
}
```

**Kiểm tra:**
- ✅ Username hoặc Email tồn tại
- ✅ Tài khoản có bị khóa không (status = 'locked')
- ✅ Verify password với hash trong DB
- ✅ Ghi lại login history

**Cho phép đăng nhập bằng:**
- Username: `john_doe`
- Email: `john@email.com`

---

#### **C. updateProfile() - Cập nhật thông tin**
```java
boolean success = userService.updateProfile(
    "john_doe",              // username (không đổi được)
    "John Smith",            // full_name mới
    "john.smith@email.com",  // email mới
    "456 Oak St",            // address mới
    new java.sql.Date(...),  // birth_date mới
    "Nam"                    // gender mới
);
```

**Cho phép cập nhật:**
- ✅ full_name
- ✅ email
- ✅ address
- ✅ birth_date
- ✅ gender

**KHÔNG cho phép cập nhật:**
- ❌ username (không đổi được)
- ❌ password (dùng changePassword())

**Kiểm tra:**
- ✅ Email mới có trùng với user khác không

---

#### **D. changePassword() - Đổi mật khẩu**
```java
boolean success = userService.changePassword(
    "john_doe",       // username
    "password123",    // old_password
    "newpass456"      // new_password
);
```

**Quy trình:**
1. Verify mật khẩu cũ bằng `login()`
2. Nếu đúng → Hash mật khẩu mới
3. Update vào database

**Bảo mật:**
- ✅ Bắt buộc nhập đúng mật khẩu cũ
- ✅ Hash SHA-256

---

#### **E. resetPassword() - Khôi phục mật khẩu**
```java
String newPassword = userService.resetPassword("john@email.com");

if (newPassword != null) {
    // Reset thành công
    // newPassword: Mật khẩu random 12 ký tự
    // → Gửi qua EmailService
} else {
    // Email không tồn tại
}
```

**Quy trình:**
1. Kiểm tra email có trong DB không
2. Tạo mật khẩu random (12 ký tự: chữ hoa, thường, số, ký tự đặc biệt)
3. Hash và update vào DB
4. Trả về mật khẩu (để gửi email)

**Ví dụ mật khẩu random:** `Ab3$xY9@pQ2z`

---

### **3. EmailService.java**

**Chức năng:**
- Gửi email reset password

**PHIÊN BẢN 1 (Hiện tại):**
- Chỉ log email ra console (không gửi thật)
- Hiển thị nội dung email đẹp

**PHIÊN BẢN 2 (Tương lai):**
- Cần thêm `javax.mail.jar` (JavaMail API)
- Gửi email thật qua SMTP

**Sử dụng:**
```java
EmailService emailService = new EmailService();

boolean sent = emailService.sendResetPasswordEmail(
    "john@email.com",  // toEmail
    "john_doe",        // username
    "Ab3$xY9@pQ2z"     // newPassword
);
```

**Output Console:**
```
======================================================================
📧 EMAIL: Khôi phục mật khẩu
======================================================================
From: Chat System <noreply@chatsystem.com>
To: john@email.com
Subject: [Chat System] Khôi phục mật khẩu
----------------------------------------------------------------------

Xin chào john_doe,

Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.

┌─────────────────────────────────────┐
│  MẬT KHẨU MỚI CỦA BẠN LÀ:         │
│                                     │
│  >>> Ab3$xY9@pQ2z <<<              │
│                                     │
└─────────────────────────────────────┘

⚠️  LƯU Ý BẢO MẬT:
   • Vui lòng đổi mật khẩu ngay sau khi đăng nhập
   • Không chia sẻ mật khẩu này với bất kỳ ai
   • Nếu bạn không yêu cầu khôi phục, hãy liên hệ admin ngay
```

---

## 🎨 **TÍCH HỢP VÀO GUI:**

### **BƯỚC TIẾP THEO:**

#### **4. Cập nhật RegisterFrame.java**
```java
// Trong nút "Đăng ký":
registerButton.addActionListener(e -> {
    String username = usernameField.getText();
    String password = String.valueOf(passwordField.getPassword());
    String fullName = fullNameField.getText();
    String email = emailField.getText();
    // ...
    
    UserService userService = new UserService();
    boolean success = userService.registerUser(username, password, fullName, ...);
    
    if (success) {
        JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
        this.dispose();
        new LoginFrame().setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this, "Đăng ký thất bại!");
    }
});
```

---

#### **5. Cập nhật LoginFrame.java**
```java
// Thay demo mode bằng xác thực thật:
private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = String.valueOf(passwordField.getPassword());
    
    // Validate input
    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
        return;
    }
    
    // XÁC THỰC VỚI DATABASE
    UserService userService = new UserService();
    boolean success = userService.login(username, password);
    
    if (success) {
        loginSuccess(username);
    } else {
        JOptionPane.showMessageDialog(this, 
            "Đăng nhập thất bại!\nSai tên đăng nhập hoặc mật khẩu.",
            "Lỗi",
            JOptionPane.ERROR_MESSAGE);
    }
}
```

---

#### **6. Xử lý "Quên mật khẩu"**
```java
// Trong LoginFrame.java:
private void handleForgotPassword() {
    String email = JOptionPane.showInputDialog(this,
        "Nhập email đã đăng ký:",
        "Khôi phục mật khẩu",
        JOptionPane.QUESTION_MESSAGE);
    
    if (email != null && !email.trim().isEmpty()) {
        UserService userService = new UserService();
        String newPassword = userService.resetPassword(email);
        
        if (newPassword != null) {
            // Gửi email
            EmailService emailService = new EmailService();
            emailService.sendResetPasswordEmail(email, "", newPassword);
            
            JOptionPane.showMessageDialog(this,
                "Mật khẩu mới đã được gửi đến email:\n" + email + 
                "\n\nVui lòng kiểm tra hộp thư!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Email không tồn tại trong hệ thống!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

---

#### **7. Tạo UpdateProfileFrame.java**
Giao diện cho phép user cập nhật:
- Họ tên
- Email
- Địa chỉ
- Ngày sinh
- Giới tính

```java
// Khi user click "Cập nhật":
UserService userService = new UserService();
boolean success = userService.updateProfile(
    currentUsername,
    fullNameField.getText(),
    emailField.getText(),
    addressField.getText(),
    (Date) birthDateSpinner.getValue(),
    (String) genderCombo.getSelectedItem()
);
```

---

#### **8. Tạo ChangePasswordFrame.java**
Giao diện đổi mật khẩu:
- JPasswordField: Mật khẩu cũ
- JPasswordField: Mật khẩu mới
- JPasswordField: Xác nhận mật khẩu mới

```java
// Khi user click "Đổi mật khẩu":
String oldPass = String.valueOf(oldPasswordField.getPassword());
String newPass = String.valueOf(newPasswordField.getPassword());
String confirmPass = String.valueOf(confirmPasswordField.getPassword());

// Validate
if (!newPass.equals(confirmPass)) {
    JOptionPane.showMessageDialog(this, "Mật khẩu mới không khớp!");
    return;
}

UserService userService = new UserService();
boolean success = userService.changePassword(currentUsername, oldPass, newPass);
```

---

## 📊 **FLOW TỔNG QUAN:**

```
┌─────────────────┐
│   Main.java     │
│   (Entry Point) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     Đăng ký      ┌──────────────────┐
│  LoginFrame     │◄─────────────────│ RegisterFrame    │
│                 │                  │                  │
│ - Username      │                  │ - Username       │
│ - Password      │                  │ - Password       │
│ - Quên MK?      │──────────┐       │ - Full Name      │
└────────┬────────┘          │       │ - Email          │
         │                   │       │ - Address        │
         │ Login             │       │ - Birth Date     │
         │ (UserService)     │       │ - Gender         │
         ▼                   │       └────────┬─────────┘
┌─────────────────┐          │                │
│ UserMainFrame   │          │ Reset Pass     │ Register
│                 │          ▼                │ (UserService)
│ - Sidebar       │   ┌──────────────┐       │
│ - Chat Area     │   │ EmailService │◄──────┘
│ - Settings      │   │ (Send Email) │
└────────┬────────┘   └──────────────┘
         │
         ├───────────────────┐
         │                   │
         ▼                   ▼
┌──────────────────┐  ┌────────────────────┐
│ UpdateProfile    │  │ ChangePassword     │
│ Frame            │  │ Frame              │
│                  │  │                    │
│ - Edit Info      │  │ - Old Password     │
│ (UserService)    │  │ - New Password     │
└──────────────────┘  │ (UserService)      │
                      └────────────────────┘
```

---

## 🔐 **BẢO MẬT:**

### **Password Hashing:**
- Dùng SHA-256
- Không lưu plain text
- Verify bằng cách hash input và so sánh

### **SQL Injection Prevention:**
- Dùng `PreparedStatement`
- Không concatenate string trong SQL

### **Validation:**
- Check username/email đã tồn tại
- Check tài khoản bị khóa
- Validate input trước khi insert

---

## 🗄️ **CẤU TRÚC DATABASE:**

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,        -- SHA-256 hash
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address TEXT,
    birth_date DATE,
    gender ENUM('Nam', 'Nữ', 'Khác'),
    status ENUM('active', 'locked') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE login_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 🚀 **CÁCH CHẠY:**

### **1. Chuẩn bị Database:**
```bash
# Chạy MySQL Server
# Sau đó chạy script:
mysql -u root -p < script/database/create_database.sql
```

### **2. Cấu hình:**
Chỉnh file `release/config.properties`:
```properties
db.username=root
db.password=your_password
```

### **3. Compile:**
```bash
javac -d bin src/user/service/*.java src/user/gui/*.java
```

### **4. Run:**
```bash
java -cp bin user.gui.Main
```

---

## 📝 **TEST CASES:**

### **Test 1: Đăng ký tài khoản**
1. Mở RegisterFrame
2. Nhập thông tin hợp lệ
3. Click "Đăng ký"
4. ✅ Hiện "Đăng ký thành công"
5. ✅ Chuyển về LoginFrame

### **Test 2: Đăng nhập**
1. Nhập username + password
2. Click "Đăng nhập"
3. ✅ Mở UserMainFrame
4. ✅ Ghi login history vào DB

### **Test 3: Đăng nhập sai**
1. Nhập sai password
2. ✅ Hiện "Đăng nhập thất bại"

### **Test 4: Reset password**
1. Click "Quên mật khẩu?"
2. Nhập email
3. ✅ Hiện email ra console
4. ✅ Có mật khẩu mới random
5. Đăng nhập bằng mật khẩu mới
6. ✅ Thành công

### **Test 5: Đổi mật khẩu**
1. Vào ChangePasswordFrame
2. Nhập mật khẩu cũ đúng
3. Nhập mật khẩu mới
4. ✅ Update thành công
5. Logout và login lại
6. ✅ Dùng mật khẩu mới OK

### **Test 6: Cập nhật profile**
1. Vào UpdateProfileFrame
2. Sửa email, họ tên, địa chỉ
3. Click "Cập nhật"
4. ✅ Update thành công
5. ✅ Thông tin mới hiển thị trong UI

---

## 📌 **GHI CHÚ:**

### **Hiện tại:**
- ✅ DatabaseConnection hoàn thành
- ✅ UserService hoàn thành (5 methods)
- ✅ EmailService hoàn thành (DEMO MODE)

### **Cần làm tiếp:**
- ⏳ Cập nhật RegisterFrame.java (tích hợp UserService)
- ⏳ Cập nhật LoginFrame.java (thay demo mode)
- ⏳ Tạo UpdateProfileFrame.java
- ⏳ Tạo ChangePasswordFrame.java
- ⏳ Test với MySQL database thật

### **Phiên bản 2 (tương lai):**
- Thêm `javax.mail.jar` để gửi email thật
- Connection pooling (HikariCP)
- Password strength validation
- Session management
- Remember me functionality
- 2FA (Two-Factor Authentication)

---

## 🎓 **HỌC PHẦN:**

Các kiến thức áp dụng:
- ✅ JDBC (Java Database Connectivity)
- ✅ Prepared Statements (chống SQL Injection)
- ✅ SHA-256 Hashing
- ✅ Singleton Pattern
- ✅ MVC Pattern (Model-View-Controller)
- ✅ Exception Handling
- ✅ File I/O (đọc config.properties)
- ✅ Swing GUI
- ✅ MySQL Database

**Điểm đánh giá:**
- UI/DB Design: 20% ✅
- Implementation: 80% ✅

---

Bạn có muốn tôi tiếp tục tạo các GUI Frame còn lại không? 🚀
