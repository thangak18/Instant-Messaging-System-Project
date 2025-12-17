package user.gui;

import user.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog thêm bạn - Tìm kiếm user và gửi lời mời kết bạn
 */
public class AddFriendDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(250, 250, 250);

    private ZaloMainFrame mainFrame;
    private String currentUsername;

    private JTextField searchField;
    private JPanel resultsPanel;
    private UserService userService;

    public AddFriendDialog(ZaloMainFrame parent, String currentUsername) {
        super(parent, "Thêm bạn", false); // Non-modal
        this.mainFrame = parent;
        this.currentUsername = currentUsername;
        this.userService = new UserService();

        initializeUI();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setSize(400, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = createHeader();

        // Search panel
        JPanel searchPanel = createSearchPanel();

        // Results panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Show helper message
        showHelperMessage();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Thêm bạn");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 20));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(closeButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Country code + phone number (like Zalo)
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(Color.WHITE);

        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 12, 10, 12)));

        // Placeholder behavior
        searchField.setForeground(new Color(150, 150, 150));
        searchField.setText("Nhập tên đăng nhập hoặc email");

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Nhập tên đăng nhập hoặc email")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(new Color(150, 150, 150));
                    searchField.setText("Nhập tên đăng nhập hoặc email");
                }
            }
        });

        // Real-time search on typing
        searchField.addActionListener(e -> performSearch());

        inputPanel.add(searchField, BorderLayout.CENTER);

        // Search button
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true); // Required for macOS
        searchButton.setContentAreaFilled(true); // Ensure background is painted
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(100, 42));
        searchButton.addActionListener(e -> performSearch());

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);

        // Helper text
        JLabel helperLabel = new JLabel("Có thể bạn quen");
        helperLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
        helperLabel.setForeground(new Color(120, 120, 120));
        helperLabel.setIcon(new ImageIcon()); // Placeholder for icon
        panel.add(helperLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void showHelperMessage() {
        resultsPanel.removeAll();

        JLabel messageLabel = new JLabel("<html><center>" +
                "<b>[?]</b><br><br>" +
                "Nhập tên đăng nhập hoặc email<br>" +
                "để tìm kiếm bạn bè" +
                "</center></html>");
        messageLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        messageLabel.setForeground(new Color(150, 150, 150));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(new EmptyBorder(100, 20, 100, 20));

        resultsPanel.add(messageLabel);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void performSearch() {
        String query = searchField.getText().trim();

        if (query.isEmpty() || query.equals("Nhập tên đăng nhập hoặc email")) {
            showHelperMessage();
            return;
        }

        // Clear results
        resultsPanel.removeAll();

        // Show loading
        JLabel loadingLabel = new JLabel("Đang tìm kiếm...");
        loadingLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        loadingLabel.setForeground(new Color(120, 120, 120));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(loadingLabel);
        resultsPanel.revalidate();
        resultsPanel.repaint();

        // Search in background
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return userService.searchUsers(query, currentUsername);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> results = get();
                    displayResults(results);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Lỗi khi tìm kiếm: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void displayResults(List<Map<String, Object>> results) {
        resultsPanel.removeAll();

        if (results == null || results.isEmpty()) {
            JLabel noResultLabel = new JLabel("<html><center>" +
                    "😔<br><br>" +
                    "Không tìm thấy kết quả" +
                    "</center></html>");
            noResultLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
            noResultLabel.setForeground(new Color(150, 150, 150));
            noResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noResultLabel.setBorder(new EmptyBorder(100, 20, 100, 20));
            resultsPanel.add(noResultLabel);
        } else {
            for (Map<String, Object> user : results) {
                UserResultPanel userPanel = new UserResultPanel(user);
                resultsPanel.add(userPanel);
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void showError(String message) {
        resultsPanel.removeAll();

        JLabel errorLabel = new JLabel("<html><center>❌<br><br>" + message + "</center></html>");
        errorLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        errorLabel.setForeground(new Color(255, 59, 48));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setBorder(new EmptyBorder(100, 20, 100, 20));

        resultsPanel.add(errorLabel);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    /**
     * Panel hiển thị kết quả user
     */
    private class UserResultPanel extends JPanel {
        public UserResultPanel(Map<String, Object> userData) {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            // Avatar
            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(50, 50));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            try {
                ImageIcon icon = new ImageIcon("icons/user.png");
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                avatarLabel.setText("[A]");
                avatarLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
                avatarLabel.setForeground(new Color(0, 132, 255));
            }

            // User info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            String username = (String) userData.get("username");
            String fullName = (String) userData.get("full_name");
            String email = (String) userData.get("email");

            JLabel nameLabel = new JLabel(fullName != null ? fullName : username);
            nameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 15));

            JLabel usernameLabel = new JLabel("@" + username);
            usernameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            usernameLabel.setForeground(new Color(120, 120, 120));

            infoPanel.add(nameLabel);
            infoPanel.add(usernameLabel);

            // Check friendship status
            String friendshipStatus = userService.getFriendshipStatus(currentUsername, username);

            // Add friend button - Thay đổi text dựa vào status
            JButton addButton = new JButton();
            addButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
            addButton.setFocusPainted(false);
            addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            switch (friendshipStatus) {
                case "friends":
                    addButton.setText("Bạn bè");
                    addButton.setForeground(new Color(67, 220, 96));
                    addButton.setBackground(Color.WHITE);
                    addButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(67, 220, 96), 1, true),
                            new EmptyBorder(6, 20, 6, 20)));
                    addButton.setEnabled(false);
                    break;

                case "pending_sent":
                    addButton.setText("Đã gửi");
                    addButton.setForeground(new Color(120, 120, 120));
                    addButton.setBackground(Color.WHITE);
                    addButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                            new EmptyBorder(6, 20, 6, 20)));
                    addButton.setEnabled(false);
                    break;

                case "pending_received":
                    addButton.setText("Phản hồi");
                    addButton.setForeground(PRIMARY_COLOR);
                    addButton.setBackground(Color.WHITE);
                    addButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                            new EmptyBorder(6, 20, 6, 20)));
                    addButton.addActionListener(e -> {
                        // Chuyển đến friend request panel
                        mainFrame.showContactPanel();
                        mainFrame.showContactContent("FRIEND_REQUESTS");
                        dispose();
                    });
                    break;

                default: // "none"
                    addButton.setText("Kết bạn");
                    addButton.setForeground(PRIMARY_COLOR);
                    addButton.setBackground(Color.WHITE);
                    addButton.setOpaque(true); // Required for macOS
                    addButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                            new EmptyBorder(6, 20, 6, 20)));
                    addButton.addActionListener(e -> sendFriendRequest(username, addButton));
                    break;
            }

            // Hover effect (chỉ khi enabled)
            if (addButton.isEnabled() && friendshipStatus.equals("none")) {
                addButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        addButton.setBackground(new Color(240, 245, 255));
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        addButton.setBackground(Color.WHITE);
                    }
                });
            }

            add(avatarLabel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
            add(addButton, BorderLayout.EAST);

            // Separator
            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(12, 15, 12, 15),
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240))));
        }
    }

    private void sendFriendRequest(String targetUsername, JButton button) {
        button.setEnabled(false);
        button.setText("Đang gửi...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Gọi UserService để lưu vào database
                boolean saved = userService.sendFriendRequest(currentUsername, targetUsername);

                if (saved) {
                    System.out.println(
                            "✅ Đã lưu lời mời kết bạn vào database: " + currentUsername + " → " + targetUsername);

                    // Gửi notification qua Socket
                    mainFrame.sendFriendRequestNotification(targetUsername);
                }

                return saved;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        button.setText("Đã gửi");
                        button.setForeground(new Color(67, 220, 96));
                        button.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(67, 220, 96), 1, true),
                                new EmptyBorder(6, 20, 6, 20)));

                        // Refresh FriendRequestPanel của user A (người gửi)
                        mainFrame.refreshFriendRequestPanel();

                        JOptionPane.showMessageDialog(AddFriendDialog.this,
                                "✅ Đã gửi lời mời kết bạn!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Không thành công - có thể bị chặn hoặc lỗi khác
                        button.setEnabled(true);
                        button.setText("Kết bạn");

                        JOptionPane.showMessageDialog(AddFriendDialog.this,
                                "❌ Không thể gửi lời mời kết bạn!\n" +
                                        "Có thể bạn đã bị chặn hoặc đã có lời mời trước đó.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    button.setEnabled(true);
                    button.setText("Kết bạn");

                    JOptionPane.showMessageDialog(AddFriendDialog.this,
                            "❌ Lỗi: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
