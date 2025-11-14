package user.socket;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message Protocol - Định nghĩa format tin nhắn giữa client và server
 * Sử dụng Serializable để có thể gửi qua ObjectOutputStream
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Message Types
    public enum MessageType {
        // Authentication
        LOGIN,              // Client gửi: đăng nhập
        LOGOUT,             // Client gửi: đăng xuất
        
        // Chat
        CHAT,               // Client gửi: tin nhắn chat
        BROADCAST,          // Server gửi: broadcast tin nhắn đến tất cả
        PRIVATE_MESSAGE,    // Client gửi: tin nhắn riêng
        
        // Friend Requests
        FRIEND_REQUEST_SENT,     // Server gửi: thông báo có lời mời kết bạn mới
        FRIEND_REQUEST_ACCEPTED, // Server gửi: lời mời được chấp nhận
        FRIEND_REQUEST_REJECTED, // Server gửi: lời mời bị từ chối
        FRIEND_REQUEST_RECALLED, // Server gửi: lời mời bị thu hồi
        
        // Friend Management
        UNFRIEND,           // Server gửi: thông báo bị hủy kết bạn
        BLOCK,              // Server gửi: thông báo bị chặn
        
        // Status
        USER_JOINED,        // Server gửi: có user mới online
        USER_LEFT,          // Server gửi: có user offline
        ONLINE_USERS,       // Server gửi: danh sách users online
        
        // Others
        TYPING,             // Client gửi: đang typing
        SUCCESS,            // Server gửi: thành công
        ERROR               // Server gửi: lỗi
    }
    
    private MessageType type;
    private String sender;          // Username của người gửi
    private String receiver;        // Username của người nhận (null nếu là broadcast)
    private String content;         // Nội dung tin nhắn
    private LocalDateTime timestamp;
    private Object data;            // Dữ liệu bổ sung (ví dụ: danh sách users)
    
    // Constructors
    public Message(MessageType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
