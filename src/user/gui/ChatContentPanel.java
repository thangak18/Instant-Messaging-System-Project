package user.gui;

import user.socket.Message;
import user.service.UserService;
import user.service.AIService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

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
    private AIService aiService;
    private String currentChatUser;
    
    // Components
    private JLabel chatUserLabel;
    private JLabel statusLabel;
    private JPanel messageListPanel;
    private JScrollPane scrollPane;
    private JTextArea messageInput;
    private JButton sendButton;
    
    // Map lưu bubble theo messageId để hỗ trợ scroll tới tin nhắn
    private Map<Integer, JPanel> messageBubbles = new HashMap<>();
    
    public ChatContentPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        this.aiService = new AIService();
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
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);
        
        // Chat management buttons with PNG icons
        JButton clearHistoryButton = createHeaderButton("icons/bin.png", "Xóa lịch sử", 22);
        JButton searchButton = createHeaderButton("icons/search.png", "Tìm kiếm", 22);
        JButton llmButton = createHeaderButton("icons/generative.png", "Trợ lý AI", 22);
        JButton reportButton = createHeaderButton("icons/alert-sign.png", "Báo cáo spam", 22);
        
        // Action handlers
        clearHistoryButton.addActionListener(e -> {
            if (currentChatUser != null) clearCurrentChatHistory();
        });
        
        searchButton.addActionListener(e -> {
            if (currentChatUser != null) showSearchInChatDialog();
        });
        
        llmButton.addActionListener(e -> showLLMAssistant());
        
        reportButton.addActionListener(e -> {
            if (currentChatUser != null) {
                showReportSpamDialog();
            }
        });
        
        actionsPanel.add(clearHistoryButton);
        actionsPanel.add(searchButton);
        actionsPanel.add(llmButton);
        actionsPanel.add(reportButton);
        
        panel.add(userInfoPanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton createHeaderButton(String iconPath, String tooltip, int iconSize) {
        JButton button = new JButton();
        
        try {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            button.setText("?");
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            System.err.println("⚠️ Không tìm thấy icon: " + iconPath);
        }
        
        button.setPreferredSize(new Dimension(36, 36));
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
        
        // Left buttons - đã bỏ để giao diện gọn gàng hơn
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonsPanel.setOpaque(false);
        
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
        
        // Check online status
        updateUserOnlineStatus(userName);
        
        // Clear old messages và map
        messageListPanel.removeAll();
        messageBubbles.clear();
        messageListPanel.revalidate();
        messageListPanel.repaint();
        
        // Load chat history từ database
        loadChatHistory(userName);
    }
    
    /**
     * Cập nhật trạng thái online của user
     */
    private void updateUserOnlineStatus(String userName) {
        boolean isOnline = false;
        if (mainFrame.getSocketClient() != null) {
            java.util.List<String> onlineUsers = mainFrame.getSocketClient().getOnlineUsers();
            isOnline = onlineUsers.contains(userName);
        }
        
        if (isOnline) {
            statusLabel.setText("● Đang hoạt động");
            statusLabel.setForeground(new Color(67, 220, 96)); // Green
        } else {
            statusLabel.setText("○ Không hoạt động");
            statusLabel.setForeground(new Color(120, 120, 120)); // Gray
        }
    }
    
    /**
     * Refresh online status của current chat user
     */
    public void refreshOnlineStatus() {
        if (currentChatUser != null && !currentChatUser.isEmpty()) {
            updateUserOnlineStatus(currentChatUser);
        }
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
                            int messageId = (int) msg.get("message_id");
                            String senderUsername = (String) msg.get("sender_username");
                            String content = (String) msg.get("content");
                            java.sql.Timestamp sentAt = (java.sql.Timestamp) msg.get("sent_at");
                            
                            boolean isSent = senderUsername.equals(mainFrame.getUsername());
                            LocalDateTime time = sentAt.toLocalDateTime();
                            
                            addMessageBubble(messageId, content, isSent, time);
                        }
                        
                        // Thêm glue ở cuối để không bị stretch
                        messageListPanel.add(Box.createVerticalGlue());
                        
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
            int messageId = userService.saveMessage(mainFrame.getUsername(), currentChatUser, content);
            System.out.println(messageId > 0 ? "✅ Đã lưu vào DB" : "❌ Lưu DB thất bại");
            
            // Gửi qua socket (real-time)
            mainFrame.sendMessage(content, currentChatUser);
            
            // Hiển thị trong GUI với messageId
            if (messageId > 0) {
                addMessageBubble(messageId, content, true, LocalDateTime.now());
            }
            
            // ✅ REFRESH CHAT LIST để hiển thị tin nhắn mới nhất
            mainFrame.refreshChatList();
            
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
    
    private void addMessageBubble(int messageId, String content, boolean isSent, LocalDateTime time) {
        // Outer wrapper để không bị stretch theo chiều dọc
        JPanel outerWrapper = new JPanel(new FlowLayout(isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 3));
        outerWrapper.setOpaque(false);
        outerWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Panel chứa bubble và menu button
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
        innerPanel.setOpaque(false);
        
        // Nút menu "..." bên ngoài bubble
        JButton menuButton = new JButton("...");
        menuButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuButton.setForeground(new Color(150, 150, 150));
        menuButton.setContentAreaFilled(false);
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuButton.setPreferredSize(new Dimension(25, 25));
        menuButton.setMaximumSize(new Dimension(25, 25));
        menuButton.setVisible(false); // Ẩn mặc định
        
        // Bubble panel
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        // Nội dung tin nhắn - sử dụng JTextArea để wrap text tự nhiên
        JTextArea messageArea = new JTextArea(content);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setForeground(isSent ? Color.WHITE : new Color(51, 51, 51));
        messageArea.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        // Giới hạn chiều rộng tối đa
        messageArea.setSize(new Dimension(280, Short.MAX_VALUE));
        Dimension prefSize = messageArea.getPreferredSize();
        messageArea.setPreferredSize(new Dimension(Math.min(280, prefSize.width), prefSize.height));
        
        JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isSent ? new Color(220, 235, 255) : new Color(120, 120, 120));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        timeLabel.setAlignmentX(isSent ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        
        bubble.add(messageArea);
        bubble.add(timeLabel);
        
        if (isSent) {
            // Tin nhắn của mình: căn phải
            innerPanel.add(menuButton);
            innerPanel.add(Box.createHorizontalStrut(5));
            innerPanel.add(bubble);
        } else {
            // Tin nhắn của người khác: căn trái
            innerPanel.add(bubble);
            innerPanel.add(Box.createHorizontalStrut(5));
            innerPanel.add(menuButton);
        }
        
        outerWrapper.add(innerPanel);
        
        // Hiển thị nút menu khi hover vào bubble hoặc menuButton
        java.awt.event.MouseAdapter hoverListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                menuButton.setVisible(true);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Kiểm tra nếu chuột không ở trên bubble hoặc menuButton
                Point mousePos = e.getPoint();
                SwingUtilities.convertPointToScreen(mousePos, e.getComponent());
                
                Point bubblePos = bubble.getLocationOnScreen();
                Rectangle bubbleRect = new Rectangle(bubblePos, bubble.getSize());
                
                Point buttonPos = menuButton.getLocationOnScreen();
                Rectangle buttonRect = new Rectangle(buttonPos, menuButton.getSize());
                
                if (!bubbleRect.contains(mousePos) && !buttonRect.contains(mousePos)) {
                    menuButton.setVisible(false);
                }
            }
        };
        
        bubble.addMouseListener(hoverListener);
        menuButton.addMouseListener(hoverListener);
        outerWrapper.addMouseListener(hoverListener);
        
        // Menu popup khi click "..."
        menuButton.addActionListener(e -> showMessageMenu(menuButton, messageId, isSent, outerWrapper));
        
        // Lưu bubble vào map để hỗ trợ scroll tới tin nhắn
        if (messageId > 0) {
            messageBubbles.put(messageId, outerWrapper);
        }
        
        // Thêm bubble vào cuối danh sách
        messageListPanel.add(outerWrapper);
        messageListPanel.revalidate();
        messageListPanel.repaint();
    }
    
    /**
     * Overload cho tin nhắn nhận real-time (chưa có messageId)
     */
    private void addMessageBubble(String content, boolean isSent, LocalDateTime time) {
        // Tạm thời dùng messageId = -1 cho tin nhắn nhận real-time
        // Tin nhắn này sẽ không có menu xóa cho đến khi refresh
        addMessageBubbleWithoutMenu(content, isSent, time);
    }
    
    /**
     * Tạo bubble đơn giản không có menu (cho tin nhắn real-time)
     */
    private void addMessageBubbleWithoutMenu(String content, boolean isSent, LocalDateTime time) {
        // Outer wrapper với FlowLayout để không bị stretch
        JPanel outerWrapper = new JPanel(new FlowLayout(isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 3));
        outerWrapper.setOpaque(false);
        outerWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Bubble panel
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        // Nội dung tin nhắn
        JTextArea messageArea = new JTextArea(content);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setForeground(isSent ? Color.WHITE : new Color(51, 51, 51));
        messageArea.setBackground(isSent ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setSize(new Dimension(280, Short.MAX_VALUE));
        Dimension prefSize = messageArea.getPreferredSize();
        messageArea.setPreferredSize(new Dimension(Math.min(280, prefSize.width), prefSize.height));
        
        JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isSent ? new Color(220, 235, 255) : new Color(120, 120, 120));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        timeLabel.setAlignmentX(isSent ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        
        bubble.add(messageArea);
        bubble.add(timeLabel);
        
        outerWrapper.add(bubble);
        
        messageListPanel.add(outerWrapper);
        messageListPanel.revalidate();
        messageListPanel.repaint();
    }
    
    /**
     * Hiển thị menu xóa tin nhắn
     */
    private void showMessageMenu(JButton menuButton, int messageId, boolean isSent, JPanel bubbleContainer) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Option 1: Xóa chỉ mình tôi
        JMenuItem deleteForMeItem = new JMenuItem("Xóa chỉ mình tôi");
        deleteForMeItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteForMeItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tin nhắn sẽ bị xóa khỏi thiết bị này.\nNgười khác vẫn có thể nhìn thấy tin nhắn.",
                "Xóa tin nhắn?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = userService.deleteMessageForMe(messageId, mainFrame.getUsername());
                if (success) {
                    // Xóa bubble khỏi UI
                    messageListPanel.remove(bubbleContainer);
                    messageListPanel.revalidate();
                    messageListPanel.repaint();
                    JOptionPane.showMessageDialog(this, "Đã xóa tin nhắn", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa tin nhắn", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        popup.add(deleteForMeItem);
        
        // Option 2: Thu hồi tin nhắn (chỉ cho tin nhắn của mình)
        if (isSent) {
            JMenuItem recallItem = new JMenuItem("Thu hồi tin nhắn");
            recallItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            recallItem.setForeground(new Color(220, 53, 69)); // Màu đỏ
            recallItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tin nhắn sẽ bị xóa vĩnh viễn cho tất cả mọi người.\nBạn có chắc chắn muốn thu hồi?",
                    "Thu hồi tin nhắn?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = userService.recallMessage(messageId, mainFrame.getUsername());
                    if (success) {
                        // Xóa bubble khỏi UI
                        messageListPanel.remove(bubbleContainer);
                        messageListPanel.revalidate();
                        messageListPanel.repaint();
                        JOptionPane.showMessageDialog(this, "Đã thu hồi tin nhắn", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể thu hồi tin nhắn", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            popup.addSeparator();
            popup.add(recallItem);
        }
        
        popup.show(menuButton, 0, menuButton.getHeight());
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
                
                // KHÔNG LƯU VÀO DATABASE Ở ĐÂY - đã lưu ở người gửi rồi
                // Chỉ hiển thị message trong GUI
                
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
    
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Scroll tới tin nhắn cụ thể và highlight nó
     */
    public void scrollToMessage(int messageId) {
        JPanel bubble = messageBubbles.get(messageId);
        if (bubble != null) {
            SwingUtilities.invokeLater(() -> {
                // Scroll tới vị trí tin nhắn
                Rectangle bounds = bubble.getBounds();
                bubble.scrollRectToVisible(bounds);
                
                // Highlight tin nhắn trong 2 giây
                Color originalBg = bubble.getBackground();
                bubble.setOpaque(true);
                bubble.setBackground(new Color(255, 255, 150)); // Màu vàng highlight
                
                // Timer để remove highlight sau 2 giây
                Timer timer = new Timer(2000, e -> {
                    bubble.setOpaque(false);
                    bubble.setBackground(originalBg);
                    bubble.repaint();
                });
                timer.setRepeats(false);
                timer.start();
                
                bubble.repaint();
            });
        } else {
            System.err.println("⚠️ Không tìm thấy tin nhắn với ID: " + messageId);
        }
    }
    
    /**
     * Hiển thị dialog báo cáo spam
     */
    private void showReportSpamDialog() {
        if (currentChatUser == null) return;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Báo cáo spam", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 59, 48));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Báo cáo spam: " + currentChatUser);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel instructionLabel = new JLabel("Vui lòng chọn lý do báo cáo:");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(instructionLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Radio buttons for reasons
        ButtonGroup reasonGroup = new ButtonGroup();
        JRadioButton spamMessagesBtn = new JRadioButton("Tin nhắn spam", true);
        JRadioButton harassmentBtn = new JRadioButton("Quấy rối");
        JRadioButton inappropriateBtn = new JRadioButton("Nội dung không phù hợp");
        JRadioButton scamBtn = new JRadioButton("Lừa đảo");
        JRadioButton otherBtn = new JRadioButton("Khác");
        
        spamMessagesBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        harassmentBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inappropriateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scamBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        otherBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        spamMessagesBtn.setBackground(Color.WHITE);
        harassmentBtn.setBackground(Color.WHITE);
        inappropriateBtn.setBackground(Color.WHITE);
        scamBtn.setBackground(Color.WHITE);
        otherBtn.setBackground(Color.WHITE);
        
        spamMessagesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        harassmentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inappropriateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        scamBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        otherBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        reasonGroup.add(spamMessagesBtn);
        reasonGroup.add(harassmentBtn);
        reasonGroup.add(inappropriateBtn);
        reasonGroup.add(scamBtn);
        reasonGroup.add(otherBtn);
        
        contentPanel.add(spamMessagesBtn);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(harassmentBtn);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(inappropriateBtn);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(scamBtn);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(otherBtn);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton submitButton = new JButton("Gửi báo cáo");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitButton.setBackground(new Color(255, 59, 48));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(120, 35));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.addActionListener(e -> {
            final String reason;
            if (spamMessagesBtn.isSelected()) reason = "Tin nhắn spam";
            else if (harassmentBtn.isSelected()) reason = "Quấy rối";
            else if (inappropriateBtn.isSelected()) reason = "Nội dung không phù hợp";
            else if (scamBtn.isSelected()) reason = "Lừa đảo";
            else if (otherBtn.isSelected()) reason = "Khác";
            else reason = "Khác";
            
            submitButton.setEnabled(false);
            submitButton.setText("Đang gửi...");
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return userService.reportSpam(mainFrame.getUsername(), currentChatUser, reason);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(dialog,
                                "Báo cáo của bạn đã được gửi.\nChúng tôi sẽ xem xét và xử lý.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Không thể gửi báo cáo. Vui lòng thử lại!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                            submitButton.setEnabled(true);
                            submitButton.setText("Gửi báo cáo");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog,
                            "Lỗi: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        submitButton.setEnabled(true);
                        submitButton.setText("Gửi báo cáo");
                    }
                }
            };
            
            worker.execute();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ==================== CHAT HISTORY FEATURES ====================
    
    /**
     * d. XÓA TOÀN BỘ LỊCH SỬ CHAT VỚI NGƯỜI HIỆN TẠI
     */
    private void clearCurrentChatHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa toàn bộ lịch sử chat với " + currentChatUser + "?\nHành động này không thể hoàn tác!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return userService.deleteChatHistory(mainFrame.getUsername(), currentChatUser);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ChatContentPanel.this,
                                "Đã xóa toàn bộ lịch sử chat!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                            messageListPanel.removeAll();
                            showWelcomeMessage();
                            messageListPanel.revalidate();
                            messageListPanel.repaint();
                        } else {
                            JOptionPane.showMessageDialog(ChatContentPanel.this,
                                "Không thể xóa lịch sử!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * f. TÌM KIẾM TRONG LỊCH SỬ CHAT VỚI NGƯỜI HIỆN TẠI
     */
    private void showSearchInChatDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Tìm kiếm với " + currentChatUser, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Tìm kiếm với " + currentChatUser);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setPreferredSize(new Dimension(100, 35));
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Từ khóa:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(topPanel, BorderLayout.NORTH);
        
        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        searchPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Search action
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập từ khóa!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            resultsPanel.removeAll();
            searchButton.setEnabled(false);
            searchButton.setText("Đang tìm...");
            
            SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
                @Override
                protected java.util.List<Map<String, Object>> doInBackground() {
                    return userService.searchInChatHistory(mainFrame.getUsername(), currentChatUser, keyword);
                }
                
                @Override
                protected void done() {
                    try {
                        java.util.List<Map<String, Object>> results = get();
                        
                        if (results == null || results.isEmpty()) {
                            JLabel label = new JLabel("Không tìm thấy kết quả");
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            label.setForeground(new Color(150, 150, 150));
                            resultsPanel.add(label);
                        } else {
                            for (Map<String, Object> result : results) {
                                int messageId = (Integer) result.get("id");
                                String sender = (String) result.get("sender");
                                String content = (String) result.get("content");
                                java.sql.Timestamp sentAt = (java.sql.Timestamp) result.get("sent_at");
                                
                                JPanel resultItem = new JPanel(new BorderLayout(10, 5));
                                resultItem.setBackground(Color.WHITE);
                                resultItem.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                                    BorderFactory.createEmptyBorder(12, 10, 12, 10)
                                ));
                                resultItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                                resultItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                
                                boolean isSent = sender.equals(mainFrame.getUsername());
                                String timeStr = new java.text.SimpleDateFormat("dd/MM HH:mm").format(sentAt);
                                
                                JLabel nameLabel = new JLabel((isSent ? "Bạn" : sender) + " - " + timeStr);
                                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                nameLabel.setForeground(isSent ? PRIMARY_COLOR : new Color(100, 100, 100));
                                
                                JLabel contentLabel = new JLabel("<html>" + highlightKeyword(content, keyword) + "</html>");
                                contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                                
                                // Icon để chỉ có thể click
                                JLabel arrowLabel = new JLabel("→");
                                arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                                arrowLabel.setForeground(PRIMARY_COLOR);
                                
                                JPanel textPanel = new JPanel();
                                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                                textPanel.setOpaque(false);
                                textPanel.add(nameLabel);
                                textPanel.add(contentLabel);
                                
                                resultItem.add(textPanel, BorderLayout.CENTER);
                                resultItem.add(arrowLabel, BorderLayout.EAST);
                                
                                // Thêm click listener để di chuyển đến tin nhắn
                                final int msgId = messageId;
                                resultItem.addMouseListener(new java.awt.event.MouseAdapter() {
                                    @Override
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        dialog.dispose(); // Đóng dialog
                                        scrollToMessage(msgId); // Scroll tới tin nhắn
                                    }
                                    
                                    @Override
                                    public void mouseEntered(java.awt.event.MouseEvent e) {
                                        resultItem.setBackground(new Color(240, 245, 255));
                                    }
                                    
                                    @Override
                                    public void mouseExited(java.awt.event.MouseEvent e) {
                                        resultItem.setBackground(Color.WHITE);
                                    }
                                });
                                
                                resultsPanel.add(resultItem);
                            }
                        }
                        
                        resultsPanel.revalidate();
                        resultsPanel.repaint();
                        searchButton.setEnabled(true);
                        searchButton.setText("Tìm kiếm");
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        searchButton.setEnabled(true);
                        searchButton.setText("Tìm kiếm");
                    }
                }
            };
            
            worker.execute();
        });
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(searchPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null) return text;
        return text.replaceAll("(?i)(" + keyword + ")", 
            "<span style='background-color: yellow; font-weight: bold;'>$1</span>");
    }
    
    /**
     * h. LLM CHAT ASSISTANT
     */
    private void showLLMAssistant() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Trợ lý AI", true);
        dialog.setSize(650, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(138, 43, 226));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Trợ lý AI - Gợi ý tin nhắn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // Status label
        JLabel statusLabel = new JLabel(aiService.isAPIConfigured() ? "Online" : "Offline Mode");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 200, 255));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Quick suggestions panel
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        quickPanel.setOpaque(false);
        quickPanel.setBorder(BorderFactory.createTitledBorder("Gợi ý nhanh:"));
        
        String[] quickSuggestions = {"Xin lỗi", "Cảm ơn", "Chúc mừng", "Hẹn gặp", "Hỏi thăm", "Động viên", "Từ chối lịch sự"};
        JTextArea inputArea = new JTextArea(3, 40);
        
        for (String suggestion : quickSuggestions) {
            JButton btn = new JButton(suggestion);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> inputArea.setText(suggestion));
            quickPanel.add(btn);
        }
        
        JLabel instructionLabel = new JLabel(
            "<html>Mô tả tình huống hoặc nhập yêu cầu, AI sẽ gợi ý tin nhắn phù hợp:</html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane inputScroll = new JScrollPane(inputArea);
        
        JButton generateButton = new JButton("Tạo gợi ý");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateButton.setBackground(new Color(138, 43, 226));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setBorderPainted(false);
        generateButton.setPreferredSize(new Dimension(130, 40));
        generateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JTextArea resultArea = new JTextArea(8, 40);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(248, 249, 250));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        resultArea.setText("Gợi ý sẽ hiển thị ở đây...");
        resultArea.setForeground(new Color(150, 150, 150));
        
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Gợi ý từ AI:"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton regenerateButton = new JButton("Tạo lại");
        regenerateButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        regenerateButton.setEnabled(false);
        regenerateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton copyButton = new JButton("Sao chép");
        copyButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copyButton.setEnabled(false);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyButton.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = 
                new java.awt.datatransfer.StringSelection(resultArea.getText());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            copyButton.setText("Đã sao chép!");
            Timer timer = new Timer(2000, evt -> copyButton.setText("Sao chép"));
            timer.setRepeats(false);
            timer.start();
        });
        
        JButton useButton = new JButton("Sử dụng");
        useButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        useButton.setBackground(PRIMARY_COLOR);
        useButton.setForeground(Color.WHITE);
        useButton.setFocusPainted(false);
        useButton.setBorderPainted(false);
        useButton.setEnabled(false);
        useButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        useButton.addActionListener(e -> {
            messageInput.setText(resultArea.getText());
            dialog.dispose();
        });
        
        buttonPanel.add(regenerateButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(useButton);
        
        // Generate action
        Runnable generateAction = () -> {
            String prompt = inputArea.getText().trim();
            if (prompt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập yêu cầu!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            generateButton.setEnabled(false);
            generateButton.setText("⏳ Đang tạo...");
            regenerateButton.setEnabled(false);
            resultArea.setText("AI đang suy nghĩ...");
            resultArea.setForeground(new Color(100, 100, 100));
            
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    // Lấy context từ chat (5 tin nhắn gần nhất)
                    String chatContext = "";
                    if (currentChatUser != null) {
                        chatContext = "Đang chat với: " + currentChatUser;
                    }
                    return aiService.generateSuggestion(prompt, chatContext);
                }
                
                @Override
                protected void done() {
                    try {
                        String suggestion = get();
                        resultArea.setText(suggestion);
                        resultArea.setForeground(Color.BLACK);
                        copyButton.setEnabled(true);
                        useButton.setEnabled(true);
                        regenerateButton.setEnabled(true);
                        generateButton.setEnabled(true);
                        generateButton.setText("Tạo gợi ý");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        resultArea.setText("Lỗi: Không thể tạo gợi ý! Vui lòng thử lại.");
                        resultArea.setForeground(new Color(200, 50, 50));
                        generateButton.setEnabled(true);
                        generateButton.setText("Tạo gợi ý");
                    }
                }
            };
            
            worker.execute();
        };
        
        generateButton.addActionListener(e -> generateAction.run());
        regenerateButton.addActionListener(e -> generateAction.run());
        
        // Input panel
        JPanel topInputPanel = new JPanel(new BorderLayout(5, 5));
        topInputPanel.setOpaque(false);
        topInputPanel.add(instructionLabel, BorderLayout.NORTH);
        topInputPanel.add(inputScroll, BorderLayout.CENTER);
        
        JPanel inputWithButton = new JPanel(new BorderLayout(10, 0));
        inputWithButton.setOpaque(false);
        inputWithButton.add(topInputPanel, BorderLayout.CENTER);
        inputWithButton.add(generateButton, BorderLayout.EAST);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        inputPanel.add(quickPanel, BorderLayout.NORTH);
        inputPanel.add(inputWithButton, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(bottomPanel, BorderLayout.CENTER);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
