package user.socket;

import user.socket.Message;
import user.socket.Message.MessageType;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Socket Client - Kết nối đến ChatServer
 * Sử dụng bởi ChatFrame để gửi/nhận messages real-time
 */
public class SocketClient {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private String username;
    private Consumer<Message> messageHandler;  // Callback xử lý message nhận được
    private List<String> onlineUsers = new ArrayList<>(); // Danh sách users online
    
    private volatile boolean running = false;
    private Thread listenerThread;
    
    /**
     * Constructor
     * @param username Username của user này
     * @param messageHandler Callback function xử lý message nhận được
     */
    public SocketClient(String username, Consumer<Message> messageHandler) {
        this.username = username;
        this.messageHandler = messageHandler;
    }
    
    /**
     * Kết nối đến server
     */
    public boolean connect() {
        try {
            // Tạo socket connection
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("✅ Connected to server: " + SERVER_HOST + ":" + SERVER_PORT);
            
            // Khởi tạo streams
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            // Gửi LOGIN message
            Message loginMsg = new Message(Message.MessageType.LOGIN, username, null);
            sendMessage(loginMsg);
            
            // Start listener thread
            running = true;
            listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();
            
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Could not connect to server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lắng nghe messages từ server (chạy trong thread riêng)
     */
    private void listenForMessages() {
        try {
            while (running) {
                Message message = (Message) in.readObject();
                System.out.println("📨 Received from server: " + message.getType());
                
                // Update online users list
                if (message.getType() == MessageType.ONLINE_USERS) {
                    Object data = message.getData();
                    if (data instanceof List<?>) {
                        onlineUsers = new ArrayList<>((List<String>) data);
                        System.out.println("👥 Online users updated: " + onlineUsers.size());
                    }
                }
                
                // Gọi callback handler
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
            }
            
        } catch (EOFException e) {
            System.out.println("📤 Server closed connection");
            
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                System.err.println("❌ Error receiving message: " + e.getMessage());
            }
            
        } finally {
            disconnect();
        }
    }
    
    /**
     * Gửi message đến server
     */
    public void sendMessage(Message message) {
        try {
            if (out != null && socket != null && socket.isConnected()) {
                out.writeObject(message);
                out.flush();
                System.out.println("📤 Sent: " + message.getType());
            } else {
                System.err.println("❌ Not connected to server!");
            }
        } catch (IOException e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            disconnect();
        }
    }
    
    /**
     * Gửi chat message (broadcast)
     */
    public void sendChatMessage(String content) {
        Message message = new Message(Message.MessageType.CHAT, username, content);
        sendMessage(message);
    }
    
    /**
     * Gửi private message
     */
    public void sendPrivateMessage(String receiver, String content) {
        Message message = new Message(Message.MessageType.PRIVATE_MESSAGE, username, receiver, content);
        sendMessage(message);
    }
    
    /**
     * Gửi typing indicator
     */
    public void sendTyping(String receiver) {
        Message message = new Message(Message.MessageType.TYPING, username, null);
        message.setReceiver(receiver);
        sendMessage(message);
    }
    
    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        running = false;
        
        try {
            // Gửi LOGOUT message
            if (out != null) {
                Message logoutMsg = new Message(Message.MessageType.LOGOUT, username, null);
                sendMessage(logoutMsg);
            }
            
            // Đóng streams
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            System.out.println("👋 Disconnected from server");
            
        } catch (IOException e) {
            System.err.println("❌ Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra connection
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && running;
    }
    
    public String getUsername() {
        return username;
    }
    
    /**
     * Lấy danh sách users online
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }
}
