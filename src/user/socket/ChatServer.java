package user.socket;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Chat Server - Multi-threaded Socket Server
 * Quản lý tất cả client connections và broadcast messages
 */
public class ChatServer {
    
    private static final int PORT = 8888;
    private static final int MAX_CLIENTS = 100;
    
    // Danh sách tất cả client handlers (thread-safe)
    private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    
    // Thread pool để quản lý client threads
    private static ExecutorService threadPool;
    
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    
    public ChatServer() {
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }
    
    /**
     * Khởi động server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            
            System.out.println("=================================");
            System.out.println("  CHAT SERVER STARTED");
            System.out.println("  Port: " + PORT);
            System.out.println("  Waiting for clients...");
            System.out.println("=================================\n");
            
            // Lắng nghe client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("📥 New connection from: " + clientSocket.getInetAddress());
                    
                    // Tạo handler cho client này
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    threadPool.execute(handler);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error accepting client: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ Could not start server on port " + PORT);
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    /**
     * Dừng server
     */
    public void stop() {
        running = false;
        
        try {
            // Đóng tất cả client connections
            for (ClientHandler handler : clients.values()) {
                handler.close();
            }
            clients.clear();
            
            // Shutdown thread pool
            threadPool.shutdown();
            
            // Đóng server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            System.out.println("\n🛑 Server stopped.");
            
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Thêm client vào danh sách
     */
    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        System.out.println("✅ " + username + " joined. Total clients: " + clients.size());
        
        // Broadcast thông báo user mới join
        Message joinMessage = new Message(Message.MessageType.USER_JOINED, username, username + " joined the chat!");
        broadcast(joinMessage, null);
        
        // Gửi danh sách users online cho tất cả
        sendOnlineUsers();
    }
    
    /**
     * Xóa client khỏi danh sách
     */
    public void removeClient(String username) {
        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            System.out.println("👋 " + username + " left. Total clients: " + clients.size());
            
            // Broadcast thông báo user left
            Message leftMessage = new Message(Message.MessageType.USER_LEFT, username, username + " left the chat.");
            broadcast(leftMessage, null);
            
            // Cập nhật danh sách users online
            sendOnlineUsers();
        }
    }
    
    /**
     * Broadcast message đến tất cả clients (trừ sender)
     */
    public void broadcast(Message message, String excludeUsername) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            String username = entry.getKey();
            ClientHandler handler = entry.getValue();
            
            // Bỏ qua sender
            if (excludeUsername != null && username.equals(excludeUsername)) {
                continue;
            }
            
            handler.sendMessage(message);
        }
    }
    
    /**
     * Gửi message cho 1 user cụ thể
     */
    public boolean sendToUser(String username, Message message) {
        ClientHandler handler = clients.get(username);
        if (handler != null) {
            handler.sendMessage(message);
            return true;
        }
        return false;
    }
    
    /**
     * Gửi danh sách users online cho tất cả clients
     */
    private void sendOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>(clients.keySet());
        Message message = new Message(Message.MessageType.ONLINE_USERS);
        message.setData(onlineUsers);
        
        broadcast(message, null);
    }
    
    /**
     * Lấy danh sách users online
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }
    
    /**
     * Main method - Khởi động server
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        
        // Xử lý shutdown gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔄 Shutting down server...");
            server.stop();
        }));
        
        // Start server
        server.start();
    }
}
