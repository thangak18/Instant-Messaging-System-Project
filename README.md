# 🚀 Hệ Thống Nhắn Tin Thời Gian Thực (Instant Messaging System)
**Một hệ thống ứng dụng Chat Desktop phong cách Zalo - Tích hợp Mã hóa Đầu cuối (E2E Encryption), Gợi ý Tin nhắn bằng AI (Google Gemini) & Quản trị Toàn diện.**

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Socket](https://img.shields.io/badge/Networking-TCP%2FIP%20Socket-007396?style=for-the-badge&logo=java&logoColor=white)
![Security](https://img.shields.io/badge/Security-AES--256--GCM%20E2E-47A248?style=for-the-badge&logo=letsencrypt&logoColor=white)
![AI](https://img.shields.io/badge/AI-Google%20Gemini%202.0-8E75B2?style=for-the-badge&logo=google&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20Supabase-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20DAO%20Pattern-323330?style=for-the-badge)

---

## 🌟 1. Tổng quan dự án (Executive Summary)

**Instant Messaging System** là đồ án phần mềm cấp độ doanh nghiệp (Enterprise-grade Desktop Application) được thiết kế và xây dựng dựa trên kiến trúc **Client - Server đa luồng (Multi-threaded TCP/IP Socket)**. Dự án lấy cảm hứng từ trải nghiệm người dùng (UX/UI) hiện đại của **Zalo**, kết hợp các công nghệ kỹ thuật nâng cao bao gồm:

- **🔐 Mã hóa đầu cuối thực sự (True End-to-End Encryption - AES-256-GCM)**: Đảm bảo quyền riêng tư tuyệt đối cho các nhóm chat; máy chủ chỉ đóng vai trò trung chuyển dữ liệu mã hóa mà không thể giải mã nội dung.
- **🤖 Trợ lý AI thông minh (AI Smart Reply)**: Tích hợp **Google Gemini API** hỗ trợ gợi ý soạn thảo tin nhắn nhanh theo ngữ cảnh cuộc trò chuyện, kèm cơ chế Fallback thông minh khi offline.
- **📊 Hệ thống Quản trị & Thống kê (Admin Dashboard & Analytics)**: Theo dõi lượng người dùng hoạt động (Active Users), tăng trưởng tài khoản mới, kiểm soát spam và giám sát hiệu năng hệ thống thời gian thực.
- **☁️ Linh hoạt Lưu trữ (Dual Database Support)**: Hỗ trợ triển khai trên cả **MySQL/PostgreSQL truyền thống** và **Supabase Cloud (REST API)**.

---

## 💡 2. Điểm nhấn kỹ thuật dành cho Nhà tuyển dụng (Engineering Highlights)

Dự án thể hiện năng lực giải quyết các bài toán phức tạp trong phát triển phần mềm (Software Engineering):

| Năng lực kỹ thuật | Cách tiếp cận & Giải pháp trong dự án |
| :--- | :--- |
| **Low-level Networking & Real-time Communication** | Xây dựng giao thức giao tiếp tùy chỉnh trên nền **TCP/IP Socket**, quản lý luồng dữ liệu song song với `ChatServer` và `ClientHandler`, đảm bảo truyền tải tin nhắn tức thì không gây nghẽn cổ chai. |
| **Zero-Knowledge E2E Security Architecture** | Áp dụng chuẩn **AES-256-GCM**. Khóa nhóm (Group Key) được sinh ngẫu nhiên tại máy khách (Client) và lưu trữ trong Cache cục bộ/Keychain. **Máy chủ không giữ khóa**, loại bỏ hoàn toàn rủi ro rò rỉ dữ liệu từ phía Server. |
| **Bất đồng bộ & Non-blocking UI (Swing/AWT)** | Tách biệt hoàn toàn luồng xử lý I/O mạng và giao diện người dùng bằng cách ứng dụng `SwingUtilities.invokeLater()` và Background Workers, giữ cho giao diện luôn mượt mà (Zalo Z-UI). |
| **Khả năng phục hồi hệ thống (Resilience & Fallback)** | Khi kết nối đến Google Gemini API gặp sự cố hoặc offline, `AIService` tự động chuyển sang cơ chế **Rule-based Offline Suggestion**, bảo đảm trải nghiệm người dùng không bị gián đoạn. |
| **Tối ưu hóa & Giám sát Cơ sở dữ liệu** | Xây dựng bộ script chẩn đoán chuyên sâu (`performance_test.sql`, `monitoring.sql`, `optimize_database.sql`), thiết lập Index tối ưu cho truy vấn lịch sử tin nhắn lớn và phân trang. |

---

## ✨ 3. Tính năng nổi bật (Key Features)

### 🧑‍💻 Phân hệ Người dùng (User Subsystem)
- **🎨 Giao diện phong cách Zalo (Zalo Modern UI)**: Thiết kế tinh tế với Bubble Chat tùy chỉnh, hiệu ứng Hover, phân chia danh sách hội thoại, Sidebar điều hướng trực quan.
- **💬 Nhắn tin Thời gian thực**: Hỗ trợ Chat riêng tư (1-1) và Chat nhóm (Group Chat) đa thành viên với tốc độ phản hồi tức thì.
- **🗑️ Xóa & Thu hồi tin nhắn (Zalo-style Message Deletion)**:
  - **Xóa chỉ mình tôi (Soft Delete)**: Ẩn tin nhắn trên thiết bị cục bộ, không ảnh hưởng đến đối phương.
  - **Thu hồi tin nhắn (Hard Delete)**: Xóa vĩnh viễn tin nhắn ở cả 2 phía (chỉ áp dụng cho tin nhắn do chính mình gửi).
- **🤖 Gợi ý tin nhắn bằng AI**: Trợ lý AI đọc ngữ cảnh hội thoại và đề xuất các câu trả lời tự nhiên, lịch sự bằng tiếng Việt (tích hợp Google Gemini 2.0/1.5 Flash).
- **🔒 Nhóm chat Mã hóa E2E**: Tạo và tham gia các phòng chat bảo mật tuyệt đối với khóa mã hóa AES-256-GCM.
- **👥 Quản lý Bạn bè & Liên lạc**: Gửi/nhận yêu cầu kết bạn, tìm kiếm người dùng, chặn/bỏ chặn tài khoản phiền toái (`BlockedUsersDialog`).
- **📧 Bảo mật & Xác thực Tài khoản**: Đăng ký, đăng nhập an toàn với mật khẩu băm **SHA-256 + Salt**, hỗ trợ xác thực email và khôi phục mật khẩu qua **JavaMail API**.

### 🛡️ Phân hệ Quản trị viên (Admin Subsystem)
- **👥 Quản lý Người dùng**: Xem danh sách chi tiết, thêm mới, chỉnh sửa thông tin, khóa/mở khóa tài khoản vi phạm, theo dõi lịch sử đăng nhập (`LoginHistoryPanel`).
- **🏢 Quản lý Nhóm chat**: Giám sát các nhóm chat trong hệ thống, danh sách thành viên, hỗ trợ cơ chế đa quản trị viên (Multi-Admin).
- **🚨 Kiểm soát Spam & Vi phạm**: Tiếp nhận và xử lý các báo cáo spam từ người dùng (`SpamReportPanel`), duy trì môi trường giao tiếp lành mạnh.
- **📈 Thống kê & Biểu đồ Tăng trưởng**: Trực quan hóa số liệu qua các biểu đồ động: Lượng người dùng hoạt động (`ActiveUserChartPanel`), báo cáo tài khoản mới (`NewUserReportPanel`), thống kê kết bạn (`StatisticsPanel`).

---

## 🏗️ 4. Kiến trúc hệ thống & Công nghệ (Architecture & Tech Stack)

```
[ Client App (Swing/Zalo UI) ]  <--- (TCP/IP Socket + E2E AES-256) --->  [ Chat Server (Multi-threaded) ]
       │              │                                                        │
       │ (REST API)   │ (SMTP)                                                 │ (JDBC / REST)
       ▼              ▼                                                        ▼
[ Google Gemini AI ] [ JavaMail ]                                    [ MySQL / Supabase Cloud DB ]
```

- **Ngôn ngữ cốt lõi**: Java 8+ (Core Java, Object-Oriented Programming).
- **Giao diện người dùng (GUI)**: Java Swing / AWT, Custom Component Architecture (Zalo UI Design System).
- **Mạng & Giao tiếp (Networking)**: Java Socket Programming, ServerSocket, Multi-threading, Custom Wire Protocol.
- **Bảo mật & Mã hóa (Security)**: `javax.crypto` (AES-256-GCM), `java.security` (SHA-256 + Random Salt), PreparedStatement (Anti-SQL Injection).
- **Cơ sở dữ liệu (Database)**: MySQL 5.7+/8.0+, PostgreSQL, Supabase Cloud DB, thiết kế theo mô hình **DAO (Data Access Object)**.
- **Thư viện & Tích hợp ngoại vi**: 
  - Google Gemini Generative AI API (REST/JSON).
  - JavaMail API (`javax.mail`) cho Email OTP & Notification.
  - PostgreSQL & MySQL JDBC Drivers.

---

## 📁 5. Cấu trúc thư mục (Project Structure)

```text
Instant-Messaging-System-Project/
├── src/
│   ├── admin/               # Phân hệ Quản trị viên (Admin Dashboard)
│   │   ├── gui/             # Giao diện Admin: User/Group Management, Reports, Charts
│   │   └── service/         # Các lớp DAO & Dịch vụ thống kê, xử lý Spam, Supabase REST
│   └── user/                # Phân hệ Người dùng (Client Chat App)
│   │   ├── gui/             # Giao diện Zalo UI: ChatContent, GroupChat, FriendList, Settings
│   │   ├── service/         # Dịch vụ cốt lõi: AIService (Gemini), EncryptionService (E2E), UserService
│   │   └── socket/          # Xử lý mạng: ChatServer, ClientHandler, SocketClient, Message Protocol
├── script/database/         # Bộ script SQL: Schema, E2E setup, Message Deletion, Performance Test
├── release/                 # File thực thi (.jar) và cấu hình kết nối (config.properties)
├── lib/                     # Các thư viện phụ thuộc (PostgreSQL JDBC, JavaMail, Activation)
├── run_*.sh                 # Script chạy nhanh ứng dụng cho Linux/macOS/Git Bash
└── HUONG_DAN_*.md           # Tài liệu hướng dẫn sử dụng và cấu hình chi tiết
```

---

## 🚀 6. Hướng dẫn Cài đặt & Khởi chạy (Quick Start)

### Yêu cầu hệ thống (Prerequisites)
- **Java Development Kit (JDK)**: Phiên bản 8 hoặc cao hơn.
- **Cơ sở dữ liệu**: MySQL Server (5.7+/8.0+) hoặc tài khoản Supabase PostgreSQL.

### Bước 1: Chuẩn bị Cơ sở dữ liệu
```bash
# Di chuyển vào thư mục script database
cd script/database

# Chạy script tạo bảng và dữ liệu mẫu (MySQL)
mysql -u root -p < create_database.sql
mysql -u root -p < ../data/sample_data.sql

# (Tùy chọn) Cập nhật các tính năng nâng cao (Mã hóa E2E, Xóa tin nhắn, Chặn người dùng)
mysql -u root -p < add_group_encryption.sql
mysql -u root -p < add_message_deletion.sql
mysql -u root -p < blocked_users_setup.sql
```

### Bước 2: Cấu hình hệ thống
Chỉnh sửa file `release/config.properties` với thông tin database và API Key của bạn:
```properties
# Cấu hình Database
db.host=localhost
db.port=3306
db.name=chat_system
db.username=root
db.password=your_db_password

# Cấu hình Google Gemini AI (Để sử dụng tính năng gợi ý tin nhắn thông minh)
gemini.api.key=YOUR_GEMINI_API_KEY
```

### Bước 3: Khởi chạy ứng dụng

**Cách 1: Sử dụng Script tự động (Khuyến nghị cho Linux/macOS/Git Bash on Windows)**
```bash
# 1. Cấp quyền thực thi cho script
chmod +x run_*.sh

# 2. Khởi động Máy chủ Chat (Chat Server)
./run_server.sh

# 3. Khởi động Ứng dụng Người dùng (Client App)
./run_user.sh

# 4. Khởi động Ứng dụng Quản trị viên (Admin Dashboard)
./run_admin.sh
```

**Cách 2: Khởi chạy trực tiếp bằng file JAR hoặc Java Command**
```bash
# 1. Khởi động Server
java -jar release/chat-server.jar

# 2. Khởi động Client Người dùng
java -jar release/user-app.jar

# 3. Khởi động Admin Dashboard
java -jar release/admin-app.jar
```

---

## 🔑 7. Tài khoản Trải nghiệm Mẫu (Sample Accounts)

Để thuận tiện cho quá trình đánh giá và thử nghiệm tính năng, bạn có thể sử dụng các tài khoản đã được thiết lập sẵn trong dữ liệu mẫu:

| Phân hệ | Tên đăng nhập (Username) | Mật khẩu (Password) | Vai trò / Ghi chú |
| :--- | :--- | :--- | :--- |
| **Quản trị viên** | `admin` | `admin123` | Toàn quyền quản lý User, Nhóm, Thống kê, Spam |
| **Người dùng** | `user1` | `user123` | Tài khoản trải nghiệm Chat, E2E, AI Suggestion |
| **Người dùng** | `user2` | `user123` | Tài khoản đối phương để test nhắn tin thời gian thực |
| **Người dùng** | `user3` / `user4` | `user123` | Tài khoản thành viên test Chat nhóm & Kết bạn |

---

## 📚 8. Tài liệu tham khảo thêm (Documentation)
- **[Hướng dẫn sử dụng chi tiết](file:///d:/Intern/Project%20Intern/Instant-Messaging-System-Project/HUONG_DAN_SU_DUNG.md)**: Chi tiết thao tác từng màn hình và xử lý sự cố.
- **[Hướng dẫn cấu hình Database](file:///d:/Intern/Project%20Intern/Instant-Messaging-System-Project/HUONG_DAN_CAU_HINH_DB.md)**: Hướng dẫn kết nối MySQL, PostgreSQL và Supabase Cloud.
- **[Đặc tả kỹ thuật: Tính năng xóa tin nhắn](file:///d:/Intern/Project%20Intern/Instant-Messaging-System-Project/MESSAGE_DELETION_FEATURE.md)**: Phân tích luồng dữ liệu Soft Delete vs Hard Delete.
- **[Báo cáo Phiên bản 1](file:///d:/Intern/Project%20Intern/Instant-Messaging-System-Project/README_PHIEN_BAN_1.md)**: Lịch sử phát triển và cấu trúc giao diện khởi tạo.

---
*Dự án được phát triển với sự chú trọng cao nhất về kiến trúc mã nguồn sạch (Clean Code), hiệu năng mạng thời gian thực và trải nghiệm người dùng hiện đại.*
