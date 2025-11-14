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
    private static final int PANEL_WIDTH = 350;
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private JTextField searchField;
    private JPanel chatListContainer;
    private Map<String, ChatItemPanel> chatItems = new HashMap<>();
    
    public ChatListPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        initializeUI();
        loadRecentChats(); // Load data thật từ database
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
        
        // Action buttons panel (Add Friend + Create Group)
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionButtonsPanel.setOpaque(false);
        
        JButton addFriendButton = createIconButton("icons/add-friend.png", "Thêm bạn", 24);
        JButton createGroupButton = createIconButton("icons/create-group.png", "Tạo nhóm", 24);
        
        // Click handler for Add Friend
        addFriendButton.addActionListener(e -> {
            AddFriendDialog dialog = new AddFriendDialog(mainFrame, mainFrame.getUsername());
            dialog.setVisible(true);
        });
        
        actionButtonsPanel.add(addFriendButton);
        actionButtonsPanel.add(createGroupButton);
        
        headerPanel.add(searchBarPanel, BorderLayout.CENTER);
        headerPanel.add(actionButtonsPanel, BorderLayout.EAST);
        
        // Tabs panel
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        tabsPanel.setBackground(Color.WHITE);
        tabsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        JLabel allTab = createTab("Tất cả", true);
        JLabel unreadTab = createTab("Chưa đọc", false);
        
        tabsPanel.add(allTab);
        tabsPanel.add(unreadTab);
        
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
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 38, 8, 10)
        ));
        searchField.setBackground(new Color(245, 245, 245));
        
        // Icon panel overlay
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        iconPanel.setOpaque(false);
        
        try {
            ImageIcon searchIcon = new ImageIcon("icons/search.png");
            Image scaledImage = searchIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconPanel.add(iconLabel);
        } catch (Exception e) {
            // Fallback emoji
            JLabel iconLabel = new JLabel("🔍");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
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
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        tab.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
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
                            
                            // Add chat item
                            addChatItem(friendUsername, displayName, lastMessage, timeStr, false, unreadCount);
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
        JLabel label = new JLabel("<html><center>💬<br><br>Chưa có cuộc trò chuyện nào<br>Hãy thêm bạn bè và bắt đầu chat!</center></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(150, 150, 150));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(80, 20, 80, 20));
        
        chatListContainer.add(label);
        chatListContainer.revalidate();
        chatListContainer.repaint();
    }
    
    
    private void addChatItem(String username, String displayName, String lastMessage, String time, boolean online, int unreadCount) {
        ChatItemPanel item = new ChatItemPanel(username, displayName, lastMessage, time, online, unreadCount);
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
            if (!chatItems.containsKey(sender)) {
                // Thêm chat item mới
                addChatItem(sender, sender, content, "Vừa xong", true, 1);
            } else {
                // Cập nhật chat item có sẵn
                ChatItemPanel item = chatItems.get(sender);
                item.updateLastMessage(content, "Vừa xong");
                
                // Move to top
                chatListContainer.remove(item);
                chatListContainer.add(item, 0);
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
        
        public ChatItemPanel(String username, String displayName, String lastMessage, String time, boolean online, int unreadCount) {
            this.username = username;
            setLayout(new BorderLayout(10, 5));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(PANEL_WIDTH, 80));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Avatar panel
            JPanel avatarPanel = new JPanel(null);
            avatarPanel.setPreferredSize(new Dimension(50, 50));
            avatarPanel.setOpaque(false);
            
            JLabel avatar = new JLabel("👤");
            avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            avatar.setBounds(0, 0, 50, 50);
            avatarPanel.add(avatar);
            
            // Online indicator
            if (online) {
                JLabel onlineDot = new JLabel("●");
                onlineDot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                onlineDot.setForeground(new Color(67, 220, 96));
                onlineDot.setBounds(35, 35, 15, 15);
                avatarPanel.add(onlineDot);
            }
            
            // Info panel
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setOpaque(false);
            
            // Name + time
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            
            nameLabel = new JLabel(displayName);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            
            timeLabel = new JLabel(time);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timeLabel.setForeground(new Color(120, 120, 120));
            
            topPanel.add(nameLabel, BorderLayout.WEST);
            topPanel.add(timeLabel, BorderLayout.EAST);
            
            // Message + badge
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            
            messageLabel = new JLabel(lastMessage);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            messageLabel.setForeground(new Color(100, 100, 100));
            
            if (unreadCount > 0) {
                badgeLabel = new JLabel(String.valueOf(unreadCount));
                badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badgeLabel.setForeground(Color.WHITE);
                badgeLabel.setBackground(new Color(255, 59, 48));
                badgeLabel.setOpaque(true);
                badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                badgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                bottomPanel.add(badgeLabel, BorderLayout.EAST);
            }
            
            bottomPanel.add(messageLabel, BorderLayout.WEST);
            
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
            messageLabel.setText(message);
            timeLabel.setText(time);
        }
    }
    
    /**
     * Refresh chat list (khi có bạn mới hoặc accept friend request)
     */
    public void refreshChatList() {
        System.out.println("🔄 Refreshing chat list...");
        loadRecentChats();
    }
}
