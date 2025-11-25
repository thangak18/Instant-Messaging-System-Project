# CHỨC NĂNG XÓA TIN NHẮN RIÊNG LẺ

## 📌 Tổng quan

Chức năng xóa tin nhắn riêng lẻ giống Zalo với 2 tùy chọn:
1. **Xóa chỉ mình tôi** (Soft Delete) - Tin nhắn bị ẩn chỉ ở phía bạn
2. **Thu hồi tin nhắn** (Hard Delete) - Tin nhắn bị xóa vĩnh viễn cho cả 2 người

## 🎯 Cách sử dụng

### Bước 1: Cài đặt Database
Chạy script SQL để tạo bảng `deleted_messages`:
```bash
script/database/add_message_deletion.sql
```

### Bước 2: Sử dụng trong Chat
1. Di chuột vào bất kỳ tin nhắn nào → Nút **⋯** xuất hiện ở góc phải
2. Click vào nút **⋯** để mở menu:
   - **Xóa chỉ mình tôi**: Ẩn tin nhắn khỏi thiết bị này
   - **Thu hồi tin nhắn**: Xóa vĩnh viễn (chỉ hiện với tin nhắn của bạn)

## 🔧 Cấu trúc kỹ thuật

### 1. Database Schema

#### Bảng `deleted_messages` (Soft Delete)
```sql
CREATE TABLE deleted_messages (
    message_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id)
);
```

**Cách hoạt động:**
- Khi user A xóa tin nhắn → Thêm `(message_id, user_id_A)` vào bảng
- User B vẫn thấy tin nhắn bình thường
- Khi load lịch sử → Loại bỏ tin nhắn có trong `deleted_messages`

### 2. Backend Methods

#### UserService.java

**deleteMessageForMe(messageId, username)**
- Thêm tin nhắn vào bảng `deleted_messages`
- Tin nhắn chỉ bị ẩn với user đó
- Return: `true` nếu thành công

**recallMessage(messageId, username)**
- Kiểm tra quyền: Chỉ người gửi mới thu hồi được
- Xóa hoàn toàn khỏi bảng `messages`
- Return: `true` nếu thành công

**getChatHistory(username1, username2)**
- Đã cập nhật để loại bỏ tin nhắn trong `deleted_messages`
- Query có điều kiện `NOT EXISTS` để filter

**saveMessage(sender, receiver, content)**
- Đã thay đổi return type từ `boolean` → `int`
- Return `message_id` để có thể xóa ngay sau khi gửi
- Dùng `RETURNING message_id` trong SQL

### 3. Frontend Components

#### ChatContentPanel.java

**addMessageBubble(messageId, content, isSent, time)**
- Tạo bubble với nút **⋯** ở góc phải
- Nút chỉ hiện khi hover vào bubble
- Click nút → Hiện menu popup

**showMessageMenu(menuButton, messageId, isSent, bubbleContainer)**
- Tạo popup menu với 2 options:
  1. **Xóa chỉ mình tôi** (luôn có)
  2. **Thu hồi tin nhắn** (chỉ với tin nhắn của mình, màu đỏ)

**addMessageBubble(content, isSent, time)** - Overload
- Dùng cho tin nhắn nhận real-time (chưa có messageId)
- Tạo bubble không có menu xóa
- Khi refresh chat → Tin nhắn sẽ có menu xóa

## 📊 Flow Chart

### Xóa chỉ mình tôi
```
User click "Xóa chỉ mình tôi"
    ↓
Confirm dialog
    ↓
UserService.deleteMessageForMe(messageId, username)
    ↓
INSERT INTO deleted_messages (message_id, user_id)
    ↓
Remove bubble from UI
    ↓
Success message
```

### Thu hồi tin nhắn
```
User click "Thu hồi tin nhắn"
    ↓
Confirm dialog (cảnh báo xóa vĩnh viễn)
    ↓
UserService.recallMessage(messageId, username)
    ↓
Check: Người gửi?
    ↓ YES
DELETE FROM messages WHERE message_id = ?
    ↓
Remove bubble from UI
    ↓
Success message
```

## 🎨 UI/UX

### Bubble Menu
- **Position**: Góc phải bubble
- **Trigger**: Hover vào bubble
- **Icon**: ⋯ (3 chấm dọc)
- **Color**: 
  - Sent bubble: rgba(230, 240, 255, 0.8)
  - Received bubble: rgba(120, 120, 120, 0.8)

### Popup Menu
- **Width**: Auto-fit
- **Border**: 1px solid #C8C8C8
- **Items**:
  1. "Xóa chỉ mình tôi" - Font 13px, màu đen
  2. Separator (nếu là tin nhắn của mình)
  3. "Thu hồi tin nhắn" - Font 13px, màu đỏ (#DC3545)

### Confirm Dialogs

**Xóa chỉ mình tôi:**
```
Title: "Xóa tin nhắn?"
Message: "Tin nhắn sẽ bị xóa khỏi thiết bị này.
          Người khác vẫn có thể nhìn thấy tin nhắn."
Type: WARNING
```

**Thu hồi tin nhắn:**
```
Title: "Thu hồi tin nhắn?"
Message: "Tin nhắn sẽ bị xóa vĩnh viễn cho tất cả mọi người.
          Bạn có chắc chắn muốn thu hồi?"
Type: WARNING
```

## 🔒 Bảo mật

### Thu hồi tin nhắn
- **Kiểm tra quyền**: Chỉ người gửi mới được thu hồi
- **SQL injection**: Dùng PreparedStatement
- **Validation**: Kiểm tra messageId và username hợp lệ

### Xóa chỉ mình tôi
- **Privacy**: User khác không biết tin nhắn đã bị xóa
- **Data retention**: Tin nhắn vẫn tồn tại trong DB
- **Conflict handling**: `ON CONFLICT DO NOTHING` khi xóa 2 lần

## 📈 Performance

### Indexes
```sql
CREATE INDEX idx_deleted_messages_user ON deleted_messages(user_id);
CREATE INDEX idx_deleted_messages_message ON deleted_messages(message_id);
```

### Query Optimization
- Dùng `NOT EXISTS` thay vì `LEFT JOIN` để loại bỏ deleted messages
- Index trên `(message_id, user_id)` cho primary key lookup nhanh

## 🐛 Known Issues & Limitations

1. **Tin nhắn real-time chưa có menu xóa**
   - Tin nhắn vừa nhận không có nút ⋯
   - Phải refresh chat để có menu
   - Nguyên nhân: Chưa có messageId khi nhận qua socket

2. **Không sync thu hồi qua socket**
   - Khi thu hồi tin nhắn, người kia chỉ thấy biến mất sau khi refresh
   - Cần implement socket message type `RECALL_MESSAGE`

3. **Soft delete không có expiry**
   - Bảng `deleted_messages` có thể phình to
   - Nên có cleanup job xóa record cũ >30 ngày

## 🚀 Tính năng tương lai

1. **Real-time recall notification**
   - Socket message: `RECALL_MESSAGE`
   - Auto remove bubble khi nhận recall event

2. **Bulk delete**
   - Select nhiều tin nhắn để xóa cùng lúc
   - Checkbox mode trong chat

3. **Undo delete**
   - Snackbar "Đã xóa tin nhắn" với nút Undo
   - Restore trong 5 giây

4. **Delete history cleanup**
   - Cronjob xóa records >30 ngày trong `deleted_messages`
   - VACUUM table định kỳ

## 📝 Testing Checklist

- [ ] Xóa tin nhắn của mình (sent)
- [ ] Xóa tin nhắn của người khác (received)
- [ ] Thu hồi tin nhắn của mình
- [ ] Thử thu hồi tin nhắn của người khác (phải fail)
- [ ] Xóa 2 lần cùng 1 tin nhắn
- [ ] Load lại chat sau khi xóa
- [ ] Gửi tin nhắn mới sau khi xóa
- [ ] Hover vào bubble để hiện nút ⋯
- [ ] Menu popup hiện đúng vị trí
- [ ] Confirm dialog hiện đúng nội dung
- [ ] UI update ngay sau khi xóa

## 📚 Related Files

### Database
- `script/database/add_message_deletion.sql`

### Backend
- `src/user/service/UserService.java`
  - `deleteMessageForMe()`
  - `recallMessage()`
  - `getChatHistory()` (updated)
  - `saveMessage()` (updated return type)

### Frontend
- `src/user/gui/ChatContentPanel.java`
  - `addMessageBubble()` (2 overloads)
  - `showMessageMenu()`
  - `addMessageBubbleWithoutMenu()`
  - `sendMessage()` (updated)

### Documentation
- `HUONG_DAN_SU_DUNG.md` (cần cập nhật)
