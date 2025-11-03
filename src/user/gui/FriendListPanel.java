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
        JLabel titleLabel = new JLabel("Bạn bè (97)");
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
        
        JTextField placeholderField = new JTextField("Tìm bạn");
        placeholderField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholderField.setBorder(null);
        placeholderField.setBackground(new Color(245, 245, 245));
        placeholderField.setForeground(new Color(150, 150, 150));
        
        searchInputPanel.add(searchIcon, BorderLayout.WEST);
        searchInputPanel.add(placeholderField, BorderLayout.CENTER);
        
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
                    List<Map<String, Object>> friends = get();
                    
                    if (friends == null || friends.isEmpty()) {
                        showEmptyMessage();
                    } else {
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
                            
                            // Add friend item
                            friendsPanel.add(createFriendItem(friend));
                        }
                    }
                    
                    friendsPanel.revalidate();
                    friendsPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyMessage();
                }
            }
        };
        
        worker.execute();
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
    
    private JPanel createFriendItem(Map<String, Object> friend) {
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
}
