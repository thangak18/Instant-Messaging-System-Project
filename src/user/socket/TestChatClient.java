package user.socket;

import user.socket.SocketClient;
import user.socket.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Test GUI client để test socket chat
 */
public class TestChatClient extends JFrame {
    
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private SocketClient socketClient;
    private String username;
    
    public TestChatClient(String username) {
        this.username = username;
        initializeGUI();
        initializeSocket();
    }
    
    private void initializeGUI() {
        setTitle("Chat Test - " + username);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Event handlers
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (socketClient != null) {
                    socketClient.disconnect();
                }
            }
        });
    }
    
    private void initializeSocket() {
        socketClient = new SocketClient(username, this::handleMessage);
        
        appendChat("🔌 Đang kết nối đến server...");
        
        new Thread(() -> {
            boolean connected = socketClient.connect();
            if (connected) {
                appendChat("✅ Đã kết nối thành công!");
            } else {
                appendChat("❌ Không thể kết nối đến server!");
            }
        }).start();
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && socketClient != null) {
            // Broadcast message
            socketClient.sendChatMessage(message);
            
            // Display sent message
            appendChat("[" + getCurrentTime() + "] Bạn: " + message);
            
            messageField.setText("");
        }
    }
    
    private void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case CHAT:
                case PRIVATE_MESSAGE:
                    appendChat("[" + getCurrentTime() + "] " + 
                              message.getSender() + ": " + 
                              message.getContent());
                    break;
                    
                case USER_JOINED:
                    appendChat("🟢 " + message.getSender() + " đã tham gia");
                    break;
                    
                case USER_LEFT:
                    appendChat("🔴 " + message.getSender() + " đã rời đi");
                    break;
                    
                case ONLINE_USERS:
                    if (message.getData() != null) {
                        java.util.List<?> users = (java.util.List<?>) message.getData();
                        appendChat("👥 Người dùng online: " + users);
                    }
                    break;
                    
                case SUCCESS:
                    appendChat("✅ " + message.getContent());
                    break;
                    
                case ERROR:
                    appendChat("❌ " + message.getContent());
                    break;
                    
                default:
                    appendChat("📨 " + message.getType() + ": " + message.getContent());
                    break;
            }
        });
    }
    
    private void appendChat(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(text + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    public static void main(String[] args) {
        // Get username from command line or prompt
        String username = "User1";
        if (args.length > 0) {
            username = args[0];
        } else {
            username = JOptionPane.showInputDialog(
                null,
                "Nhập tên của bạn:",
                "Chat Test Client",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (username == null || username.trim().isEmpty()) {
                username = "User" + System.currentTimeMillis() % 1000;
            }
        }
        
        final String finalUsername = username;
        SwingUtilities.invokeLater(() -> {
            TestChatClient client = new TestChatClient(finalUsername);
            client.setLocationRelativeTo(null);
            client.setVisible(true);
        });
    }
}
