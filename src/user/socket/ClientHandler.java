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
