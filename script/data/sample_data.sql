-- Script chèn dữ liệu mẫu cho hệ thống chat
USE chat_system;

-- Chèn dữ liệu người dùng mẫu
INSERT INTO users (username, password, full_name, email, address, birth_date, gender, status) VALUES
('admin', 'admin123', 'Quản trị viên', 'admin@chat.com', 'Hà Nội', '1990-01-01', 'Nam', 'active'),
('user1', 'user123', 'Nguyễn Văn A', 'user1@email.com', 'TP.HCM', '1995-05-15', 'Nam', 'active'),
('user2', 'user123', 'Trần Thị B', 'user2@email.com', 'Đà Nẵng', '1998-03-20', 'Nữ', 'active'),
('user3', 'user123', 'Lê Văn C', 'user3@email.com', 'Hải Phòng', '1992-12-10', 'Nam', 'active'),
('user4', 'user123', 'Phạm Thị D', 'user4@email.com', 'Cần Thơ', '1996-08-25', 'Nữ', 'active'),
('user5', 'user123', 'Hoàng Văn E', 'user5@email.com', 'Nha Trang', '1994-11-30', 'Nam', 'locked');

-- Chèn lịch sử đăng nhập mẫu
INSERT INTO login_history (user_id, login_time, ip_address) VALUES
(1, '2024-01-01 08:00:00', '192.168.1.1'),
(2, '2024-01-01 09:00:00', '192.168.1.2'),
(3, '2024-01-01 10:00:00', '192.168.1.3'),
(1, '2024-01-02 08:30:00', '192.168.1.1'),
(2, '2024-01-02 09:15:00', '192.168.1.2'),
(4, '2024-01-02 11:00:00', '192.168.1.4'),
(5, '2024-01-02 14:00:00', '192.168.1.5');

-- Chèn dữ liệu bạn bè mẫu
INSERT INTO friendships (user1_id, user2_id, status) VALUES
(2, 3, 'accepted'),
(2, 4, 'accepted'),
(3, 4, 'accepted'),
(2, 5, 'pending'),
(3, 5, 'accepted'),
(4, 5, 'blocked');

-- Chèn nhóm chat mẫu
INSERT INTO chat_groups (group_name, description, created_by) VALUES
('Nhóm bạn thân', 'Nhóm chat của những người bạn thân', 2),
('Công việc', 'Nhóm chat về công việc', 3),
('Học tập', 'Nhóm chat về học tập', 4);

-- Chèn thành viên nhóm mẫu
INSERT INTO group_members (group_id, user_id, role) VALUES
(1, 2, 'admin'),
(1, 3, 'member'),
(1, 4, 'member'),
(2, 3, 'admin'),
(2, 4, 'member'),
(2, 5, 'member'),
(3, 4, 'admin'),
(3, 2, 'member'),
(3, 3, 'member');

-- Chèn tin nhắn riêng mẫu
INSERT INTO private_messages (sender_id, receiver_id, message, sent_at) VALUES
(2, 3, 'Chào bạn!', '2024-01-01 10:00:00'),
(3, 2, 'Chào bạn! Bạn khỏe không?', '2024-01-01 10:01:00'),
(2, 3, 'Mình khỏe, cảm ơn bạn!', '2024-01-01 10:02:00'),
(2, 4, 'Hôm nay thế nào?', '2024-01-01 11:00:00'),
(4, 2, 'Tốt lắm, cảm ơn bạn!', '2024-01-01 11:01:00');

-- Chèn tin nhắn nhóm mẫu
INSERT INTO group_messages (group_id, sender_id, message, sent_at) VALUES
(1, 2, 'Chào mọi người!', '2024-01-01 12:00:00'),
(1, 3, 'Chào bạn!', '2024-01-01 12:01:00'),
(1, 4, 'Chào cả nhóm!', '2024-01-01 12:02:00'),
(2, 3, 'Hôm nay có cuộc họp không?', '2024-01-01 13:00:00'),
(2, 4, 'Có, 2h chiều', '2024-01-01 13:01:00');

-- Chèn báo cáo spam mẫu
INSERT INTO spam_reports (reporter_id, reported_user_id, reason, status) VALUES
(2, 5, 'Gửi tin nhắn spam', 'pending'),
(3, 5, 'Quấy rối', 'resolved'),
(4, 5, 'Nội dung không phù hợp', 'dismissed');

-- Chèn hoạt động người dùng mẫu
INSERT INTO user_activities (user_id, activity_type, activity_data, created_at) VALUES
(2, 'login', '{"ip": "192.168.1.2"}', '2024-01-01 09:00:00'),
(2, 'chat', '{"with_user": 3, "message_count": 3}', '2024-01-01 10:00:00'),
(2, 'group_chat', '{"group_id": 1, "message_count": 1}', '2024-01-01 12:00:00'),
(3, 'login', '{"ip": "192.168.1.3"}', '2024-01-01 10:00:00'),
(3, 'chat', '{"with_user": 2, "message_count": 2}', '2024-01-01 10:00:00'),
(3, 'friend_request', '{"to_user": 5}', '2024-01-01 15:00:00');
