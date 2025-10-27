package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính hiện đại - Phong cách Zalo
 * Sidebar navigation + Main content area
 */
public class UserMainFrame extends JFrame {
    // Colors - Zalo Style
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color SIDEBAR_COLOR = new Color(255, 255, 255);
    private static final Color SIDEBAR_HOVER = new Color(240, 242, 245);
    private static final Color SELECTED_COLOR = new Color(230, 240, 255);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    private static final Color ONLINE_COLOR = new Color(67, 160, 71);
    
    // Components
    private JPanel sidebarPanel, headerPanel, contentPanel;
    private JLabel userAvatarLabel, userNameLabel, userStatusLabel;
    private JTextField searchField;
    private JButton messagesBtn, contactsBtn, groupsBtn, settingsBtn;
    private JPanel mainContentArea;
    
    // Current user info
    private String currentUsername;
    private String currentStatus = "Đang hoạt động";
    
    // Constructor with username (called from LoginFrame)
    public UserMainFrame(String username) {
        this.currentUsername = username;
        initializeComponents();
        setupLayout();
        applyModernStyle();
    }
    
    // Constructor without username (for testing)
    public UserMainFrame() {
        this("Demo User"); // Default username
    }
    
    private void initializeComponents() {
        setTitle("InstantChat - Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Sidebar components
        createSidebarComponents();
        
        // Header components
        createHeaderComponents();
        
        // Main content area
        mainContentArea = new JPanel(new BorderLayout());
        mainContentArea.setBackground(Color.WHITE);
        mainContentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'>"
                + "<h1 style='color: #0084FF;'>Chào mừng đến với InstantChat!</h1>"
                + "<p style='color: #999; font-size: 14px;'>Chọn một cuộc trò chuyện để bắt đầu</p>"
                + "</div></html>", JLabel.CENTER);
        mainContentArea.add(welcomeLabel, BorderLayout.CENTER);
    }
    
    private void createSidebarComponents() {
        // User avatar (circle)
        userAvatarLabel = new JLabel("👤", JLabel.CENTER);
        userAvatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        userAvatarLabel.setPreferredSize(new Dimension(60, 60));
        userAvatarLabel.setOpaque(true);
        userAvatarLabel.setBackground(PRIMARY_COLOR);
        userAvatarLabel.setForeground(Color.WHITE);
        
        // User info
        userNameLabel = new JLabel(currentUsername);
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userNameLabel.setForeground(TEXT_COLOR);
        
        userStatusLabel = new JLabel("● " + currentStatus);
        userStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userStatusLabel.setForeground(ONLINE_COLOR);
        
        // Navigation buttons
        messagesBtn = createSidebarButton("💬", "Tin nhắn", true);
        contactsBtn = createSidebarButton("👥", "Danh bạ", false);
        groupsBtn = createSidebarButton("👨‍👩‍👧‍👦", "Nhóm", false);
        settingsBtn = createSidebarButton("⚙️", "Cài đặt", false);
        
        // Button actions
        messagesBtn.addActionListener(e -> switchTab(messagesBtn, "Tin nhắn"));
        contactsBtn.addActionListener(e -> switchTab(contactsBtn, "Danh bạ"));
        groupsBtn.addActionListener(e -> switchTab(groupsBtn, "Nhóm"));
        settingsBtn.addActionListener(e -> switchTab(settingsBtn, "Cài đặt"));
    }
    
    private void createHeaderComponents() {
        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setText("🔍  Tìm kiếm tin nhắn, bạn bè...");
        searchField.setForeground(new Color(153, 153, 153));
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Placeholder effect
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("🔍  Tìm kiếm tin nhắn, bạn bè...")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(new Color(153, 153, 153));
                    searchField.setText("🔍  Tìm kiếm tin nhắn, bạn bè...");
                }
            }
        });
    }
    
    private JButton createSidebarButton(String icon, String text, boolean selected) {
        JButton button = new JButton("<html><div style='text-align: center;'>"
                + "<div style='font-size: 24px; margin-bottom: 5px;'>" + icon + "</div>"
                + "<div style='font-size: 11px;'>" + text + "</div>"
                + "</div></html>");
        
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(selected ? PRIMARY_COLOR : TEXT_COLOR);
        button.setBackground(selected ? SELECTED_COLOR : SIDEBAR_COLOR);
        button.setPreferredSize(new Dimension(80, 80));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(SELECTED_COLOR)) {
                    button.setBackground(SIDEBAR_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(SELECTED_COLOR)) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // === SIDEBAR (Left) ===
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(90, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        
        // User profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(SIDEBAR_COLOR);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        profilePanel.setMaximumSize(new Dimension(90, 120));
        
        userAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(userAvatarLabel);
        profilePanel.add(Box.createVerticalStrut(10));
        
        sidebarPanel.add(profilePanel);
        sidebarPanel.add(createSeparator());
        
        // Navigation buttons
        messagesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contactsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        groupsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        sidebarPanel.add(messagesBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(contactsBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(groupsBtn);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(settingsBtn);
        sidebarPanel.add(Box.createVerticalStrut(15));
        
        // === CONTENT PANEL (Right) ===
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Header with search
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel headerTitle = new JLabel("Tin nhắn");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(TEXT_COLOR);
        
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerLeft.setOpaque(false);
        headerLeft.add(headerTitle);
        
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerRight.setOpaque(false);
        headerRight.add(searchField);
        
        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentArea, BorderLayout.CENTER);
        
        // Add to frame
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void applyModernStyle() {
        // Apply modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel
        }
    }
    
    private void switchTab(JButton selectedButton, String tabName) {
        // Reset all buttons
        messagesBtn.setBackground(SIDEBAR_COLOR);
        messagesBtn.setForeground(TEXT_COLOR);
        contactsBtn.setBackground(SIDEBAR_COLOR);
        contactsBtn.setForeground(TEXT_COLOR);
        groupsBtn.setBackground(SIDEBAR_COLOR);
        groupsBtn.setForeground(TEXT_COLOR);
        settingsBtn.setBackground(SIDEBAR_COLOR);
        settingsBtn.setForeground(TEXT_COLOR);
        
        // Highlight selected
        selectedButton.setBackground(SELECTED_COLOR);
        selectedButton.setForeground(PRIMARY_COLOR);
        
        // Update header title
        Component[] components = ((JPanel) headerPanel.getComponent(0)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText(tabName);
        }
        
        // Switch content based on tab
        mainContentArea.removeAll();
        
        switch (tabName) {
            case "Tin nhắn":
                loadMessagesView();
                break;
            case "Danh bạ":
                loadContactsView();
                break;
            case "Nhóm":
                loadGroupsView();
                break;
            case "Cài đặt":
                loadSettingsView();
                break;
        }
        
        mainContentArea.revalidate();
        mainContentArea.repaint();
    }
    
    private void loadMessagesView() {
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.setBackground(Color.WHITE);
        
        // Conversation list
        JPanel conversationList = new JPanel();
        conversationList.setLayout(new BoxLayout(conversationList, BoxLayout.Y_AXIS));
        conversationList.setBackground(Color.WHITE);
        
        // Sample conversations
        conversationList.add(createConversationItem("Nguyễn Văn B", "Bạn: Ok nhé!", "10:30", true));
        conversationList.add(createConversationItem("Trần Thị C", "Hẹn gặp lại!", "Hôm qua", false));
        conversationList.add(createConversationItem("Nhóm Java Dev", "Nguyễn D: Đã hiểu rồi", "2 ngày trước", true));
        conversationList.add(createConversationItem("Lê Văn E", "Thanks bạn!", "1 tuần trước", false));
        
        JScrollPane scrollPane = new JScrollPane(conversationList);
        scrollPane.setBorder(null);
        messagesPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainContentArea.add(messagesPanel, BorderLayout.CENTER);
    }
    
    private JPanel createConversationItem(String name, String lastMessage, String time, boolean hasUnread) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Avatar
        JLabel avatar = new JLabel("👤", JLabel.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        avatar.setPreferredSize(new Dimension(50, 50));
        avatar.setOpaque(true);
        avatar.setBackground(PRIMARY_COLOR);
        avatar.setForeground(Color.WHITE);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        
        JLabel messageLabel = new JLabel(lastMessage);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(120, 120, 120));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(messageLabel);
        
        contentPanel.add(textPanel, BorderLayout.CENTER);
        
        // Time and badge
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(150, 150, 150));
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPanel.add(timeLabel);
        
        if (hasUnread) {
            rightPanel.add(Box.createVerticalStrut(5));
            JLabel badge = new JLabel("  ");
            badge.setOpaque(true);
            badge.setBackground(PRIMARY_COLOR);
            badge.setPreferredSize(new Dimension(8, 8));
            badge.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(badge);
        }
        
        item.add(avatar, BorderLayout.WEST);
        item.add(contentPanel, BorderLayout.CENTER);
        item.add(rightPanel, BorderLayout.EAST);
        
        // Hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(Color.WHITE);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Open chat window
                openPrivateChat(name);
            }
        });
        
        return item;
    }
    
    private void loadContactsView() {
        JLabel label = new JLabel("<html><div style='text-align: center;'>"
                + "<h2>Danh sách bạn bè</h2>"
                + "<p style='color: #999;'>Chức năng đang phát triển...</p>"
                + "</div></html>", JLabel.CENTER);
        mainContentArea.add(label, BorderLayout.CENTER);
    }
    
    private void loadGroupsView() {
        JLabel label = new JLabel("<html><div style='text-align: center;'>"
                + "<h2>Danh sách nhóm</h2>"
                + "<p style='color: #999;'>Chức năng đang phát triển...</p>"
                + "</div></html>", JLabel.CENTER);
        mainContentArea.add(label, BorderLayout.CENTER);
    }
    
    private void loadSettingsView() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Cài đặt tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        settingsPanel.add(titleLabel);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(createSettingItem("Thông tin cá nhân", "Cập nhật thông tin của bạn"));
        settingsPanel.add(createSettingItem("Đổi mật khẩu", "Thay đổi mật khẩu bảo mật"));
        settingsPanel.add(createSettingItem("Quyền riêng tư", "Quản lý quyền riêng tư"));
        settingsPanel.add(createSettingItem("Thông báo", "Cài đặt thông báo"));
        settingsPanel.add(Box.createVerticalStrut(20));
        
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(244, 67, 54));
        logoutBtn.setPreferredSize(new Dimension(150, 40));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());
        
        settingsPanel.add(logoutBtn);
        
        mainContentArea.add(settingsPanel, BorderLayout.NORTH);
    }
    
    private JPanel createSettingItem(String title, String description) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descLabel);
        
        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        arrow.setForeground(new Color(180, 180, 180));
        
        item.add(textPanel, BorderLayout.CENTER);
        item.add(arrow, BorderLayout.EAST);
        
        // Hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(Color.WHITE);
            }
        });
        
        return item;
    }
    
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(220, 220, 220));
        return separator;
    }
    
    private void openPrivateChat(String userName) {
        // Open chat in a new window or replace content
        mainContentArea.removeAll();
        
        ChatFrame chatFrame = new ChatFrame(userName);
        mainContentArea.setLayout(new BorderLayout());
        mainContentArea.add(chatFrame.getContentPane(), BorderLayout.CENTER);
        
        mainContentArea.revalidate();
        mainContentArea.repaint();
    }
    
    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Đóng UserMainFrame
            this.dispose();
            
            // Mở lại LoginFrame
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}
