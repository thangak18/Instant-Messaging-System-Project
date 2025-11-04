package user.gui;

import user.socket.Message;
import user.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Chat Content Panel - Khu vực chat chính
 * Header + message list + input panel
 */
public class ChatContentPanel extends JPanel {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color SENT_BUBBLE_COLOR = new Color(0, 132, 255);
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 242, 245);
    private static final Color BG_COLOR = Color.WHITE;
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private String currentChatUser;
    
    // Components
    private JLabel chatUserLabel;
    private JLabel statusLabel;
    private JPanel messageListPanel;
    private JScrollPane scrollPane;
    private JTextArea messageInput;
    private JButton sendButton;
    
    public ChatContentPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        
        // Message area
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(BG_COLOR);
        messageListPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Input panel
        JPanel inputPanel = createInputPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Welcome message
        showWelcomeMessage();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        // Left - User info
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        
        chatUserLabel = new JLabel("Chọn một cuộc trò chuyện");
        chatUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        statusLabel = new JLabel("Bắt đầu chat ngay!");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(120, 120, 120));
        
        userInfoPanel.add(chatUserLabel);
        userInfoPanel.add(statusLabel);
        
        // Right - Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);
        
        JButton callButton = createHeaderButton("📞", "Gọi thoại");
        JButton videoButton = createHeaderButton("📹", "Gọi video");
        JButton infoButton = createHeaderButton("ℹ️", "Thông tin");
        
        actionsPanel.add(callButton);
        actionsPanel.add(videoButton);
        actionsPanel.add(infoButton);
        
        panel.add(userInfoPanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton createHeaderButton(String icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        button.setPreferredSize(new Dimension(40, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
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
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        // Left buttons - emoji, attachment
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftButtonsPanel.setOpaque(false);
        
        JButton emojiButton = createInputButton("😊", "Emoji");
        JButton attachButton = createInputButton("📎", "Đính kèm");
        JButton galleryButton = createInputButton("🖼️", "Hình ảnh");
        
        leftButtonsPanel.add(emojiButton);
        leftButtonsPanel.add(attachButton);
        leftButtonsPanel.add(galleryButton);
        
        // Message input
        messageInput = new JTextArea();
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setRows(2);
        messageInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JScrollPane inputScroll = new JScrollPane(messageInput);
        inputScroll.setBorder(null);
        
        // Send button
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setPreferredSize(new Dimension(70, 50));
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        sendButton.addActionListener(e -> sendMessage());
        
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        // Right panel - input + send
        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(inputScroll, BorderLayout.CENTER);
        rightPanel.add(sendButton, BorderLayout.EAST);
        
        panel.add(leftButtonsPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createInputButton(String icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        button.setPreferredSize(new Dimension(40, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
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
    
    private void showWelcomeMessage() {
        JLabel welcomeLabel = new JLabel("Chào mừng đến với Zalo Chat!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(150, 150, 150));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Chọn một cuộc trò chuyện để bắt đầu nhắn tin");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(new Color(180, 180, 180));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        messageListPanel.add(Box.createVerticalGlue());
        messageListPanel.add(welcomeLabel);
        messageListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        messageListPanel.add(subLabel);
        messageListPanel.add(Box.createVerticalGlue());
    }
    
    public void openChat(String userName) {
        this.currentChatUser = userName;
        chatUserLabel.setText(userName);
        statusLabel.setText("● Đang hoạt động");
        
        // Clear old messages
        messageListPanel.removeAll();
        messageListPanel.revalidate();
        messageListPanel.repaint();
        
        // Load chat history từ database
        loadChatHistory(userName);
    }
    
    /**
     * LOAD LỊCH SỬ CHAT TỪ DATABASE
     */
    private void loadChatHistory(String friendUsername) {
        SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                return userService.getChatHistory(mainFrame.getUsername(), friendUsername);
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> messages = get();
                    
                    if (messages != null && !messages.isEmpty()) {
                        for (Map<String, Object> msg : messages) {
                            String senderUsername = (String) msg.get("sender_username");
                            String content = (String) msg.get("content");
                            java.sql.Timestamp sentAt = (java.sql.Timestamp) msg.get("sent_at");
                            
                            boolean isSent = senderUsername.equals(mainFrame.getUsername());
                            LocalDateTime time = sentAt.toLocalDateTime();
                            
                            addMessageBubble(content, isSent, time);
                        }
                        
                        scrollToBottom();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (!content.isEmpty() && currentChatUser != null) {
            System.out.println("💬 User gửi: '" + content + "' → " + currentChatUser);
            
            // Lưu vào database
            boolean saved = userService.saveMessage(mainFrame.getUsername(), currentChatUser, content);
            System.out.println(saved ? "✅ Đã lưu vào DB" : "❌ Lưu DB thất bại");
            
            // Gửi qua socket (real-time)
            mainFrame.sendMessage(content, currentChatUser);
            
            // Hiển thị trong GUI
            addMessageBubble(content, true, LocalDateTime.now());
            
            // Clear input
            messageInput.setText("");
            
            // Scroll to bottom
            scrollToBottom();
        } else {
            if (content.isEmpty()) {
                System.err.println("⚠️ Tin nhắn trống, không gửi");
            }
            if (currentChatUser == null) {
                System.err.println("⚠️ Chưa chọn người nhận");
            }
        }
    }
    
    private void addMessageBubble(String content, boolean isSent, LocalDateTime time) {
        JPanel bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.X_AXIS));
        bubbleContainer.setOpaque(false);
        bubbleContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        if (isSent) {
            bubbleContainer.add(Box.createHorizontalGlue());
        }
        
        // Bubble panel
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        bubble.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        
        JLabel messageLabel = new JLabel("<html><div style='width: 200px;'>" + content + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(isSent ? Color.WHITE : new Color(51, 51, 51));
        
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
        
        messageListPanel.add(bubbleContainer);
        messageListPanel.revalidate();
        messageListPanel.repaint();
    }
    
    public void handleMessage(Message message) {
        System.out.println("📩 Nhận message: " + message.getType() + " từ " + message.getSender());
        
        switch (message.getType()) {
            case CHAT:
            case PRIVATE_MESSAGE:
                String sender = message.getSender();
                String content = message.getContent();
                
                System.out.println("  💬 Nội dung: " + content);
                System.out.println("  👤 Current chat user: " + currentChatUser);
                
                // Lưu vào database
                boolean saved = userService.saveMessage(sender, mainFrame.getUsername(), content);
                System.out.println(saved ? "  ✅ Đã lưu vào DB" : "  ❌ Lưu DB thất bại");
                
                // Nếu đang chat với người gửi thì hiển thị message
                if (currentChatUser != null && sender.equals(currentChatUser)) {
                    addMessageBubble(content, false, LocalDateTime.now());
                    scrollToBottom();
                    System.out.println("  ✅ Đã hiển thị message bubble");
                } else {
                    System.out.println("  ⚠️ Không hiển thị (không đang chat với " + sender + ")");
                }
                break;
                
            case USER_JOINED:
                if (currentChatUser != null && message.getSender().equals(currentChatUser)) {
                    statusLabel.setText("● Đang hoạt động");
                }
                break;
                
            case USER_LEFT:
                if (currentChatUser != null && message.getSender().equals(currentChatUser)) {
                    statusLabel.setText("○ Không hoạt động");
                }
                break;
                
            default:
                break;
        }
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
