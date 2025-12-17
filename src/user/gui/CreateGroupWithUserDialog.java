package user.gui;

import user.service.GroupService;
import user.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog tạo nhóm chat với một user được chọn sẵn
 */
public class CreateGroupWithUserDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    
    private ZaloMainFrame mainFrame;
    private GroupService groupService;
    private UserService userService;
    
    private String preSelectedUsername;
    private String preSelectedFullName;
    
    private JTextField groupNameField;
    private JTextArea descriptionArea;
    private JList<CheckableItem> membersList;
    private DefaultListModel<CheckableItem> membersListModel;
    
    public CreateGroupWithUserDialog(ZaloMainFrame mainFrame, String preSelectedUsername, String preSelectedFullName) {
        super(mainFrame, "Tạo nhóm chat mới", true);
        this.mainFrame = mainFrame;
        this.preSelectedUsername = preSelectedUsername;
        this.preSelectedFullName = preSelectedFullName;
        this.groupService = new GroupService();
        this.userService = new UserService();
        
        initComponents();
        loadMembers();
    }
    
    private void initComponents() {
        setSize(500, 600);
        setLocationRelativeTo(mainFrame);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Tạo nhóm chat với " + (preSelectedFullName != null ? preSelectedFullName : preSelectedUsername));
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Thêm thành viên khác nếu muốn");
        subtitleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
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
        JPanel namePanel = createInputPanel("Tên nhóm:", groupNameField = new JTextField());
        groupNameField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        contentPanel.add(namePanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Description
        JPanel descPanel = new JPanel(new BorderLayout(0, 5));
        descPanel.setBackground(BG_COLOR);
        descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLabel = new JLabel("Mô tả (tùy chọn):");
        descLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descScroll, BorderLayout.CENTER);
        contentPanel.add(descPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Selected user info
        JPanel selectedUserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectedUserPanel.setBackground(new Color(230, 245, 255));
        selectedUserPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel selectedLabel = new JLabel("✓ Đã chọn: " + (preSelectedFullName != null ? preSelectedFullName : preSelectedUsername) + " (@" + preSelectedUsername + ")");
        selectedLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        selectedLabel.setForeground(PRIMARY_COLOR);
        selectedUserPanel.add(selectedLabel);
        selectedUserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedUserPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        contentPanel.add(selectedUserPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Additional members list
        JPanel membersPanel = new JPanel(new BorderLayout(0, 5));
        membersPanel.setBackground(BG_COLOR);
        membersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel membersLabel = new JLabel("Thêm thành viên khác (tùy chọn):");
        membersLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        
        membersListModel = new DefaultListModel<>();
        membersList = new JList<>(membersListModel);
        membersList.setCellRenderer(new CheckableListCellRenderer());
        membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = membersList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    CheckableItem item = membersListModel.get(index);
                    item.setSelected(!item.isSelected());
                    membersList.repaint();
                }
            }
        });
        
        JScrollPane membersScroll = new JScrollPane(membersList);
        membersScroll.setPreferredSize(new Dimension(400, 150));
        membersScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        membersPanel.add(membersLabel, BorderLayout.NORTH);
        membersPanel.add(membersScroll, BorderLayout.CENTER);
        contentPanel.add(membersPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> dispose());
        
        JButton createButton = new JButton("Tạo nhóm");
        createButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        createButton.setForeground(Color.WHITE);
        createButton.setBackground(PRIMARY_COLOR);
        createButton.setBorderPainted(false);
        createButton.setPreferredSize(new Dimension(120, 38));
        createButton.addActionListener(e -> handleCreateGroup());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        contentPanel.add(buttonPanel);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BG_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        
        field.setPreferredSize(new Dimension(400, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadMembers() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                // Lấy danh sách bạn bè
                return userService.getFriendsList(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> friends = get();
                    membersListModel.clear();
                    
                    for (Map<String, Object> friend : friends) {
                        String username = (String) friend.get("username");
                        String fullName = (String) friend.get("full_name");
                        
                        // Bỏ qua user đã được chọn sẵn
                        if (!username.equals(preSelectedUsername)) {
                            String displayName = fullName != null ? fullName + " (@" + username + ")" : "@" + username;
                            membersListModel.addElement(new CheckableItem(username, displayName, false));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void handleCreateGroup() {
        String groupName = groupNameField.getText().trim();
        
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tên nhóm!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Thu thập thành viên đã chọn
        List<String> selectedMembers = new ArrayList<>();
        selectedMembers.add(preSelectedUsername); // Luôn thêm user được chọn sẵn
        
        for (int i = 0; i < membersListModel.size(); i++) {
            CheckableItem item = membersListModel.get(i);
            if (item.isSelected()) {
                selectedMembers.add(item.getUsername());
            }
        }
        
        String description = descriptionArea.getText().trim();
        
        // Tạo nhóm
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return groupService.createGroup(groupName, description, mainFrame.getUsername(), selectedMembers, false);
            }
            
            @Override
            protected void done() {
                try {
                    int groupId = get();
                    if (groupId > 0) {
                        JOptionPane.showMessageDialog(CreateGroupWithUserDialog.this,
                            "✅ Đã tạo nhóm \"" + groupName + "\" thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh group list
                        mainFrame.refreshGroupList();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(CreateGroupWithUserDialog.this,
                            "❌ Không thể tạo nhóm. Vui lòng thử lại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CreateGroupWithUserDialog.this,
                        "❌ Lỗi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Inner class for checkable list items
     */
    class CheckableItem {
        private String username;
        private String displayName;
        private boolean selected;
        
        public CheckableItem(String username, String displayName, boolean selected) {
            this.username = username;
            this.displayName = displayName;
            this.selected = selected;
        }
        
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        
        @Override
        public String toString() { return displayName; }
    }
    
    /**
     * Cell renderer for checkable list
     */
    class CheckableListCellRenderer extends JPanel implements ListCellRenderer<CheckableItem> {
        private JCheckBox checkBox;
        private JLabel label;
        
        public CheckableListCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(8, 10, 8, 10));
            
            checkBox = new JCheckBox();
            checkBox.setOpaque(false);
            
            label = new JLabel();
            label.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
            
            add(checkBox, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, 
                CheckableItem value, int index, boolean isSelected, boolean cellHasFocus) {
            
            checkBox.setSelected(value.isSelected());
            label.setText(value.getDisplayName());
            
            if (isSelected) {
                setBackground(new Color(240, 245, 255));
            } else {
                setBackground(Color.WHITE);
            }
            
            return this;
        }
    }
}
