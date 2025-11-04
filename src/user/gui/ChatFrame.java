package user.gui;

import user.socket.SocketClient;
import user.socket.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Giao diện chat hiện đại - Phong cách Zalo/Messenger
 * Chat bubbles với màu sắc đẹp, timestamp, và layout hiện đại
 */
public class ChatFrame extends JInternalFrame {
    // Colors
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color SENT_BUBBLE_COLOR = PRIMARY_COLOR;
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 242, 245);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(51, 51,51);
    
    private JPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton, attachButton, emojiButton;
    private JScrollPane scrollPane;
    private String currentChatUser;
    
    // Socket client
    private SocketClient socketClient;
    private String myUsername;
    
    public ChatFrame(String chatUser, String myUsername) {
        this.currentChatUser = chatUser;
        this.myUsername = myUsername;
        initializeComponents();
        setupLayout();
        initializeSocket();
    }
    
    private void initializeComponents() {
        setTitle("Chat với " + currentChatUser);
        setSize(700, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Chat panel with chat bubbles
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BACKGROUND_COLOR);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Scroll pane
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Message input field
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Buttons
        emojiButton = createIconButton("😊");
        attachButton = createIconButton("📎");
        sendButton = createSendButton();
        
        // Enter key to send
        messageField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header with user info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JLabel userLabel = new JLabel("👤  " + currentChatUser);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        userLabel.setForeground(Color.WHITE);
        
        JLabel statusLabel = new JLabel("● Đang hoạt động");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(200, 230, 255));
        
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        userInfoPanel.add(userLabel);
        userInfoPanel.add(statusLabel);
        
        headerPanel.add(userInfoPanel, BorderLayout.WEST);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftButtonsPanel.setOpaque(false);
        leftButtonsPanel.add(emojiButton);
        leftButtonsPanel.add(attachButton);
        
        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(messageField, BorderLayout.CENTER);
        rightPanel.add(sendButton, BorderLayout.EAST);
        
        inputPanel.add(leftButtonsPanel, BorderLayout.WEST);
        inputPanel.add(rightPanel, BorderLayout.CENTER);
        
        // Add to frame
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JButton createIconButton(String icon) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        button.setPreferredSize(new Dimension(40, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(true);
                button.setBackground(new Color(240, 242, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(false);
            }
        });
        
        return button;
    }
    
    private JButton createSendButton() {
        JButton button = new JButton("Gửi");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setPreferredSize(new Dimension(70, 40));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Gửi qua socket
            if (socketClient != null && socketClient.isConnected()) {
                socketClient.sendPrivateMessage(currentChatUser, message);
            }
            
            // Hiển thị trong GUI
            addChatBubble(message, true, LocalDateTime.now());
            messageField.setText("");
            
            // Scroll to bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }
    
    /**
     * Khởi tạo socket connection
     */
    private void initializeSocket() {
        socketClient = new SocketClient(myUsername, this::handleIncomingMessage);
        
        // Kết nối trong background thread
        new Thread(() -> {
            boolean connected = socketClient.connect();
            if (!connected) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Không thể kết nối đến server!\nVui lòng kiểm tra server đang chạy.",
                        "Lỗi kết nối",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Xử lý message nhận được từ server
     */
    private void handleIncomingMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case CHAT:
                case PRIVATE_MESSAGE:
                    // Chỉ hiển thị nếu message từ người đang chat
                    if (message.getSender().equals(currentChatUser)) {
                        addChatBubble(message.getContent(), false, LocalDateTime.now());
                    }
                    break;
                    
                case USER_JOINED:
                    // Update status
                    setTitle("Chat với " + currentChatUser + " - ● Online");
                    break;
                    
                case USER_LEFT:
                    // Update status
                    setTitle("Chat với " + currentChatUser + " - ○ Offline");
                    break;
                    
                case TYPING:
                    // Show typing indicator (TODO)
                    break;
                    
                default:
                    break;
            }
        });
    }
    
    private void addChatBubble(String message, boolean isSent, LocalDateTime time) {
        JPanel bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.X_AXIS));
        bubbleContainer.setOpaque(false);
        bubbleContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bubbleContainer.setAlignmentX(isSent ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        
        if (isSent) {
            bubbleContainer.add(Box.createHorizontalGlue());
        }
        
        // Create bubble
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        bubble.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        
        JLabel messageLabel = new JLabel("<html><div style='width: 200px;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(isSent ? Color.WHITE : TEXT_COLOR);
        
        JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isSent ? new Color(230, 240, 255) : new Color(120, 120, 120));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(messageLabel);
        textPanel.add(timeLabel);
        
        bubble.add(textPanel, BorderLayout.CENTER);
        
        bubbleContainer.add(bubble);
        
        if (!isSent) {
            bubbleContainer.add(Box.createHorizontalGlue());
        }
        
        chatPanel.add(bubbleContainer);
        chatPanel.revalidate();
        chatPanel.repaint();
    }
}
