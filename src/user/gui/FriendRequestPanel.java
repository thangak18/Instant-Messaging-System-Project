package user.gui;

import user.service.UserService;
import user.socket.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Friend Request Panel - Hiển thị lời mời kết bạn
 * 2 tabs: Lời mời đã nhận + Lời mời đã gửi
 */
public class FriendRequestPanel extends JPanel {
    
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    
    private JPanel receivedPanel;
    private JPanel sentPanel;
    private JTabbedPane tabbedPane;
    
    public FriendRequestPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        initializeUI();
        loadFriendRequests();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
        
        // Received requests panel
        receivedPanel = new JPanel();
        receivedPanel.setLayout(new BoxLayout(receivedPanel, BoxLayout.Y_AXIS));
        receivedPanel.setBackground(BG_COLOR);
        
        JScrollPane receivedScroll = new JScrollPane(receivedPanel);
        receivedScroll.setBorder(null);
        receivedScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        // Sent requests panel
        sentPanel = new JPanel();
        sentPanel.setLayout(new BoxLayout(sentPanel, BoxLayout.Y_AXIS));
        sentPanel.setBackground(BG_COLOR);
        
        JScrollPane sentScroll = new JScrollPane(sentPanel);
        sentScroll.setBorder(null);
        sentScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        tabbedPane.addTab("Lời mời đã nhận (2)", receivedScroll);
        tabbedPane.addTab("Lời mời đã gửi (8)", sentScroll);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void loadFriendRequests() {
        // Load received requests
        loadReceivedRequests();
        
        // Load sent requests
        loadSentRequests();
    }
    
    private void loadReceivedRequests() {
        receivedPanel.removeAll();
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return userService.getReceivedFriendRequests(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> requests = get();
                    
                    if (requests == null || requests.isEmpty()) {
                        showEmptyMessage(receivedPanel, "Không có lời mời kết bạn");
                        tabbedPane.setTitleAt(0, "Lời mời đã nhận (0)");
                    } else {
                        for (Map<String, Object> request : requests) {
                            ReceivedRequestPanel requestPanel = new ReceivedRequestPanel(request);
                            receivedPanel.add(requestPanel);
                        }
                        tabbedPane.setTitleAt(0, "Lời mời đã nhận (" + requests.size() + ")");
                    }
                    
                    receivedPanel.revalidate();
                    receivedPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyMessage(receivedPanel, "Lỗi: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void loadSentRequests() {
        sentPanel.removeAll();
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return userService.getSentFriendRequests(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> requests = get();
                    
                    if (requests == null || requests.isEmpty()) {
                        showEmptyMessage(sentPanel, "Bạn chưa gửi lời mời nào");
                        tabbedPane.setTitleAt(1, "Lời mời đã gửi (0)");
                    } else {
                        for (Map<String, Object> request : requests) {
                            SentRequestPanel requestPanel = new SentRequestPanel(request);
                            sentPanel.add(requestPanel);
                        }
                        tabbedPane.setTitleAt(1, "Lời mời đã gửi (" + requests.size() + ")");
                    }
                    
                    sentPanel.revalidate();
                    sentPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyMessage(sentPanel, "Lỗi: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void showEmptyMessage(JPanel panel, String message) {
        panel.removeAll();
        
        JLabel label = new JLabel("<html><center>😔<br><br>" + message + "</center></html>");
        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        label.setForeground(new Color(150, 150, 150));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(80, 20, 80, 20));
        
        panel.add(label);
        panel.revalidate();
        panel.repaint();
    }
    
    /**
     * Panel cho từng lời mời ĐÃ NHẬN
     */
    private class ReceivedRequestPanel extends JPanel {
        public ReceivedRequestPanel(Map<String, Object> requestData) {
            setLayout(new BorderLayout(12, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            
            String senderUsername = (String) requestData.get("sender_username");
            String senderName = (String) requestData.get("sender_name");
            String createdAt = requestData.get("created_at") != null ? 
                              requestData.get("created_at").toString() : "18/10";
            
            // Left - Avatar + Info
            JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
            leftPanel.setOpaque(false);
            
            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(50, 50));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            try {
                ImageIcon icon = new ImageIcon("icons/user.png");
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                avatarLabel.setText("[A]");
                avatarLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 16));
            }
            
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(senderName != null ? senderName : senderUsername);
            nameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 15));
            
            JLabel timeLabel = new JLabel(createdAt);
            timeLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            timeLabel.setForeground(new Color(120, 120, 120));
            
            JLabel messageLabel = new JLabel("Xin chào, mình là " + (senderName != null ? senderName : senderUsername) + ". Kết bạn với mình nhé!");
            messageLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            messageLabel.setForeground(new Color(100, 100, 100));
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            infoPanel.add(timeLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(messageLabel);
            
            leftPanel.add(avatarLabel, BorderLayout.WEST);
            leftPanel.add(infoPanel, BorderLayout.CENTER);
            
            // Right - Action buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            buttonPanel.setOpaque(false);
            
            JButton acceptButton = createActionButton("Đồng ý", PRIMARY_COLOR, Color.WHITE);
            JButton rejectButton = createActionButton("Từ chối", Color.WHITE, new Color(100, 100, 100));
            
            acceptButton.addActionListener(e -> acceptFriendRequest(senderUsername, requestData));
            rejectButton.addActionListener(e -> rejectFriendRequest(senderUsername, requestData));
            
            buttonPanel.add(acceptButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            buttonPanel.add(rejectButton);
            
            add(leftPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.EAST);
        }
    }
    
    /**
     * Panel cho từng lời mời ĐÃ GỬI
     */
    private class SentRequestPanel extends JPanel {
        public SentRequestPanel(Map<String, Object> requestData) {
            setLayout(new BorderLayout(12, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            
            String receiverUsername = (String) requestData.get("receiver_username");
            String receiverName = (String) requestData.get("receiver_name");
            
            // Left - Avatar + Info
            JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
            leftPanel.setOpaque(false);
            
            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(50, 50));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            try {
                ImageIcon icon = new ImageIcon("icons/user.png");
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                avatarLabel.setText("[A]");
                avatarLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 16));
            }
            
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(receiverName != null ? receiverName : receiverUsername);
            nameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 15));
            
            JLabel statusLabel = new JLabel("Bạn đã gửi lời mời");
            statusLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
            statusLabel.setForeground(new Color(120, 120, 120));
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(statusLabel);
            
            leftPanel.add(avatarLabel, BorderLayout.WEST);
            leftPanel.add(infoPanel, BorderLayout.CENTER);
            
            // Right - Recall button
            JButton recallButton = createActionButton("Thu hồi lời mời", Color.WHITE, new Color(100, 100, 100));
            recallButton.addActionListener(e -> recallFriendRequest(receiverUsername, requestData));
            
            add(leftPanel, BorderLayout.CENTER);
            add(recallButton, BorderLayout.EAST);
        }
    }
    
    private JButton createActionButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setPreferredSize(new Dimension(100, 36));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.equals(Color.WHITE) ? new Color(200, 200, 200) : bgColor, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void acceptFriendRequest(String senderUsername, Map<String, Object> requestData) {
        int friendshipId = (int) requestData.get("friendship_id");
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userService.acceptFriendRequest(friendshipId);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        System.out.println("✅ Đã chấp nhận lời mời từ: " + senderUsername);
                        
                        // Gửi notification qua Socket cho User B (người gửi lời mời)
                        mainFrame.sendFriendRequestAcceptedNotification(senderUsername);
                        
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "✅ Đã chấp nhận lời mời kết bạn!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload friend requests
                        loadReceivedRequests();
                        
                        // ✅ REFRESH CHAT LIST VÀ FRIEND LIST CỦA USER A (người chấp nhận)
                        mainFrame.refreshChatAndFriendList();
                    } else {
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "❌ Không thể chấp nhận lời mời!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    private void rejectFriendRequest(String senderUsername, Map<String, Object> requestData) {
        int friendshipId = (int) requestData.get("friendship_id");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn từ chối lời mời từ " + senderUsername + "?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userService.rejectFriendRequest(friendshipId);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        System.out.println("❌ Đã từ chối lời mời từ: " + senderUsername);
                        
                        // Gửi notification qua Socket
                        mainFrame.sendFriendRequestRejectedNotification(senderUsername);
                        
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "✅ Đã từ chối lời mời!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload
                        loadReceivedRequests();
                    } else {
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "❌ Không thể từ chối lời mời!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    private void recallFriendRequest(String receiverUsername, Map<String, Object> requestData) {
        int friendshipId = (int) requestData.get("friendship_id");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn thu hồi lời mời gửi cho " + receiverUsername + "?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userService.recallFriendRequest(friendshipId);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        System.out.println("🔙 Đã thu hồi lời mời gửi cho: " + receiverUsername);
                        
                        // Gửi notification qua Socket
                        mainFrame.sendFriendRequestRecalledNotification(receiverUsername);
                        
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "✅ Đã thu hồi lời mời!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload
                        loadSentRequests();
                    } else {
                        JOptionPane.showMessageDialog(FriendRequestPanel.this,
                            "❌ Không thể thu hồi lời mời!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    /**
     * Refresh friend requests - Gọi khi nhận notification từ Socket
     */
    public void refreshFriendRequests() {
        System.out.println("🔄 Refreshing friend requests...");
        SwingUtilities.invokeLater(() -> {
            loadReceivedRequests();
            loadSentRequests();
        });
    }
}
