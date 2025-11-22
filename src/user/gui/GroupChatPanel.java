package user.gui;

import user.service.GroupService;
import user.socket.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel chat nhóm - Tương tự ChatContentPanel nhưng cho group
 */
public class GroupChatPanel extends JPanel {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color SENT_BUBBLE_COLOR = new Color(0, 132, 255);
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 242, 245);
    private static final Color BG_COLOR = Color.WHITE;
    
    private ZaloMainFrame mainFrame;
    private GroupService groupService;
    
    private int groupId;
    private String groupName;
    private boolean isAdmin;
    
    // Components
    private JLabel groupNameLabel;
    private JLabel memberCountLabel;
    private JPanel messageListPanel;
    private JScrollPane scrollPane;
    private JTextArea messageInput;
    private JButton sendButton;
    
    public GroupChatPanel(ZaloMainFrame mainFrame, int groupId, String groupName, boolean isAdmin) {
        this.mainFrame = mainFrame;
        this.groupService = new GroupService();
        this.groupId = groupId;
        this.groupName = groupName;
        this.isAdmin = isAdmin;
        
        initializeUI();
        loadGroupMessages();
        loadMemberCount();
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
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        // Left - Group info
        JPanel groupInfoPanel = new JPanel();
        groupInfoPanel.setLayout(new BoxLayout(groupInfoPanel, BoxLayout.Y_AXIS));
        groupInfoPanel.setOpaque(false);
        
        groupNameLabel = new JLabel(groupName);
        groupNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        memberCountLabel = new JLabel("Đang tải...");
        memberCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        memberCountLabel.setForeground(new Color(120, 120, 120));
        
        groupInfoPanel.add(groupNameLabel);
        groupInfoPanel.add(memberCountLabel);
        
        // Right - Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);
        
        JButton membersButton = createHeaderButton("👥", "Xem thành viên");
        membersButton.addActionListener(e -> showMembers());
        
        if (isAdmin) {
            JButton settingsButton = createHeaderButton("⚙️", "Cài đặt nhóm");
            settingsButton.addActionListener(e -> showGroupSettings());
            actionsPanel.add(settingsButton);
        }
        
        actionsPanel.add(membersButton);
        
        panel.add(groupInfoPanel, BorderLayout.WEST);
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
        
        // Message input
        messageInput = new JTextArea(2, 20);
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Enter to send
        messageInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });
        
        JScrollPane inputScroll = new JScrollPane(messageInput);
        inputScroll.setBorder(null);
        
        // Send button
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());
        
        panel.add(inputScroll, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadMemberCount() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return groupService.getGroupMembers(groupId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> members = get();
                    if (members != null) {
                        memberCountLabel.setText(members.size() + " thành viên");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void loadGroupMessages() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return groupService.getGroupMessages(groupId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> messages = get();
                    displayMessages(messages);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GroupChatPanel.this,
                        "Lỗi khi tải tin nhắn: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayMessages(List<Map<String, Object>> messages) {
        messageListPanel.removeAll();
        
        if (messages == null || messages.isEmpty()) {
            JLabel emptyLabel = new JLabel("Chưa có tin nhắn nào");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageListPanel.add(Box.createVerticalGlue());
            messageListPanel.add(emptyLabel);
            messageListPanel.add(Box.createVerticalGlue());
        } else {
            for (Map<String, Object> msg : messages) {
                String senderUsername = (String) msg.get("sender_username");
                String senderFullName = (String) msg.get("sender_full_name");
                String content = (String) msg.get("message");
                LocalDateTime sentAt = (LocalDateTime) msg.get("sent_at");
                
                boolean isSentByMe = senderUsername.equals(mainFrame.getUsername());
                
                JPanel messagePanel = createMessageBubble(
                    senderUsername,
                    senderFullName != null ? senderFullName : senderUsername,
                    content,
                    sentAt,
                    isSentByMe
                );
                
                messageListPanel.add(messagePanel);
                messageListPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        messageListPanel.revalidate();
        messageListPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private JPanel createMessageBubble(String senderUsername, String senderName, 
                                      String content, LocalDateTime sentAt, boolean isSentByMe) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setOpaque(true);
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        // Giới hạn tối đa ~400px chiều rộng, cho phép cao nhiều dòng
        bubble.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        
        if (isSentByMe) {
            bubble.setBackground(SENT_BUBBLE_COLOR);
            wrapper.add(Box.createHorizontalGlue());
        } else {
            bubble.setBackground(RECEIVED_BUBBLE_COLOR);
            
            // Hiển thị tên người gửi cho tin nhắn nhận
            JLabel senderLabel = new JLabel(senderName);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            senderLabel.setForeground(PRIMARY_COLOR);
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(senderLabel);
            bubble.add(Box.createVerticalStrut(3));
        }
        
        // Message content
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setForeground(isSentByMe ? Color.WHITE : Color.BLACK);
        contentArea.setOpaque(false);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Gợi ý width khoảng 30 columns, để Bubble không giãn full
        contentArea.setColumns(30);
        
        // Timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        JLabel timeLabel = new JLabel(sentAt.format(formatter));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(isSentByMe ? new Color(220, 230, 255) : new Color(120, 120, 120));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        bubble.add(contentArea);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLabel);
        
        wrapper.add(bubble);
        
        if (!isSentByMe) {
            wrapper.add(Box.createHorizontalGlue());
        }
        
        return wrapper;
    }
    
    private void sendMessage() {
        String content = messageInput.getText().trim();
        
        if (content.isEmpty()) {
            return;
        }
        
        sendButton.setEnabled(false);
        sendButton.setText("Đang gửi...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return groupService.sendGroupMessage(groupId, mainFrame.getUsername(), content);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        messageInput.setText("");
                        loadGroupMessages(); // Reload messages
                        
                        // TODO: Send socket message to notify other members
                        
                    } else {
                        JOptionPane.showMessageDialog(GroupChatPanel.this,
                            "Không thể gửi tin nhắn!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GroupChatPanel.this,
                        "Lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    sendButton.setEnabled(true);
                    sendButton.setText("Gửi");
                }
            }
        };
        
        worker.execute();
    }
    
    private void showMembers() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return groupService.getGroupMembers(groupId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> members = get();
                    
                    if (members != null) {
                        StringBuilder sb = new StringBuilder("Danh sách thành viên:\n\n");
                        
                        for (Map<String, Object> member : members) {
                            String username = (String) member.get("username");
                            String fullName = (String) member.get("full_name");
                            String role = (String) member.get("role");
                            
                            String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
                            
                            sb.append("• ").append(displayName);
                            if ("admin".equals(role)) {
                                sb.append(" (Quản trị viên)");
                            }
                            sb.append("\n");
                        }
                        
                        JOptionPane.showMessageDialog(GroupChatPanel.this,
                            sb.toString(),
                            "Thành viên nhóm",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void showGroupSettings() {
        JPopupMenu settingsMenu = new JPopupMenu();
        settingsMenu.setBackground(Color.WHITE);
        settingsMenu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JMenuItem renameItem = createMenuItem("Đổi tên nhóm");
        renameItem.addActionListener(e -> handleRenameGroup());
        
        JMenuItem addMemberItem = createMenuItem("Thêm thành viên");
        addMemberItem.addActionListener(e -> handleAddMember());
        
        JMenuItem manageMembersItem = createMenuItem("Quản lý thành viên");
        manageMembersItem.addActionListener(e -> handleManageMembers());
        
        settingsMenu.add(renameItem);
        settingsMenu.add(addMemberItem);
        settingsMenu.add(manageMembersItem);
        
        // Show menu at top-right
        settingsMenu.show(this, getWidth() - 200, 60);
    }
    
    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return item;
    }
    
    private void handleRenameGroup() {
        String newName = JOptionPane.showInputDialog(this,
            "Nhập tên nhóm mới:",
            groupName);
        
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(groupName)) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return groupService.renameGroup(groupId, newName.trim(), mainFrame.getUsername());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            groupName = newName.trim();
                            groupNameLabel.setText(groupName);
                            JOptionPane.showMessageDialog(GroupChatPanel.this,
                                "Đổi tên nhóm thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(GroupChatPanel.this,
                                "Không thể đổi tên nhóm!",
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
    
    private void handleAddMember() {
        JOptionPane.showMessageDialog(this,
            "Chức năng thêm thành viên đang được phát triển...",
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleManageMembers() {
        JOptionPane.showMessageDialog(this,
            "Chức năng quản lý thành viên đang được phát triển...",
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void receiveGroupMessage(String senderUsername, String content) {
        // Called when receiving real-time group message from socket
        loadGroupMessages(); // Reload to show new message
    }
    
    public int getGroupId() {
        return groupId;
    }
}
