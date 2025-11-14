# ✅ FRIEND REQUEST SYSTEM - HOÀN THÀNH

## 🎯 Những gì đã fix:

### 1. **Layout Fix - ContactPanel**
- ❌ **Trước**: ContactPanel hiển thị nội dung bên trong (sai layout)
- ✅ **Sau**: ContactPanel chỉ có 4 menu items (350px width)
- ✅ Khi click menu → thay thế **ChatContentPanel bên phải** (main area)

**Cấu trúc mới:**
```
┌─────────┬────────────────┬──────────────────────────┐
│ Sidebar │  ContactPanel  │   Content Area (RIGHT)   │
│  (60px) │   (350px)      │   - Friend Requests      │
│         │  ┌──────────┐  │   - Friends List         │
│  👤     │  │👥 Bạn bè │  │   - Groups List          │
│  💬     │  │👨‍👩‍👧 Nhóm  │  │   - Group Invites        │
│  👥  ←  │  │👋 Lời mời│← │   (CardLayout switching) │
│  ⚙️     │  │📩 Mời nhóm│  │                          │
│         │  └──────────┘  │                          │
└─────────┴────────────────┴──────────────────────────┘
```

### 2. **Socket Error Fix**
- ❌ **Trước**: Hiện error dialog khi không kết nối được server (blocking UX)
- ✅ **Sau**: Chỉ log warning, vẫn cho phép dùng các features khác
```java
⚠️ Không thể kết nối đến chat server. Socket features sẽ bị tắt.
```

### 3. **Friend Request Features**
#### Backend (UserService.java):
- `getReceivedFriendRequests(username)` - Query pending requests WHERE friend_id = user
- `getSentFriendRequests(username)` - Query pending requests WHERE user_id = user
- `sendFriendRequest(sender, receiver)` - INSERT với status='pending'
- `acceptFriendRequest(friendshipId)` - UPDATE status='accepted'
- `rejectFriendRequest(friendshipId)` - DELETE request
- `recallFriendRequest(friendshipId)` - DELETE sent request
- `countReceivedFriendRequests(username)` - COUNT pending requests

#### Frontend Components:
**FriendRequestPanel.java:**
- Tab "Lời mời đã nhận": Avatar, tên, thời gian, "Đồng ý"/"Từ chối"
- Tab "Lời mời đã gửi": Avatar, tên, status, "Thu hồi lời mời"
- SwingWorker cho async database calls
- Confirmation dialogs cho destructive actions
- Auto-reload sau mỗi action

**ContactPanel.java:**
- 4 menu items với notification badges
- Click handler → `mainFrame.showContactContent(panelKey)`
- Clean separation: menu ở left, content ở right

**ZaloMainFrame.java:**
- CardLayout cho leftPanel (Chat/Contact)
- CardLayout cho rightPanel (ChatContent/Friends/Groups/FriendRequests/GroupInvites)
- Methods: `showChatPanel()`, `showContactPanel()`, `showContactContent(key)`

**AddFriendDialog.java:**
- Button "Kết bạn" gọi `userService.sendFriendRequest()`
- Lưu vào database với status='pending'
- TODO: Socket notification (chưa implement)

## 🧪 Cách test:

### Test 1: Gửi lời mời kết bạn
1. **User A**: Login → Click Add Friend icon
2. Search "Hung" → Click "Kết bạn"
3. ✅ Database: INSERT vào `friends` table
4. ✅ Button đổi thành "Đã gửi" (màu xanh)

### Test 2: Nhận lời mời kết bạn
1. **User B (Hung)**: Login → Click Contact icon (sidebar)
2. Click "👋 Lời mời kết bạn" (badge hiện số 5)
3. Tab "Lời mời đã nhận" → Xem danh sách
4. ✅ Click "Đồng ý" → UPDATE status='accepted'
5. ✅ Click "Từ chối" → DELETE request

### Test 3: Thu hồi lời mời đã gửi
1. **User A**: Click Contact icon → "Lời mời kết bạn"
2. Tab "Lời mời đã gửi" → Xem danh sách
3. ✅ Click "Thu hồi lời mời" → DELETE request
4. ✅ Request biến mất khỏi tab "Lời mời đã nhận" của User B

### Test 4: Switching panels
1. Click icon Chat (sidebar) → Show ChatListPanel + ChatContentPanel
2. Click icon Contact (sidebar) → Show ContactPanel + FriendRequestPanel
3. Trong ContactPanel, click:
   - "Danh sách bạn bè" → Show placeholder "Danh sách bạn bè"
   - "Lời mời kết bạn" → Show FriendRequestPanel
   - "Danh sách nhóm" → Show placeholder "Danh sách nhóm"

## 📊 Database Structure:
```sql
friends table:
- friendship_id (PK)
- user_id (FK → users) -- người GỬI
- friend_id (FK → users) -- người NHẬN
- status: 'pending' | 'accepted' | 'blocked'
- created_at
- updated_at

Queries:
-- Lời mời đã nhận
SELECT * FROM friends WHERE friend_id = ? AND status = 'pending'

-- Lời mời đã gửi
SELECT * FROM friends WHERE user_id = ? AND status = 'pending'
```

## 🚀 Chạy ứng dụng:

### Bước 1: Start ChatServer (optional)
```bash
cd D:\Instant-Messaging-System-Project
java -cp "bin;lib/*" user.socket.ChatServer
```

### Bước 2: Compile
```bash
javac -encoding UTF-8 -d bin -cp "bin;lib/*" src/user/gui/*.java src/user/service/*.java
```

### Bước 3: Run Client
```bash
java -cp "bin;lib/*" user.gui.Main
```

### Bước 4: Test
- Login với username: `admin`, `Hung`, hoặc user khác
- Click icon Contact (sidebar) để vào ContactPanel
- Click "Lời mời kết bạn" để xem FriendRequestPanel

## ⚠️ Lưu ý:

### Lỗi "Không thể kết nối đến server!"
- **Nguyên nhân**: ChatServer chưa chạy hoặc port 8888 bị block
- **Giải pháp**: Start ChatServer trước khi login
- **Workaround**: Bỏ qua error (vẫn dùng được friend request features)

### Notification badge chưa real-time
- Hiện tại badge số "5" là hardcoded
- Cần implement: `contactPanel.updateBadge(count)` với `userService.countReceivedFriendRequests()`
- Cần Socket message type FRIEND_REQUEST để update real-time

### Chưa có icon files
- Hiện tại dùng fallback "?" text
- Cần download 7 icons: user.png, chat.png, contact.png, settings.png, search.png, add-friend.png, create-group.png
- Place vào folder `icons/`

## 📝 TODO - Next Steps:

### High Priority:
1. ✅ Fix layout (DONE)
2. ✅ Implement accept/reject/recall (DONE)
3. ❌ Dynamic notification badge với `countReceivedFriendRequests()`
4. ❌ Real-time Socket notifications (FRIEND_REQUEST message type)

### Medium Priority:
5. ❌ Implement FriendListPanel (danh sách bạn bè)
6. ❌ Implement GroupListPanel (danh sách nhóm)
7. ❌ Download và add icon files

### Low Priority:
8. ❌ Optimization: Cache friend requests để giảm database calls
9. ❌ Pagination cho friend requests (nếu >50 requests)
10. ❌ Search trong friend requests

## 🎉 Kết quả:

**Trước:**
- ❌ Socket error blocking UX
- ❌ ContactPanel layout sai (hiện content bên trong)
- ❌ User gửi không thấy lời mời đã gửi (SQL query chưa implement)

**Sau:**
- ✅ Socket error chỉ log warning
- ✅ ContactPanel layout đúng (menu left, content right)
- ✅ User gửi thấy được lời mời đã gửi ở tab "Lời mời đã gửi"
- ✅ Accept/Reject/Recall hoạt động với confirmation dialog
- ✅ CardLayout switching giữa Chat và Contact mode
- ✅ Full friend request workflow hoàn chỉnh

**Test thử đi nhé!** 🚀
