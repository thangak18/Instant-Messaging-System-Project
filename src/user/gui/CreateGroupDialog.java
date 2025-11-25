package user.gui;

import user.service.UserService;
import user.service.GroupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Dialog tạo nhóm chat mới
 */
public class CreateGroupDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private GroupService groupService;
    
    private JTextField groupNameField;
    private JTextArea descriptionArea;
    private JList<CheckableItem> friendsList;
    private DefaultListModel<CheckableItem> friendsListModel;
    private JButton createButton, cancelButton;
    
    private List<Map<String, Object>> allFriends;
    
    public CreateGroupDialog(ZaloMainFrame mainFrame) {
        super(mainFrame, "Tạo nhóm chat mới", true);
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        this.groupService = new GroupService();
        
        initComponents();
        loadFriends();
    }
    
    private void initComponents() {
        setSize(500, 650);
        setLocationRelativeTo(mainFrame);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Tạo nhóm chat mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Chọn tối thiểu 1 bạn bè để tạo nhóm");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Group name
        JPanel namePanel = createFieldPanel("Tên nhóm:");
        groupNameField = new JTextField();
        groupNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        groupNameField.setPreferredSize(new Dimension(440, 40));
        groupNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        namePanel.add(groupNameField);
        
        // Description
        JPanel descPanel = createFieldPanel("Mô tả (tùy chọn):");
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(440, 80));
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        descPanel.add(descScroll);
        
        // Friends list
        JPanel friendsPanel = createFieldPanel("Chọn thành viên:");
        friendsListModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsListModel);
        friendsList.setCellRenderer(new CheckBoxListRenderer());
        friendsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        friendsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = friendsList.locationToIndex(e.getPoint());
                if (index != -1) {
                    CheckableItem item = friendsListModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    friendsList.repaint();
                }
            }
        });
        
        JScrollPane friendsScroll = new JScrollPane(friendsList);
        friendsScroll.setPreferredSize(new Dimension(440, 250));
        friendsScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        friendsPanel.add(friendsScroll);
        
        contentPanel.add(namePanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(descPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(friendsPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        cancelButton = createButton("Hủy", Color.WHITE, new Color(100, 100, 100));
        cancelButton.addActionListener(e -> dispose());
        
        createButton = createButton("Tạo nhóm", PRIMARY_COLOR, Color.WHITE);
        createButton.addActionListener(e -> handleCreateGroup());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFieldPanel(String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(new Color(50, 50, 50));
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(8));
        
        return panel;
    }
    
    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(120, 38));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadFriends() {
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
                        for (Map<String, Object> friend : allFriends) {
                            String username = (String) friend.get("username");
                            String fullName = (String) friend.get("full_name");
                            String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
                            
                            friendsListModel.addElement(new CheckableItem(username, displayName));
                        }
                    } else {
                        JOptionPane.showMessageDialog(CreateGroupDialog.this,
                            "Bạn chưa có bạn bè nào. Hãy kết bạn trước khi tạo nhóm!",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CreateGroupDialog.this,
                        "Lỗi khi tải danh sách bạn bè: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleCreateGroup() {
        String groupName = groupNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        // Validation
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tên nhóm!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected members
        List<String> selectedMembers = new ArrayList<>();
        for (int i = 0; i < friendsListModel.getSize(); i++) {
            CheckableItem item = friendsListModel.getElementAt(i);
            if (item.isSelected()) {
                selectedMembers.add(item.getUsername());
            }
        }
        
        if (selectedMembers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn ít nhất 1 thành viên!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable button
        createButton.setEnabled(false);
        createButton.setText("Đang tạo...");
        
        // Create group
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return groupService.createGroup(groupName, description, mainFrame.getUsername(), selectedMembers);
            }
            
            @Override
            protected void done() {
                try {
                    int groupId = get();
                    
                    if (groupId > 0) {
                        JOptionPane.showMessageDialog(CreateGroupDialog.this,
                            "Tạo nhóm thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dispose();
                        
                        // Mở group chat (sẽ tự động refresh khi cần)
                        mainFrame.openGroupChat(groupId, groupName, true);
                        
                        // Refresh chat list để hiển thị nhóm mới
                        SwingUtilities.invokeLater(() -> {
                            mainFrame.refreshChatList();
                        });
                        
                    } else {
                        JOptionPane.showMessageDialog(CreateGroupDialog.this,
                            "Không thể tạo nhóm. Vui lòng thử lại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        
                        createButton.setEnabled(true);
                        createButton.setText("Tạo nhóm");
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CreateGroupDialog.this,
                        "Lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    
                    createButton.setEnabled(true);
                    createButton.setText("Tạo nhóm");
                }
            }
        };
        
        worker.execute();
    }
    
    // Checkable item class
    private class CheckableItem {
        private String username;
        private String displayName;
        private boolean selected;
        
        public CheckableItem(String username, String displayName) {
            this.username = username;
            this.displayName = displayName;
            this.selected = false;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Custom renderer for checkbox list
    private class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<CheckableItem> {
        
        public CheckBoxListRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, CheckableItem value,
                                                     int index, boolean isSelected, boolean cellHasFocus) {
            setSelected(value.isSelected());
            setText(value.getDisplayName());
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            if (isSelected) {
                setBackground(new Color(230, 240, 255));
            } else {
                setBackground(Color.WHITE);
            }
            
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(8, 12, 8, 12)
            ));
            
            return this;
        }
    }
}
