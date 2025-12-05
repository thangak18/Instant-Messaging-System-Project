package user.socket;

import java.io.*;
import java.net.*;

/**
 * Client Handler - Xử lý 1 client connection
 * Mỗi client sẽ chạy trên 1 thread riêng
 */
public class ClientHandler implements Runnable {
    
    private Socket socket;
    private ChatServer server;
    private String username;
    
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private volatile boolean running = false;
    
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            // Khởi tạo streams
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            running = true;
            
            // Đọc messages từ client
            while (running) {
                try {
                    Message message = (Message) in.readObject();
                    handleMessage(message);
                    
                } catch (ClassNotFoundException e) {
                    System.err.println("❌ Invalid message format");
                    break;
                }
            }
            
        } catch (EOFException e) {
            // Client đã disconnect
            System.out.println("📤 Client disconnected: " + (username != null ? username : socket.getInetAddress()));
            
        } catch (IOException e) {
            if (running) {
                System.err.println("❌ Error handling client: " + e.getMessage());
            }
            
        } finally {
            close();
        }
    }
    
    /**
     * Xử lý message từ client
     */
    private void handleMessage(Message message) {
        System.out.println("📨 Received: " + message);
        
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
                
            case LOGOUT:
                handleLogout();
                break;
                
            case CHAT:
                handleChat(message);
                break;
                
            case PRIVATE_MESSAGE:
                handlePrivateMessage(message);
                break;
                
            case TYPING:
                handleTyping(message);
                break;
                
            // Friend Request notifications
            case FRIEND_REQUEST_SENT:
            case FRIEND_REQUEST_ACCEPTED:
            case FRIEND_REQUEST_REJECTED:
            case FRIEND_REQUEST_RECALLED:
                handleFriendRequestNotification(message);
                break;
            
            case UNFRIEND:
            case BLOCK:
                handleFriendManagementNotification(message);
                break;
            
            case GROUP_MESSAGE:
                handleGroupMessage(message);
                break;
            
            case GROUP_CREATED:
                handleGroupCreated(message);
                break;
                
            default:
                System.err.println("⚠️  Unknown message type: " + message.getType());
        }
    }
    
    /**
     * Xử lý LOGIN
     */
    private void handleLogin(Message message) {
        this.username = message.getSender();
        
        // Thêm vào danh sách clients
        server.addClient(username, this);
        
        // Gửi SUCCESS cho client
        Message response = new Message(Message.MessageType.SUCCESS);
        response.setContent("Login successful. Welcome " + username + "!");
        sendMessage(response);
    }
    
    /**
     * Xử lý LOGOUT
     */
    private void handleLogout() {
        running = false;
        close();
    }
    
    /**
     * Xử lý CHAT message (broadcast)
     */
    private void handleChat(Message message) {
        message.setSender(username);  // Set sender
        message.setType(Message.MessageType.BROADCAST);
        
        // Broadcast đến tất cả (trừ sender)
        server.broadcast(message, username);
        
        // TODO: Lưu vào database
        // saveMessageToDatabase(message);
    }
    
    /**
     * Xử lý PRIVATE_MESSAGE
     */
    private void handlePrivateMessage(Message message) {
        message.setSender(username);
        
        String receiver = message.getReceiver();
        boolean sent = server.sendToUser(receiver, message);
        
        if (!sent) {
            // User không online
            Message errorMsg = new Message(Message.MessageType.ERROR);
            errorMsg.setContent("User " + receiver + " is not online.");
            sendMessage(errorMsg);
        }
        
        // TODO: Lưu vào database
        // saveMessageToDatabase(message);
    }
    
    /**
     * Xử lý TYPING indicator
     */
    private void handleTyping(Message message) {
        message.setSender(username);
        
        if (message.getReceiver() != null) {
            // Gửi cho 1 người cụ thể
            server.sendToUser(message.getReceiver(), message);
        } else {
            // Broadcast cho tất cả
            server.broadcast(message, username);
        }
    }
    
    /**
     * Xử lý Friend Request notifications
     */
    private void handleFriendRequestNotification(Message message) {
        message.setSender(username);
        String receiver = message.getReceiver();
        
        if (receiver != null) {
            boolean sent = server.sendToUser(receiver, message);
            if (sent) {
                System.out.println("✅ Sent " + message.getType() + " notification: " + username + " → " + receiver);
            } else {
                System.out.println("⚠️  User " + receiver + " is offline. Notification not sent.");
            }
        }
    }
    
    /**
     * Xử lý Unfriend/Block notifications
     */
    private void handleFriendManagementNotification(Message message) {
        message.setSender(username);
        String receiver = message.getReceiver();
        
        if (receiver != null) {
            boolean sent = server.sendToUser(receiver, message);
            if (sent) {
                System.out.println("✅ Sent " + message.getType() + " notification: " + username + " → " + receiver);
            } else {
                System.out.println("⚠️  User " + receiver + " is offline. Notification not sent.");
            }
        }
    }
    
    /**
     * Xử lý Group Message - broadcast đến tất cả thành viên nhóm
     */
    private void handleGroupMessage(Message message) {
        message.setSender(username);
        int groupId = (Integer) message.getData();
        
        System.out.println("📨 Group message from " + username + " to group " + groupId);
        
        // Lấy danh sách thành viên nhóm
        try {
            user.service.GroupService groupService = new user.service.GroupService();
            java.util.List<java.util.Map<String, Object>> members = groupService.getGroupMembers(groupId);
            
            // Broadcast đến tất cả thành viên online (trừ người gửi)
            for (java.util.Map<String, Object> member : members) {
                String memberUsername = (String) member.get("username");
                if (memberUsername != null && !memberUsername.equals(username)) {
                    boolean sent = server.sendToUser(memberUsername, message);
                    if (sent) {
                        System.out.println("✅ Group message sent to: " + memberUsername);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error broadcasting group message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Xử lý thông báo nhóm mới được tạo - gửi đến các thành viên
     */
    @SuppressWarnings("unchecked")
    private void handleGroupCreated(Message message) {
        message.setSender(username);
        
        try {
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) message.getData();
            int groupId = (Integer) data.get("groupId");
            java.util.List<String> members = (java.util.List<String>) data.get("members");
            
            System.out.println("📨 Group created notification from " + username + " for group " + groupId);
            
            // Gửi thông báo đến tất cả thành viên (trừ người tạo)
            for (String memberUsername : members) {
                if (!memberUsername.equals(username)) {
                    boolean sent = server.sendToUser(memberUsername, message);
                    if (sent) {
                        System.out.println("✅ Group created notification sent to: " + memberUsername);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling group created: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gửi message cho client này
     */
    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
                System.out.println("📤 Sent to " + username + ": " + message.getType());
            }
        } catch (IOException e) {
            System.err.println("❌ Error sending message to " + username + ": " + e.getMessage());
            close();
        }
    }
    
    /**
     * Đóng connection
     */
    public void close() {
        running = false;
        
        // Xóa khỏi server
        if (username != null) {
            server.removeClient(username);
        }
        
        // Đóng streams
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("❌ Error closing resources: " + e.getMessage());
        }
    }
    
    public String getUsername() {
        return username;
    }
}
