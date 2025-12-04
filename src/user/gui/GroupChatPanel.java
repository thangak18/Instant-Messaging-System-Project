package user.gui;

import user.service.GroupService;
import user.service.AIService;
import user.socket.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    private AIService aiService;
    
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
    
    // Map lưu bubble theo messageId để hỗ trợ scroll tới tin nhắn
    private Map<Integer, JPanel> messageBubbles = new HashMap<>();
    
    public GroupChatPanel(ZaloMainFrame mainFrame, int groupId, String groupName, boolean isAdmin) {
        this.mainFrame = mainFrame;
        this.groupService = new GroupService();
        this.aiService = new AIService();
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
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);
        
        // Search button
        JButton searchButton = createHeaderButton("icons/search.png", "Tìm kiếm tin nhắn", 22);
        searchButton.addActionListener(e -> showSearchInGroupDialog());
        
        // AI button
        JButton aiButton = createHeaderButton("icons/generative.png", "Trợ lý AI", 22);
        aiButton.addActionListener(e -> showAIAssistant());
        
        // Rename group button
        JButton renameButton = createHeaderButton("icons/file.png", "Đổi tên nhóm", 22);
        renameButton.addActionListener(e -> renameGroup());
        
        // Add member button
        JButton addMemberButton = createHeaderButton("icons/add-friend.png", "Thêm thành viên", 22);
        addMemberButton.addActionListener(e -> addMember());
        
        // View members button
        JButton membersButton = createHeaderButton("icons/user.png", "Quản lý thành viên", 22);
        membersButton.addActionListener(e -> showMembersManagement());
        
        // Encryption toggle button
        JButton encryptButton = createHeaderButton("icons/settings.png", "Mã hóa nhóm", 22);
        encryptButton.addActionListener(e -> toggleEncryption());
        
        // Delete group button (chỉ admin mới thấy)
        JButton deleteGroupButton = createHeaderButton("icons/bin.png", "Xóa nhóm", 22);
        deleteGroupButton.addActionListener(e -> deleteGroup());
        if (!isAdmin) {
            deleteGroupButton.setVisible(false); // Ẩn nếu không phải admin
        }

        actionsPanel.add(searchButton);
        actionsPanel.add(aiButton);
        actionsPanel.add(renameButton);
        actionsPanel.add(addMemberButton);
        actionsPanel.add(membersButton);
        actionsPanel.add(encryptButton);
        actionsPanel.add(deleteGroupButton);
        
        panel.add(groupInfoPanel, BorderLayout.WEST);
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
        messageBubbles.clear();
        
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
                int messageId = msg.get("message_id") != null ? (int) msg.get("message_id") : 0;
                String senderUsername = (String) msg.get("sender_username");
                String senderFullName = (String) msg.get("sender_full_name");
                String content = (String) msg.get("message");
                LocalDateTime sentAt = (LocalDateTime) msg.get("sent_at");
                
                boolean isSentByMe = senderUsername.equals(mainFrame.getUsername());
                
                JPanel messagePanel = createMessageBubble(
                    messageId,
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
    
    private JPanel createMessageBubble(int messageId, String senderUsername, String senderName, 
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
        
        // Lưu vào map để hỗ trợ scroll đến tin nhắn
        if (messageId > 0) {
            messageBubbles.put(messageId, wrapper);
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
    
    // ==================== GROUP MANAGEMENT FEATURES ====================
    
    /**
     * ĐỔI TÊN NHÓM
     */
    private void renameGroup() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this,
                "Chỉ admin mới có quyền đổi tên nhóm!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String newName = JOptionPane.showInputDialog(this,
            "Nhập tên mới cho nhóm:",
            "Đổi tên nhóm",
            JOptionPane.QUESTION_MESSAGE);
        
        if (newName != null && !newName.trim().isEmpty()) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return groupService.updateGroupName(groupId, newName.trim());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            groupName = newName.trim();
                            groupNameLabel.setText(groupName);
                            JOptionPane.showMessageDialog(GroupChatPanel.this,
                                "Đã đổi tên nhóm thành công!",
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
    
    /**
     * c. THÊM THÀNH VIÊN
     */
    private void addMember() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this,
                "Chỉ admin mới có quyền thêm thành viên!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Thêm thành viên", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("Nhập username cần thêm:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(usernameField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton addButton = new JButton("Thêm");
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập username!");
                return;
            }
            
            addButton.setEnabled(false);
            addButton.setText("Đang thêm...");
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return groupService.addMemberToGroup(groupId, username);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(dialog,
                                "Đã thêm " + username + " vào nhóm!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            loadMemberCount();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Không thể thêm thành viên! Kiểm tra username.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                            addButton.setEnabled(true);
                            addButton.setText("Thêm");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        addButton.setEnabled(true);
                        addButton.setText("Thêm");
                    }
                }
            };
            
            worker.execute();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * d, e. QUẢN LÝ THÀNH VIÊN (Gán admin, Xóa thành viên)
     */
    private void showMembersManagement() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Quản lý thành viên", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("👥 Thành viên nhóm: " + groupName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Members list
        JPanel membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBackground(Color.WHITE);
        membersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return groupService.getGroupMembers(groupId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> members = get();
                    
                    if (members == null || members.isEmpty()) {
                        JLabel emptyLabel = new JLabel("Không có thành viên");
                        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        emptyLabel.setForeground(new Color(150, 150, 150));
                        membersPanel.add(emptyLabel);
                    } else {
                        for (Map<String, Object> member : members) {
                            String username = (String) member.get("username");
                            String fullName = (String) member.get("full_name");
                            boolean memberIsAdmin = (boolean) member.get("is_admin");
                            
                            JPanel memberPanel = new JPanel(new BorderLayout(10, 0));
                            memberPanel.setBackground(Color.WHITE);
                            memberPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                                BorderFactory.createEmptyBorder(12, 10, 12, 10)
                            ));
                            memberPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                            
                            JPanel infoPanel = new JPanel();
                            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                            infoPanel.setOpaque(false);
                            
                            JLabel nameLabel = new JLabel((fullName != null ? fullName : username) + 
                                (memberIsAdmin ? " 👑" : ""));
                            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                            
                            JLabel usernameLabel = new JLabel("@" + username);
                            usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                            usernameLabel.setForeground(new Color(120, 120, 120));
                            
                            infoPanel.add(nameLabel);
                            infoPanel.add(usernameLabel);
                            
                            // Action buttons (only for admin)
                            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                            actionPanel.setOpaque(false);
                            
                            if (isAdmin && !username.equals(mainFrame.getUsername())) {
                                // d. Gán/Bỏ quyền admin
                                JButton adminButton = new JButton(memberIsAdmin ? "Bỏ Admin" : "Gán Admin");
                                adminButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                adminButton.setBackground(memberIsAdmin ? new Color(255, 152, 0) : PRIMARY_COLOR);
                                adminButton.setForeground(Color.WHITE);
                                adminButton.setFocusPainted(false);
                                adminButton.setBorderPainted(false);
                                adminButton.addActionListener(e -> {
                                    int confirm = JOptionPane.showConfirmDialog(dialog,
                                        (memberIsAdmin ? "Bỏ quyền admin của " : "Gán quyền admin cho ") + username + "?",
                                        "Xác nhận",
                                        JOptionPane.YES_NO_OPTION);
                                    
                                    if (confirm == JOptionPane.YES_OPTION) {
                                        boolean success = groupService.setGroupAdmin(groupId, username, !memberIsAdmin);
                                        if (success) {
                                            JOptionPane.showMessageDialog(dialog, "Đã cập nhật quyền!");
                                            dialog.dispose();
                                            showMembersManagement(); // Reload
                                        } else {
                                            JOptionPane.showMessageDialog(dialog, "Không thể cập nhật quyền!");
                                        }
                                    }
                                });
                                
                                // e. Xóa thành viên
                                JButton removeButton = new JButton("Xóa");
                                removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                removeButton.setBackground(new Color(255, 59, 48));
                                removeButton.setForeground(Color.WHITE);
                                removeButton.setFocusPainted(false);
                                removeButton.setBorderPainted(false);
                                removeButton.addActionListener(e -> {
                                    int confirm = JOptionPane.showConfirmDialog(dialog,
                                        "Xóa " + username + " khỏi nhóm?",
                                        "Xác nhận",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE);
                                    
                                    if (confirm == JOptionPane.YES_OPTION) {
                                        boolean success = groupService.removeMemberFromGroup(groupId, username);
                                        if (success) {
                                            JOptionPane.showMessageDialog(dialog, "Đã xóa thành viên!");
                                            dialog.dispose();
                                            showMembersManagement(); // Reload
                                            loadMemberCount();
                                        } else {
                                            JOptionPane.showMessageDialog(dialog, "Không thể xóa thành viên!");
                                        }
                                    }
                                });
                                
                                actionPanel.add(adminButton);
                                actionPanel.add(removeButton);
                            }
                            
                            memberPanel.add(infoPanel, BorderLayout.WEST);
                            memberPanel.add(actionPanel, BorderLayout.EAST);
                            
                            membersPanel.add(memberPanel);
                        }
                    }
                    
                    membersPanel.revalidate();
                    membersPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
        
        JScrollPane scrollPane = new JScrollPane(membersPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    /**
     * g. MÃ HÓA NHÓM (End-to-End Encryption)
     */
    private void toggleEncryption() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this,
                "Chỉ admin mới có quyền bật/tắt mã hóa!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Mã hóa đầu cuối", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80)); // Green
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🔒 Mã hóa đầu cuối");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel descLabel = new JLabel("<html><div style='width:400px;'>" +
            "<b>Mã hóa đầu cuối</b> bảo vệ tin nhắn của bạn khỏi việc bị đọc trộm.<br><br>" +
            "<b>Khi bật:</b><br>" +
            "• Chỉ thành viên trong nhóm có thể đọc tin nhắn<br>" +
            "• Tin nhắn được mã hóa trước khi gửi<br>" +
            "• Ngay cả server cũng không thể đọc nội dung<br><br>" +
            "<b>Lưu ý:</b> Tính năng này tương tự Facebook Secret Conversation" +
            "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Check current encryption status
        SwingWorker<Boolean, Void> checkWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return groupService.isGroupEncrypted(groupId);
            }
            
            @Override
            protected void done() {
                try {
                    boolean isEncrypted = get();
                    
                    JLabel statusLabel = new JLabel("Trạng thái hiện tại: " + 
                        (isEncrypted ? "🔒 Đã mã hóa" : "🔓 Chưa mã hóa"));
                    statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    statusLabel.setForeground(isEncrypted ? new Color(76, 175, 80) : new Color(255, 152, 0));
                    statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    contentPanel.add(statusLabel);
                    contentPanel.revalidate();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        checkWorker.execute();
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = new JButton("Đóng");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton toggleButton = new JButton("Bật/Tắt mã hóa");
        toggleButton.setBackground(new Color(76, 175, 80));
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Bạn có chắc muốn thay đổi cài đặt mã hóa?\n" +
                "Điều này sẽ ảnh hưởng đến tất cả thành viên trong nhóm.",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                toggleButton.setEnabled(false);
                toggleButton.setText("Đang xử lý...");
                
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        return groupService.toggleGroupEncryption(groupId);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(dialog,
                                    "Đã cập nhật cài đặt mã hóa!",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                                dialog.dispose();
                            } else {
                                JOptionPane.showMessageDialog(dialog,
                                    "Không thể thay đổi cài đặt mã hóa!",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                                toggleButton.setEnabled(true);
                                toggleButton.setText("Bật/Tắt mã hóa");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            toggleButton.setEnabled(true);
                            toggleButton.setText("Bật/Tắt mã hóa");
                        }
                    }
                };
                
                worker.execute();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(toggleButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * XÓA NHÓM CHAT (CHỈ ADMIN)
     */
    private void deleteGroup() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this,
                "Chỉ admin mới có quyền xóa nhóm!",
                "Không có quyền",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Tạo dialog xác nhận
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xóa nhóm chat", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header panel với icon cảnh báo
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(220, 53, 69)); // Màu đỏ cảnh báo
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        
        JLabel titleLabel = new JLabel("Xóa nhóm \"" + groupName + "\"?");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Content panel với cảnh báo
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel warningLabel1 = new JLabel("Hành động này không thể hoàn tác!");
        warningLabel1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warningLabel1.setForeground(new Color(220, 53, 69));
        warningLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel warningLabel2 = new JLabel("<html><div style='width: 300px;'>" +
            "• Tất cả tin nhắn trong nhóm sẽ bị xóa vĩnh viễn<br>" +
            "• Tất cả thành viên sẽ bị xóa khỏi nhóm<br>" +
            "• Nhóm sẽ không thể khôi phục</div></html>");
        warningLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        warningLabel2.setForeground(new Color(80, 80, 80));
        warningLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(warningLabel1);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(warningLabel2);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton deleteButton = new JButton("Xóa nhóm");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setPreferredSize(new Dimension(120, 35));
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        deleteButton.addActionListener(e -> {
            // Xác nhận lần 2
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Bạn có CHẮC CHẮN muốn xóa nhóm \"" + groupName + "\" không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                deleteButton.setEnabled(false);
                deleteButton.setText("Đang xóa...");
                
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        return groupService.deleteGroup(groupId, mainFrame.getUsername());
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                dialog.dispose();
                                JOptionPane.showMessageDialog(
                                    SwingUtilities.getWindowAncestor(GroupChatPanel.this),
                                    "Đã xóa nhóm \"" + groupName + "\" thành công!",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                                
                                // Quay lại danh sách nhóm
                                mainFrame.showGroupList();
                            } else {
                                JOptionPane.showMessageDialog(dialog,
                                    "Không thể xóa nhóm! Vui lòng thử lại.",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                                deleteButton.setEnabled(true);
                                deleteButton.setText("Xóa nhóm");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(dialog,
                                "Có lỗi xảy ra: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                            deleteButton.setEnabled(true);
                            deleteButton.setText("Xóa nhóm");
                        }
                    }
                };
                
                worker.execute();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    // ==================== TÌM KIẾM TIN NHẮN ====================
    
    /**
     * Hiển thị dialog tìm kiếm tin nhắn trong nhóm
     */
    private void showSearchInGroupDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Tìm kiếm trong " + groupName, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🔍 Tìm kiếm trong " + groupName);
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
        
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(null);
        resultsScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        searchPanel.add(resultsScroll, BorderLayout.CENTER);
        
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
            
            SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Map<String, Object>> doInBackground() {
                    return groupService.searchGroupMessages(groupId, keyword);
                }
                
                @Override
                protected void done() {
                    try {
                        List<Map<String, Object>> results = get();
                        
                        if (results == null || results.isEmpty()) {
                            JLabel label = new JLabel("Không tìm thấy kết quả");
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            label.setForeground(new Color(150, 150, 150));
                            resultsPanel.add(label);
                        } else {
                            for (Map<String, Object> result : results) {
                                int messageId = result.get("message_id") != null ? (int) result.get("message_id") : 0;
                                String sender = (String) result.get("sender_username");
                                String senderName = (String) result.get("sender_full_name");
                                String content = (String) result.get("message");
                                LocalDateTime sentAt = (LocalDateTime) result.get("sent_at");
                                
                                JPanel resultItem = new JPanel(new BorderLayout(10, 5));
                                resultItem.setBackground(Color.WHITE);
                                resultItem.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                                    BorderFactory.createEmptyBorder(12, 10, 12, 10)
                                ));
                                resultItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                                resultItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                
                                boolean isSent = sender.equals(mainFrame.getUsername());
                                String displayName = senderName != null ? senderName : sender;
                                String timeStr = sentAt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
                                
                                JLabel nameLabel = new JLabel((isSent ? "Bạn" : displayName) + " - " + timeStr);
                                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                nameLabel.setForeground(isSent ? PRIMARY_COLOR : new Color(100, 100, 100));
                                
                                JLabel contentLabel = new JLabel("<html>" + highlightKeyword(content, keyword) + "</html>");
                                contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                                
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
                                
                                // Click để scroll đến tin nhắn
                                final int msgId = messageId;
                                resultItem.addMouseListener(new java.awt.event.MouseAdapter() {
                                    @Override
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        dialog.dispose();
                                        scrollToMessage(msgId);
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
    
    /**
     * Highlight keyword trong text
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null) return text;
        return text.replaceAll("(?i)(" + java.util.regex.Pattern.quote(keyword) + ")", 
            "<span style='background-color: yellow; font-weight: bold;'>$1</span>");
    }
    
    /**
     * Scroll đến tin nhắn cụ thể và highlight
     */
    private void scrollToMessage(int messageId) {
        JPanel bubble = messageBubbles.get(messageId);
        if (bubble != null) {
            SwingUtilities.invokeLater(() -> {
                Rectangle bounds = bubble.getBounds();
                bubble.scrollRectToVisible(bounds);
                
                // Highlight tin nhắn trong 2 giây
                Color originalBg = bubble.getBackground();
                bubble.setOpaque(true);
                bubble.setBackground(new Color(255, 255, 150));
                
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
    
    // ==================== TRỢ LÝ AI ====================
    
    /**
     * Hiển thị dialog trợ lý AI
     */
    private void showAIAssistant() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "🤖 Trợ lý AI", true);
        dialog.setSize(650, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(138, 43, 226));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🤖 Trợ lý AI - Gợi ý tin nhắn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel statusLabel = new JLabel(aiService.isAPIConfigured() ? "🟢 Online" : "🟡 Offline Mode");
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
        quickPanel.setBorder(BorderFactory.createTitledBorder("💡 Gợi ý nhanh:"));
        
        String[] quickSuggestions = {"Xin lỗi", "Cảm ơn", "Chúc mừng", "Hẹn gặp", "Hỏi thăm", "Động viên"};
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
            "<html>Mô tả tình huống, AI sẽ gợi ý tin nhắn phù hợp cho nhóm:</html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane inputScroll = new JScrollPane(inputArea);
        
        JButton generateButton = new JButton("✨ Tạo gợi ý");
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
        resultScroll.setBorder(BorderFactory.createTitledBorder("📝 Gợi ý từ AI:"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton regenerateButton = new JButton("🔄 Tạo lại");
        regenerateButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        regenerateButton.setEnabled(false);
        regenerateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton copyButton = new JButton("📋 Sao chép");
        copyButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copyButton.setEnabled(false);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyButton.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = 
                new java.awt.datatransfer.StringSelection(resultArea.getText());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            copyButton.setText("✓ Đã sao chép!");
            Timer timer = new Timer(2000, evt -> copyButton.setText("📋 Sao chép"));
            timer.setRepeats(false);
            timer.start();
        });
        
        JButton useButton = new JButton("✓ Sử dụng");
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
                    String chatContext = "Đang chat trong nhóm: " + groupName;
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
                        generateButton.setText("✨ Tạo gợi ý");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        resultArea.setText("❌ Lỗi: Không thể tạo gợi ý! Vui lòng thử lại.");
                        resultArea.setForeground(new Color(200, 50, 50));
                        generateButton.setEnabled(true);
                        generateButton.setText("✨ Tạo gợi ý");
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
