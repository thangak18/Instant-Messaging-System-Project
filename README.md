# Hệ Thống Chat - Đồ án cuối kỳ

## Mô tả dự án
Hệ thống chat gồm 2 phân hệ:
- **Phân hệ quản trị**: Quản lý người dùng, nhóm chat, báo cáo spam, thống kê
- **Phân hệ người dùng**: Chat, quản lý bạn bè, nhóm chat

## Cấu trúc dự án

### Phiên bản 1 (20%)
```
├── src/                    # Mã nguồn các màn hình (chỉ giao diện)
│   ├── admin/             # Phân hệ quản trị
│   └── user/              # Phân hệ người dùng
├── script/                # Script tạo cơ sở dữ liệu
├── git/                   # Hình ảnh commit của từng thành viên
└── bao_cao_phien_ban_1.pdf # Báo cáo phiên bản 1
```

### Phiên bản 2 (80%)
```
├── src/                    # Mã nguồn chạy được của 2 phân hệ
├── release/               # File jar và cấu hình
│   ├── admin-app.jar      # Ứng dụng quản trị
│   ├── user-app.jar       # Ứng dụng người dùng
│   └── config.properties  # Cấu hình kết nối database
├── script/                # Script tạo cơ sở dữ liệu
├── git/                   # Hình ảnh commit của từng thành viên
└── bao_cao_phien_ban_2.pdf # Báo cáo phiên bản 2
```

## Yêu cầu kỹ thuật
- Java
- Database (MySQL/PostgreSQL)
- Git để quản lý mã nguồn
- Giao diện Swing/JavaFX

## Hướng dẫn sử dụng
Xem file `HUONG_DAN_SU_DUNG.md` để biết cách chạy ứng dụng.
# Instant-Messaging-System-Project
