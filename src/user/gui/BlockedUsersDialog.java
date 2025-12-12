package user.gui;

import user.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog hiển thị danh sách người dùng đã chặn
 */
public class BlockedUsersDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(250, 250, 250);
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private JPanel blockedListPanel;
    
    public BlockedUsersDialog(ZaloMainFrame mainFrame) {
        super(mainFrame, "Danh sách người đã chặn", true);
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        
        initializeUI();
        loadBlockedUsers();
    }
    
    private void initializeUI() {
        setSize(500, 600);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Danh sách người đã chặn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // List panel
        blockedListPanel = new JPanel();
        blockedListPanel.setLayout(new BoxLayout(blockedListPanel, BoxLayout.Y_AXIS));
        blockedListPanel.setBackground(BG_COLOR);
        blockedListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(blockedListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBlockedUsers() {
        blockedListPanel.removeAll();
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return userService.getBlockedUsers(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> blockedUsers = get();
                    
                    if (blockedUsers == null || blockedUsers.isEmpty()) {
                        JLabel emptyLabel = new JLabel("Bạn chưa chặn ai");
                        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                        emptyLabel.setForeground(new Color(150, 150, 150));
                        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        
                        blockedListPanel.add(Box.createVerticalGlue());
                        blockedListPanel.add(emptyLabel);
                        blockedListPanel.add(Box.createVerticalGlue());
                    } else {
                        for (Map<String, Object> user : blockedUsers) {
                            String username = (String) user.get("username");
                            String fullName = (String) user.get("full_name");
                            String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
                            
                            JPanel userItem = createBlockedUserItem(username, displayName);
                            blockedListPanel.add(userItem);
                            blockedListPanel.add(Box.createVerticalStrut(5));
                        }
                    }
                    
                    blockedListPanel.revalidate();
                    blockedListPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(BlockedUsersDialog.this,
                        "Lỗi khi tải danh sách: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private JPanel createBlockedUserItem(String username, String displayName) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Avatar (placeholder)
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon("icons/user.png");
            Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            avatarLabel.setText("[A]");
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            avatarLabel.setForeground(new Color(0, 132, 255));
        }
        
        // User info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JLabel usernameLabel = new JLabel("@" + username);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usernameLabel.setForeground(new Color(120, 120, 120));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(usernameLabel);
        
        // Unblock button
        JButton unblockButton = new JButton("Bỏ chặn");
        unblockButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        unblockButton.setBackground(new Color(220, 53, 69));
        unblockButton.setForeground(Color.WHITE);
        unblockButton.setBorderPainted(false);
        unblockButton.setFocusPainted(false);
        unblockButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unblockButton.setPreferredSize(new Dimension(90, 32));
        
        unblockButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn bỏ chặn " + displayName + "?",
                "Xác nhận bỏ chặn",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                unblockUser(username, unblockButton);
            }
        });
        
        item.add(avatarLabel, BorderLayout.WEST);
        item.add(infoPanel, BorderLayout.CENTER);
        item.add(unblockButton, BorderLayout.EAST);
        
        return item;
    }
    
    private void unblockUser(String blockedUsername, JButton button) {
        button.setEnabled(false);
        button.setText("Đang xử lý...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return userService.unblockUser(mainFrame.getUsername(), blockedUsername);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(BlockedUsersDialog.this,
                            "Đã bỏ chặn thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload list
                        loadBlockedUsers();
                    } else {
                        JOptionPane.showMessageDialog(BlockedUsersDialog.this,
                            "Không thể bỏ chặn. Vui lòng thử lại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        button.setEnabled(true);
                        button.setText("Bỏ chặn");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(BlockedUsersDialog.this,
                        "Lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    button.setEnabled(true);
                    button.setText("Bỏ chặn");
                }
            }
        };
        
        worker.execute();
    }
}
