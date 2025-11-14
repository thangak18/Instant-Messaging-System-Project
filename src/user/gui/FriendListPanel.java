package user.gui;

import user.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Friend List Panel - Danh sách bạn bè theo phong cách Zalo
 */
public class FriendListPanel extends JPanel {
    
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_PRIMARY = new Color(50, 50, 50);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private JPanel friendsPanel;
    private JTextField searchField;
    private List<Map<String, Object>> allFriends; // Cache tất cả bạn bè
    private JLabel titleLabel; // Để update số lượng bạn bè
    
    public FriendListPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        initializeUI();
        loadFriends();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Friends list (scrollable)
        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBackground(BG_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        titleLabel = new JLabel("Bạn bè");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(WHITE);
        searchPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setBackground(new Color(245, 245, 245));
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 8));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.setBackground(new Color(245, 245, 245));
        searchInputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        searchField = new JTextField("Tìm bạn");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(null);
        searchField.setBackground(new Color(245, 245, 245));
        searchField.setForeground(new Color(150, 150, 150));
        
        // Placeholder behavior
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Tìm bạn")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Tìm bạn");
                    searchField.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        // Search on key type
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterFriends();
            }
        });
        
        searchInputPanel.add(searchIcon, BorderLayout.WEST);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        
        // Sort and Filter buttons
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolsPanel.setBackground(WHITE);
        toolsPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        JButton sortButton = createToolButton("⬍ Tên (A-Z)");
        JButton filterButton = createToolButton("≡ Tất cả");
        
        toolsPanel.add(sortButton);
        toolsPanel.add(filterButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(WHITE);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(toolsPanel, BorderLayout.CENTER);
        
        headerPanel.add(bottomPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JButton createToolButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadFriends() {
        friendsPanel.removeAll();
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return userService.getFriendsList(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    allFriends = get();
                    
                    if (allFriends != null && !allFriends.isEmpty()) {
                        titleLabel.setText("Bạn bè (" + allFriends.size() + ")");
                    } else {
                        titleLabel.setText("Bạn bè (0)");
                    }
                    
                    displayFriends(allFriends);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyMessage();
                }
            }
        };
        
        worker.execute();
    }
    
    private void filterFriends() {
        if (allFriends == null) return;
        
        String query = searchField.getText().toLowerCase().trim();
        if (query.equals("tìm bạn")) {
            displayFriends(allFriends);
            return;
        }
        
        List<Map<String, Object>> filtered = new java.util.ArrayList<>();
        for (Map<String, Object> friend : allFriends) {
            String fullName = (String) friend.get("full_name");
            String username = (String) friend.get("username");
            
            String searchText = (fullName != null ? fullName : username).toLowerCase();
            if (searchText.contains(query)) {
                filtered.add(friend);
            }
        }
        
        displayFriends(filtered);
    }
    
    private void displayFriends(List<Map<String, Object>> friends) {
        friendsPanel.removeAll();
        
        if (friends == null || friends.isEmpty()) {
            showEmptyMessage();
        } else {
            // Get online users
            List<String> onlineUsers = mainFrame.getSocketClient() != null 
                ? mainFrame.getSocketClient().getOnlineUsers() 
                : new java.util.ArrayList<>();
            
            // Group by first letter
            char currentLetter = '\0';
            
            for (Map<String, Object> friend : friends) {
                String name = (String) friend.get("full_name");
                if (name == null || name.isEmpty()) {
                    name = (String) friend.get("username");
                }
                
                char firstLetter = Character.toUpperCase(name.charAt(0));
                
                // Add section header
                if (firstLetter != currentLetter) {
                    currentLetter = firstLetter;
                    friendsPanel.add(createSectionHeader(String.valueOf(currentLetter)));
                }
                
                // Check online status
                String username = (String) friend.get("username");
                boolean isOnline = onlineUsers.contains(username);
                
                // Add friend item
                friendsPanel.add(createFriendItem(friend, isOnline));
            }
        }
        
        friendsPanel.revalidate();
        friendsPanel.repaint();
    }
    
    private JPanel createSectionHeader(String letter) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(12, 15, 8, 15));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel label = new JLabel(letter);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_SECONDARY);
        
        panel.add(label, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createFriendItem(Map<String, Object> friend, boolean isOnline) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String username = (String) friend.get("username");
        String fullName = (String) friend.get("full_name");
        String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
        
        // Avatar with online status
        JPanel avatarPanel = new JPanel(null);
        avatarPanel.setPreferredSize(new Dimension(50, 50));
        avatarPanel.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        avatarLabel.setBounds(0, 0, 50, 50);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarPanel.add(avatarLabel);
        
        // Online status indicator
        if (isOnline) {
            JPanel statusDot = new JPanel();
            statusDot.setBackground(new Color(68, 214, 44)); // Green
            statusDot.setBounds(36, 36, 14, 14);
            statusDot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WHITE, 2),
                BorderFactory.createEmptyBorder()
            ));
            avatarPanel.add(statusDot);
        }
        
        // Name and status
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameLabel.setForeground(TEXT_PRIMARY);
        
        JLabel statusLabel = new JLabel(isOnline ? "🟢 Đang hoạt động" : "⚪ Không hoạt động");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(statusLabel);
        
        // Menu button
        JButton menuButton = new JButton("⋮");
        menuButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuButton.setForeground(TEXT_SECONDARY);
        menuButton.setPreferredSize(new Dimension(30, 30));
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Menu popup
        menuButton.addActionListener(e -> {
            showFriendMenu(menuButton, username, displayName);
        });
        
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(menuButton, BorderLayout.EAST);
        
        // Hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(245, 247, 250));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(WHITE);
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Open chat with friend
                mainFrame.openChat(username);
                // Switch to chat tab
                SwingUtilities.invokeLater(() -> {
                    mainFrame.switchToTab("chat");
                });
            }
        });
        
        return panel;
    }
    
    private void showFriendMenu(JComponent source, String username, String displayName) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        
        // Nhắn tin
        JMenuItem chatItem = createMenuItem("💬 Nhắn tin", null);
        chatItem.addActionListener(e -> {
            mainFrame.openChat(username);
            mainFrame.switchToTab("chat");
        });
        
        // Xem trang cá nhân
        JMenuItem profileItem = createMenuItem("👤 Xem trang cá nhân", null);
        profileItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Chức năng xem trang cá nhân đang được phát triển",
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        menu.add(chatItem);
        menu.add(profileItem);
        menu.addSeparator();
        
        // Huỷ kết bạn
        JMenuItem unfriendItem = createMenuItem("💔 Huỷ kết bạn", new Color(220, 53, 69));
        unfriendItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn huỷ kết bạn với " + displayName + "?",
                "Xác nhận huỷ kết bạn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                handleUnfriend(username, displayName);
            }
        });
        
        // Block và huỷ kết bạn
        JMenuItem blockItem = createMenuItem("🚫 Chặn người này", new Color(220, 53, 69));
        blockItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Bạn có chắc muốn chặn " + displayName + "?<br>" +
                "Hành động này sẽ:<br>" +
                "• Huỷ kết bạn với " + displayName + "<br>" +
                "• Người này sẽ không thể gửi lời mời kết bạn cho bạn nữa</html>",
                "Xác nhận chặn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                handleBlockUser(username, displayName);
            }
        });
        
        menu.add(unfriendItem);
        menu.add(blockItem);
        
        menu.show(source, source.getWidth() - menu.getPreferredSize().width, source.getHeight());
    }
    
    private JMenuItem createMenuItem(String text, Color color) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.setForeground(color != null ? color : TEXT_PRIMARY);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return item;
    }
    
    private void handleUnfriend(String username, String displayName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return userService.unfriend(mainFrame.getUsername(), username);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        System.out.println("💔 Đã hủy kết bạn với: " + username);
                        
                        // Gửi notification qua Socket
                        mainFrame.sendUnfriendNotification(username);
                        
                        JOptionPane.showMessageDialog(FriendListPanel.this,
                            "Đã huỷ kết bạn với " + displayName,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload friends list và chat list
                        loadFriends();
                        mainFrame.refreshChatAndFriendList();
                    } else {
                        JOptionPane.showMessageDialog(FriendListPanel.this,
                            "Không thể huỷ kết bạn. Vui lòng thử lại.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(FriendListPanel.this,
                        "Đã xảy ra lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleBlockUser(String username, String displayName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return userService.blockUser(mainFrame.getUsername(), username);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        System.out.println("🚫 Đã chặn: " + username);
                        
                        // Gửi notification qua Socket
                        mainFrame.sendBlockNotification(username);
                        
                        JOptionPane.showMessageDialog(FriendListPanel.this,
                            "Đã chặn " + displayName,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload friends list và chat list
                        loadFriends();
                        mainFrame.refreshChatAndFriendList();
                    } else {
                        JOptionPane.showMessageDialog(FriendListPanel.this,
                            "Không thể chặn người dùng. Vui lòng thử lại.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(FriendListPanel.this,
                        "Đã xảy ra lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private JPanel createFriendItemOld(Map<String, Object> friend) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String username = (String) friend.get("username");
        String fullName = (String) friend.get("full_name");
        String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
        
        // Avatar
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Name
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameLabel.setForeground(TEXT_PRIMARY);
        
        // Menu button
        JButton menuButton = new JButton("⋮");
        menuButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuButton.setForeground(TEXT_SECONDARY);
        menuButton.setPreferredSize(new Dimension(30, 30));
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(avatarLabel, BorderLayout.WEST);
        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(menuButton, BorderLayout.EAST);
        
        // Hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(245, 247, 250));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(WHITE);
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // TODO: Open chat with friend
                System.out.println("Open chat with: " + username);
            }
        });
        
        return panel;
    }
    
    private void showEmptyMessage() {
        friendsPanel.removeAll();
        
        JLabel label = new JLabel("<html><center>😔<br><br>Chưa có bạn bè nào</center></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(80, 20, 80, 20));
        
        friendsPanel.add(label);
        friendsPanel.revalidate();
        friendsPanel.repaint();
    }
    
    /**
     * Refresh online status real-time khi có user login/logout
     */
    public void refreshOnlineStatus() {
        if (allFriends != null && !allFriends.isEmpty()) {
            displayFriends(allFriends);
        }
    }
    
    /**
     * Refresh friend list (khi accept friend request)
     */
    public void refreshFriendList() {
        System.out.println("🔄 Refreshing friend list...");
        loadFriends();
    }
}
