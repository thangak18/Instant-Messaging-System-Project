package user.gui;

import user.socket.Message;
import user.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Chat List Panel - Danh sách cuộc trò chuyện
 * Search bar, tabs, chat items
 */
public class ChatListPanel extends JPanel {
    
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color SEARCH_BG = Color.WHITE;
    private static final Color ITEM_HOVER = new Color(240, 240, 240);
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final int PANEL_WIDTH = 350;
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private JTextField searchField;
    private JPanel chatListContainer;
    private Map<String, ChatItemPanel> chatItems = new HashMap<>();
    private javax.swing.Timer refreshTimer;
    private java.util.List<String> onlineUsers = new java.util.ArrayList<>();
    
    public ChatListPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        initializeUI();
        loadRecentChats(); // Load data thật từ database
        startAutoRefresh(); // Auto refresh mỗi 1 phút
    }
    
    private void initializeUI() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        // Search bar with icon
        JPanel searchBarPanel = createSearchBar();
        
        // Action buttons panel
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionButtonsPanel.setOpaque(false);
        
        // Global search button (paper-plane)
        JButton globalSearchButton = createIconButton("icons/paper-plane.png", "Tìm kiếm toàn bộ lịch sử", 24);
        globalSearchButton.addActionListener(e -> showGlobalSearchDialog());
        
        JButton addFriendButton = createIconButton("icons/add-friend.png", "Thêm bạn", 24);
        
        // Click handler for Add Friend
        addFriendButton.addActionListener(e -> {
            AddFriendDialog dialog = new AddFriendDialog(mainFrame, mainFrame.getUsername());
            dialog.setVisible(true);
        });
        
        actionButtonsPanel.add(globalSearchButton);
        actionButtonsPanel.add(addFriendButton);
        
        headerPanel.add(searchBarPanel, BorderLayout.CENTER);
        headerPanel.add(actionButtonsPanel, BorderLayout.EAST);
        
        // Tabs panel
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        tabsPanel.setBackground(Color.WHITE);
        tabsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        JLabel allTab = createTab("Tất cả", true);
        JLabel onlineTab = createTab("Online", false);
        
        // Click handlers for tabs
        allTab.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Update tab styles
                allTab.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
                allTab.setForeground(new Color(0, 132, 255));
                allTab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 132, 255)));
                
                onlineTab.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
                onlineTab.setForeground(new Color(100, 100, 100));
                onlineTab.setBorder(null);
                
                // Show all chats
                loadRecentChats();
            }
        });
        
        onlineTab.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Update tab styles
                onlineTab.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
                onlineTab.setForeground(new Color(0, 132, 255));
                onlineTab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 132, 255)));
                
                allTab.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
                allTab.setForeground(new Color(100, 100, 100));
                allTab.setBorder(null);
                
                // Show only online friends
                loadOnlineFriends();
            }
        });
        
        tabsPanel.add(allTab);
        tabsPanel.add(onlineTab);
        
        // Chat list container
        chatListContainer = new JPanel();
        chatListContainer.setLayout(new BoxLayout(chatListContainer, BoxLayout.Y_AXIS));
        chatListContainer.setBackground(BG_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(chatListContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Combine header + tabs
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(tabsPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Tạo search bar với icon search bên trong
     */
    private JPanel createSearchBar() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setPreferredSize(new Dimension(240, 38));
        
        // TextField
        searchField = new JTextField();
        searchField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 38, 8, 10)
        ));
        searchField.setBackground(new Color(245, 245, 245));
        
        // Thêm event listener: chỉ search khi nhấn Enter
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        // Icon panel overlay - có thể click
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        iconPanel.setOpaque(false);
        iconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm click listener cho icon panel
        iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                performSearch();
            }
        });
        
        try {
            ImageIcon searchIcon = new ImageIcon("icons/search.png");
            Image scaledImage = searchIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconPanel.add(iconLabel);
        } catch (Exception e) {
            // Fallback text
            JLabel iconLabel = new JLabel("Tim");
            iconLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 12));
            iconLabel.setForeground(new Color(150, 150, 150));
            iconPanel.add(iconLabel);
        }
        
        // Layer panel để đặt icon lên trên text field
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(240, 38));
        
        searchField.setBounds(0, 0, 240, 38);
        iconPanel.setBounds(0, 0, 40, 38);
        
        layeredPane.add(searchField, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(iconPanel, JLayeredPane.PALETTE_LAYER);
        
        container.add(layeredPane, BorderLayout.CENTER);
        
        return container;
    }
    
    /**
     * THỰC HIỆN TÌM KIẾM - chỉ gọi khi bấm icon hoặc Enter
     */
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // Nếu rỗng, load lại danh sách đầy đủ
            loadRecentChats();
        } else {
            // Thực hiện tìm kiếm
            filterChatList(searchText);
        }
    }
    
    /**
     * Tạo icon button từ file PNG
     */
    private JButton createIconButton(String iconPath, String tooltip, int iconSize) {
        JButton button = new JButton();
        
        try {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Fallback text
            button.setText("?");
            button.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
            button.setForeground(new Color(0, 132, 255));
            System.err.println("⚠️ Không tìm thấy icon: " + iconPath);
        }
        
        button.setPreferredSize(new Dimension(36, 36));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setContentAreaFilled(false);
        
        // Hover effect
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
    
    private JLabel createTab(String text, boolean active) {
        JLabel tab = new JLabel(text);
        tab.setFont(new Font(UIHelper.getDefaultFontName(), active ? Font.BOLD : Font.PLAIN, 14));
        tab.setForeground(active ? new Color(0, 132, 255) : new Color(100, 100, 100));
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (active) {
            tab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 132, 255)));
        }
        
        return tab;
    }
    
    /**
     * LOAD DANH SÁCH CHAT TỪ DATABASE
     */
    private void loadRecentChats() {
        chatListContainer.removeAll();
        chatItems.clear();
        
        SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                return userService.getRecentChats(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> chats = get();
                    
                    if (chats == null || chats.isEmpty()) {
                        showEmptyMessage();
                    } else {
                        for (Map<String, Object> chat : chats) {
                            String friendUsername = (String) chat.get("friend_username");
                            String friendName = (String) chat.get("friend_name");
                            String displayName = (friendName != null && !friendName.isEmpty()) ? 
                                                 friendName : friendUsername;
                            
                            String lastMessage = (String) chat.get("last_message");
                            java.sql.Timestamp sentAt = (java.sql.Timestamp) chat.get("sent_at");
                            int unreadCount = (int) chat.get("unread_count");
                            
                            // Format time
                            String timeStr = formatTime(sentAt);
                            
                            // Check online status
                            boolean isOnline = false;
                            if (mainFrame.getSocketClient() != null && mainFrame.getSocketClient().isConnected()) {
                                onlineUsers = mainFrame.getSocketClient().getOnlineUsers();
                                isOnline = onlineUsers.contains(friendUsername);
                            }
                            
                            // Add chat item with online status
                            ChatItemPanel item = new ChatItemPanel(friendUsername, displayName, lastMessage, timeStr, isOnline, unreadCount, sentAt);
                            item.addMouseListener(new java.awt.event.MouseAdapter() {
                                @Override
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    mainFrame.openChat(friendUsername);
                                }
                            });
                            chatListContainer.add(item);
                            chatItems.put(friendUsername, item);
                        }
                    }
                    
                    chatListContainer.revalidate();
                    chatListContainer.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyMessage();
                }
            }
        };
        
        worker.execute();
    }
    
    private String formatTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "";
        
        java.util.Date now = new java.util.Date();
        java.util.Date msgTime = new java.util.Date(timestamp.getTime());
        
        long diffMs = now.getTime() - msgTime.getTime();
        long diffMins = diffMs / (60 * 1000);
        long diffHours = diffMs / (60 * 60 * 1000);
        long diffDays = diffMs / (24 * 60 * 60 * 1000);
        
        if (diffMins < 1) {
            return "Vừa xong";
        } else if (diffMins < 60) {
            return diffMins + " phút";
        } else if (diffHours < 24) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(msgTime);
        } else if (diffDays < 7) {
            if (diffDays == 1) return "Hôm qua";
            return diffDays + " ngày";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            return sdf.format(msgTime);
        }
    }
    
    private void showEmptyMessage() {
        JLabel label = new JLabel("<html><center><b>[Chat]</b><br><br>Chưa có cuộc trò chuyện nào<br>Hãy thêm bạn bè và bắt đầu chat!</center></html>");
        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        label.setForeground(new Color(150, 150, 150));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(80, 20, 80, 20));
        
        chatListContainer.add(label);
        chatListContainer.revalidate();
        chatListContainer.repaint();
    }
    
    
    private void addChatItem(String username, String displayName, String lastMessage, String time, boolean online, int unreadCount) {
        // Kiểm tra online status từ mainFrame
        if (mainFrame.getSocketClient() != null && mainFrame.getSocketClient().isConnected()) {
            onlineUsers = mainFrame.getSocketClient().getOnlineUsers();
            online = onlineUsers.contains(username);
        }
        
        ChatItemPanel item = new ChatItemPanel(username, displayName, lastMessage, time, online, unreadCount, null);
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mainFrame.openChat(username);
            }
        });
        
        chatListContainer.add(item);
        chatItems.put(username, item);
    }
    
    /**
     * CẬP NHẬT CHAT LIST KHI NHẬN TIN NHẮN MỚI
     */
    public void updateChatList(Message message) {
        String sender = message.getSender();
        String content = message.getContent();
        
        // Bỏ qua message từ chính mình
        if (sender != null && sender.equals(mainFrame.getUsername())) {
            return;
        }
        
        // Chỉ xử lý message loại PRIVATE_MESSAGE và BROADCAST
        Message.MessageType type = message.getType();
        if (type != Message.MessageType.PRIVATE_MESSAGE && 
            type != Message.MessageType.BROADCAST) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            boolean isOnline = onlineUsers.contains(sender);
            
            if (!chatItems.containsKey(sender)) {
                // Thêm chat item mới
                ChatItemPanel item = new ChatItemPanel(sender, sender, content, "Vừa xong", isOnline, 1, now);
                item.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        mainFrame.openChat(sender);
                    }
                });
                chatListContainer.add(item, 0);
                chatItems.put(sender, item);
            } else {
                // Cập nhật chat item có sẵn
                JPanel panel = chatItems.get(sender);
                if (panel instanceof ChatItemPanel) {
                    ChatItemPanel item = (ChatItemPanel) panel;
                    item.updateLastMessage(content, now);
                    
                    // Move to top
                    chatListContainer.remove(item);
                    chatListContainer.add(item, 0);
                }
            }
            
            chatListContainer.revalidate();
            chatListContainer.repaint();
        });
    }
    
    /**
     * Chat Item Panel - Item trong danh sách chat
     */
    private class ChatItemPanel extends JPanel {
        private String username; // Lưu username để identify
        private JLabel nameLabel;
        private JLabel messageLabel;
        private JLabel timeLabel;
        private JLabel badgeLabel;
        private JLabel onlineDot;
        private java.sql.Timestamp sentAt; // Lưu timestamp để refresh
        
        public ChatItemPanel(String username, String displayName, String lastMessage, String time, boolean online, int unreadCount, java.sql.Timestamp sentAt) {
            this.username = username;
            this.sentAt = sentAt;
            setLayout(new BorderLayout(10, 5));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setPreferredSize(new Dimension(PANEL_WIDTH, 70));
            setMinimumSize(new Dimension(PANEL_WIDTH, 70));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Avatar panel
            JPanel avatarPanel = new JPanel(null);
            avatarPanel.setPreferredSize(new Dimension(50, 50));
            avatarPanel.setOpaque(false);
            
            JLabel avatar = new JLabel();
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setBounds(0, 0, 50, 50);
            try {
                ImageIcon icon = new ImageIcon("icons/user.png");
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                avatar.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                avatar.setText("[A]");
                avatar.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
                avatar.setForeground(new Color(0, 132, 255));
            }
            avatarPanel.add(avatar);
            
            // Online indicator (chấm xanh)
            onlineDot = new JLabel("●");
            onlineDot.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            onlineDot.setForeground(new Color(67, 220, 96));
            onlineDot.setBounds(35, 35, 15, 15);
            onlineDot.setVisible(online); // Chỉ hiện khi online
            avatarPanel.add(onlineDot);
            
            // Info panel
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setOpaque(false);
            
            // Name + time
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            
            nameLabel = new JLabel(displayName);
            nameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 15));
            
            timeLabel = new JLabel(time);
            timeLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            timeLabel.setForeground(new Color(120, 120, 120));
            
            topPanel.add(nameLabel, BorderLayout.WEST);
            topPanel.add(timeLabel, BorderLayout.EAST);
            
            // Message + badge
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            
            // Giới hạn độ dài tin nhắn
            String displayMessage = lastMessage;
            if (displayMessage != null && displayMessage.length() > 30) {
                displayMessage = displayMessage.substring(0, 30) + "...";
            }
            messageLabel = new JLabel(displayMessage);
            messageLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
            messageLabel.setForeground(new Color(100, 100, 100));
            
            if (unreadCount > 0) {
                badgeLabel = new JLabel(String.valueOf(unreadCount));
                badgeLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 11));
                badgeLabel.setForeground(Color.WHITE);
                badgeLabel.setBackground(new Color(255, 59, 48));
                badgeLabel.setOpaque(true);
                badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                badgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                bottomPanel.add(badgeLabel, BorderLayout.EAST);
            }
            
            bottomPanel.add(messageLabel, BorderLayout.CENTER);
            
            infoPanel.add(topPanel, BorderLayout.NORTH);
            infoPanel.add(bottomPanel, BorderLayout.CENTER);
            
            add(avatarPanel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
            
            // Hover effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(ITEM_HOVER);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(Color.WHITE);
                }
            });
        }
        
        public void updateLastMessage(String message, String time) {
            String displayMessage = message;
            if (displayMessage != null && displayMessage.length() > 30) {
                displayMessage = displayMessage.substring(0, 30) + "...";
            }
            messageLabel.setText(displayMessage);
            timeLabel.setText(time);
        }
        
        public void updateLastMessage(String message, java.sql.Timestamp timestamp) {
            this.sentAt = timestamp;
            String displayMessage = message;
            if (displayMessage != null && displayMessage.length() > 30) {
                displayMessage = displayMessage.substring(0, 30) + "...";
            }
            messageLabel.setText(displayMessage);
            timeLabel.setText(formatTime(timestamp));
        }
        
        public void refreshTimeLabel() {
            if (sentAt != null) {
                timeLabel.setText(formatTime(sentAt));
            }
        }
        
        public void setOnlineStatus(boolean online) {
            if (onlineDot != null) {
                onlineDot.setVisible(online);
            }
        }
    }
    
    /**
     * Refresh chat list (khi có bạn mới hoặc accept friend request)
     */
    public void refreshChatList() {
        System.out.println("🔄 Refreshing chat list...");
        loadRecentChats();
    }
    
    /**
     * LOAD DANH SÁCH NHÓM CỦA USER VÀ THÊM VÀO CHAT LIST
     */
    /**
     * BẮT ĐẦU AUTO-REFRESH MỖI 1 PHÚT
     */
    private void startAutoRefresh() {
        // Refresh mỗi 60 giây (1 phút) để cập nhật thời gian
        refreshTimer = new javax.swing.Timer(60000, e -> {
            System.out.println("⏰ Auto-refresh chat list (1 phút)");
            refreshTimeLabels();
        });
        refreshTimer.start();
    }
    
    /**
     * CHỈ CẬP NHẬT THỜI GIAN CHO CÁC CHAT ITEMS (KHÔNG RELOAD TỪ DB)
     */
    private void refreshTimeLabels() {
        for (ChatItemPanel panel : chatItems.values()) {
            panel.refreshTimeLabel();
        }
    }
    
    /**
     * CẬP NHẬT ONLINE USERS LIST
     */
    public void updateOnlineUsers(java.util.List<String> users) {
        this.onlineUsers = new java.util.ArrayList<>(users);
        // Refresh để hiển thị chấm xanh
        for (Map.Entry<String, ChatItemPanel> entry : chatItems.entrySet()) {
            String username = entry.getKey();
            ChatItemPanel item = entry.getValue();
            boolean isOnline = onlineUsers.contains(username);
            item.setOnlineStatus(isOnline);
        }
    }
    
    /**
     * LỌC DANH SÁCH CHAT THEO TỪ KHÓA TÌM KIẾM
     */
    private void filterChatList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // Nếu search rỗng, load lại toàn bộ danh sách
            loadRecentChats();
            return;
        }
        
        chatListContainer.removeAll();
        chatItems.clear();
        
        SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                return userService.searchFriends(mainFrame.getUsername(), searchText);
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> friends = get();
                    
                    if (friends == null || friends.isEmpty()) {
                        JLabel label = new JLabel("<html><center><b>[?]</b><br><br>Không tìm thấy kết quả<br>cho '" + searchText + "'</center></html>");
                        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
                        label.setForeground(new Color(150, 150, 150));
                        label.setAlignmentX(Component.CENTER_ALIGNMENT);
                        label.setBorder(new EmptyBorder(80, 20, 80, 20));
                        chatListContainer.add(label);
                    } else {
                        for (Map<String, Object> friend : friends) {
                            String friendUsername = (String) friend.get("username");
                            String friendName = (String) friend.get("full_name");
                            String displayName = (friendName != null && !friendName.isEmpty()) ? 
                                                 friendName : friendUsername;
                            
                            boolean isOnline = onlineUsers.contains(friendUsername);
                            
                            // Add chat item (sẽ hiện "Bắt đầu trò chuyện" nếu chưa có tin nhắn)
                            ChatItemPanel item = new ChatItemPanel(friendUsername, displayName, "Bắt đầu trò chuyện", "", isOnline, 0, null);
                            item.addMouseListener(new java.awt.event.MouseAdapter() {
                                @Override
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    mainFrame.openChat(friendUsername);
                                }
                            });
                            chatListContainer.add(item);
                            chatItems.put(friendUsername, item);
                        }
                    }
                    
                    chatListContainer.revalidate();
                    chatListContainer.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * LOAD DANH SÁCH BẠN BÈ ĐANG ONLINE
     */
    private void loadOnlineFriends() {
        chatListContainer.removeAll();
        chatItems.clear();
        
        SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                java.util.List<Map<String, Object>> allChats = userService.getRecentChats(mainFrame.getUsername());
                java.util.List<Map<String, Object>> onlineChats = new java.util.ArrayList<>();
                
                // Lấy danh sách online users từ socket client
                java.util.List<String> onlineUsers = new java.util.ArrayList<>();
                if (mainFrame.getSocketClient() != null && mainFrame.getSocketClient().isConnected()) {
                    onlineUsers = mainFrame.getSocketClient().getOnlineUsers();
                }
                
                // Filter chỉ lấy những người đang online
                for (Map<String, Object> chat : allChats) {
                    String friendUsername = (String) chat.get("friend_username");
                    if (onlineUsers.contains(friendUsername)) {
                        onlineChats.add(chat);
                    }
                }
                
                return onlineChats;
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> chats = get();
                    
                    if (chats == null || chats.isEmpty()) {
                        JLabel label = new JLabel("<html><center>💤<br><br>Không có bạn bè nào đang online<br>Hãy quay lại sau!</center></html>");
                        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
                        label.setForeground(new Color(150, 150, 150));
                        label.setAlignmentX(Component.CENTER_ALIGNMENT);
                        label.setBorder(new EmptyBorder(80, 20, 80, 20));
                        chatListContainer.add(label);
                    } else {
                        for (Map<String, Object> chat : chats) {
                            String friendUsername = (String) chat.get("friend_username");
                            String friendName = (String) chat.get("friend_name");
                            String displayName = (friendName != null && !friendName.isEmpty()) ? 
                                                 friendName : friendUsername;
                            
                            String lastMessage = (String) chat.get("last_message");
                            java.sql.Timestamp sentAt = (java.sql.Timestamp) chat.get("sent_at");
                            int unreadCount = (int) chat.get("unread_count");
                            
                            // Format time
                            String timeStr = formatTime(sentAt);
                            
                            // Add chat item with online status (luôn online vì đang ở tab Online)
                            ChatItemPanel item = new ChatItemPanel(friendUsername, displayName, lastMessage, timeStr, true, unreadCount, sentAt);
                            item.addMouseListener(new java.awt.event.MouseAdapter() {
                                @Override
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    mainFrame.openChat(friendUsername);
                                }
                            });
                            chatListContainer.add(item);
                            chatItems.put(friendUsername, item);
                        }
                    }
                    
                    chatListContainer.revalidate();
                    chatListContainer.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    // ==================== TÌM KIẾM TOÀN BỘ LỊCH SỬ ====================
    
    /**
     * Hiển thị dialog tìm kiếm toàn bộ lịch sử chat với tất cả mọi người
     */
    private void showGlobalSearchDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Tìm kiếm toàn bộ lịch sử", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Tìm kiếm trong toàn bộ lịch sử chat");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Nhập từ khóa tìm kiếm...");
        
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setPreferredSize(new Dimension(110, 40));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(topPanel, BorderLayout.NORTH);
        
        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Kết quả tìm kiếm"));
        resultsScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        searchPanel.add(resultsScroll, BorderLayout.CENTER);
        
        // Stats label
        JLabel statsLabel = new JLabel("Nhập từ khóa để tìm kiếm trong toàn bộ lịch sử chat");
        statsLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.ITALIC, 12));
        statsLabel.setForeground(new Color(120, 120, 120));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        searchPanel.add(statsLabel, BorderLayout.SOUTH);
        
        // Search action
        Runnable doSearch = () -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập từ khóa!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (keyword.length() < 2) {
                JOptionPane.showMessageDialog(dialog, "Từ khóa phải có ít nhất 2 ký tự!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            resultsPanel.removeAll();
            searchButton.setEnabled(false);
            searchButton.setText("Đang tìm...");
            statsLabel.setText("Đang tìm kiếm...");
            
            SwingWorker<java.util.List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
                @Override
                protected java.util.List<Map<String, Object>> doInBackground() {
                    return userService.searchAllChatHistory(mainFrame.getUsername(), keyword);
                }
                
                @Override
                protected void done() {
                    try {
                        java.util.List<Map<String, Object>> results = get();
                        
                        if (results == null || results.isEmpty()) {
                            JLabel label = new JLabel("<html><center>😔 Không tìm thấy kết quả nào<br>Thử từ khóa khác</center></html>");
                            label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
                            label.setForeground(new Color(150, 150, 150));
                            label.setAlignmentX(Component.CENTER_ALIGNMENT);
                            label.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
                            resultsPanel.add(label);
                            statsLabel.setText("Không tìm thấy kết quả nào");
                        } else {
                            statsLabel.setText("Tìm thấy " + results.size() + " tin nhắn");
                            
                            for (Map<String, Object> result : results) {
                                String friendUsername = (String) result.get("friend_username");
                                String senderUsername = (String) result.get("sender_username");
                                String content = (String) result.get("content");
                                java.sql.Timestamp sentAt = (java.sql.Timestamp) result.get("sent_at");
                                int messageId = result.get("message_id") != null ? (int) result.get("message_id") : 0;
                                
                                // Xác định người chat
                                boolean isSentByMe = senderUsername.equals(mainFrame.getUsername());
                                String chatWith = friendUsername;
                                
                                JPanel resultItem = new JPanel(new BorderLayout(10, 5));
                                resultItem.setBackground(Color.WHITE);
                                resultItem.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
                                ));
                                resultItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
                                resultItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                
                                // Header: Chat với ai + thời gian
                                String timeStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(sentAt);
                                JLabel headerLabel = new JLabel("Chat với " + chatWith + " - " + timeStr);
                                headerLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 12));
                                headerLabel.setForeground(PRIMARY_COLOR);
                                
                                // Content với highlight
                                String displayContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                                JLabel contentLabel = new JLabel("<html>" + highlightKeyword(displayContent, keyword) + "</html>");
                                contentLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
                                
                                // Sender info
                                JLabel senderLabel = new JLabel(isSentByMe ? "Bạn đã gửi" : senderUsername + " đã gửi");
                                senderLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.ITALIC, 11));
                                senderLabel.setForeground(new Color(120, 120, 120));
                                
                                // Arrow icon
                                JLabel arrowLabel = new JLabel("→");
                                arrowLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
                                arrowLabel.setForeground(PRIMARY_COLOR);
                                
                                JPanel textPanel = new JPanel();
                                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                                textPanel.setOpaque(false);
                                textPanel.add(headerLabel);
                                textPanel.add(Box.createVerticalStrut(3));
                                textPanel.add(contentLabel);
                                textPanel.add(Box.createVerticalStrut(2));
                                textPanel.add(senderLabel);
                                
                                resultItem.add(textPanel, BorderLayout.CENTER);
                                resultItem.add(arrowLabel, BorderLayout.EAST);
                                
                                // Click để mở chat và scroll đến tin nhắn
                                final String targetUser = chatWith;
                                final int msgId = messageId;
                                resultItem.addMouseListener(new java.awt.event.MouseAdapter() {
                                    @Override
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        dialog.dispose();
                                        // Mở chat với người đó
                                        mainFrame.openChat(targetUser);
                                        // Scroll đến tin nhắn (delay để chat load xong)
                                        if (msgId > 0) {
                                            javax.swing.Timer timer = new javax.swing.Timer(500, evt -> {
                                                mainFrame.scrollToMessageInChat(msgId);
                                            });
                                            timer.setRepeats(false);
                                            timer.start();
                                        }
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
                        statsLabel.setText("Có lỗi xảy ra!");
                        searchButton.setEnabled(true);
                        searchButton.setText("Tìm kiếm");
                    }
                }
            };
            
            worker.execute();
        };
        
        searchButton.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run()); // Enter to search
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(searchPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    /**
     * Highlight keyword trong text
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null) return text;
        return text.replaceAll("(?i)(" + java.util.regex.Pattern.quote(keyword) + ")", 
            "<span style='background-color: #FFFF00; font-weight: bold;'>$1</span>");
    }
}
