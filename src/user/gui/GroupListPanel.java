package user.gui;

import user.service.GroupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel hiển thị danh sách nhóm chat
 */
public class GroupListPanel extends JPanel {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    
    private ZaloMainFrame mainFrame;
    private GroupService groupService;
    
    private JPanel groupsContainer;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private java.util.List<Map<String, Object>> allGroups = new java.util.ArrayList<>();
    
    public GroupListPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.groupService = new GroupService();
        
        initComponents();
        loadGroups();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        
        // Header panel với search bar và icon button
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        // Search bar
        JPanel searchBarPanel = createSearchBar();
        
        headerPanel.add(searchBarPanel, BorderLayout.CENTER);
        
        // Container cho danh sách nhóm
        groupsContainer = new JPanel();
        groupsContainer.setLayout(new BoxLayout(groupsContainer, BoxLayout.Y_AXIS));
        groupsContainer.setBackground(BG_COLOR);
        
        scrollPane = new JScrollPane(groupsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Tạo search bar với icon search bên trong
     */
    private JPanel createSearchBar() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setPreferredSize(new Dimension(240, 38));
        
        // TextField
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 38, 8, 10)
        ));
        searchField.setBackground(new Color(245, 245, 245));
        
        // Thêm event listener: chỉ search khi nhấn Enter
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        // Icon panel overlay - có thể click
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        iconPanel.setOpaque(false);
        iconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm click listener cho icon panel
        iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                performSearch();
            }
        });
        
        try {
            ImageIcon searchIcon = new ImageIcon("icons/search.png");
            Image scaledImage = searchIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconPanel.add(iconLabel);
        } catch (Exception e) {
            // Fallback text
            JLabel iconLabel = new JLabel("Tim");
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            iconPanel.add(iconLabel);
        }
        
        // Layer panel để đặt icon lên trên text field
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(240, 38));
        
        searchField.setBounds(0, 0, 240, 38);
        iconPanel.setBounds(0, 0, 40, 38);
        
        layeredPane.add(searchField, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(iconPanel, JLayeredPane.PALETTE_LAYER);
        
        container.add(layeredPane, BorderLayout.CENTER);
        
        return container;
    }
    
    /**
     * THỰC HIỆN TÌM KIẾM - chỉ gọi khi bấm icon hoặc Enter
     */
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // Nếu rỗng, hiển thị lại tất cả nhóm
            displayGroups(allGroups);
        } else {
            // Tìm kiếm theo tên nhóm
            filterGroups(searchText);
        }
    }
    
    /**
     * LỌC NHÓM THEO TỪ KHÓA TÌM KIẾM
     */
    private void filterGroups(String searchText) {
        if (allGroups == null || allGroups.isEmpty()) {
            return;
        }
        
        List<Map<String, Object>> filteredGroups = new java.util.ArrayList<>();
        String lowerSearchText = searchText.toLowerCase();
        
        for (Map<String, Object> group : allGroups) {
            String groupName = (String) group.get("group_name");
            if (groupName != null && groupName.toLowerCase().contains(lowerSearchText)) {
                filteredGroups.add(group);
            }
        }
        
        displayGroups(filteredGroups);
    }
    
    /**
     * Refresh danh sách nhóm
     */
    public void refreshGroupList() {
        loadGroups();
    }
    
    public void loadGroups() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return groupService.getUserGroups(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> groups = get();
                    allGroups = groups; // Lưu lại danh sách đầy đủ
                    displayGroups(groups);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayGroups(List<Map<String, Object>> groups) {
        groupsContainer.removeAll();
        
        if (groups == null || groups.isEmpty()) {
            String searchText = searchField.getText().trim();
            JLabel emptyLabel;
            
            if (!searchText.isEmpty()) {
                // Đang tìm kiếm nhưng không có kết quả
                emptyLabel = new JLabel("<html><center>Không tìm thấy nhóm<br>cho '" + searchText + "'</center></html>");
            } else {
                // Chưa có nhóm nào
                emptyLabel = new JLabel("Chưa có nhóm nào");
            }
            
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 0, 0));
            groupsContainer.add(emptyLabel);
        } else {
            for (Map<String, Object> group : groups) {
                GroupItemPanel groupItem = new GroupItemPanel(group);
                groupsContainer.add(groupItem);
            }
        }
        
        groupsContainer.revalidate();
        groupsContainer.repaint();
    }
    
    /**
     * Panel cho từng nhóm
     */
    private class GroupItemPanel extends JPanel {
        
        private Map<String, Object> groupData;
        private static final Color ENCRYPTED_COLOR = new Color(0, 150, 80); // Màu xanh lá cho E2E
        
        public GroupItemPanel(Map<String, Object> groupData) {
            this.groupData = groupData;
            
            setLayout(new BorderLayout(12, 0));
            setBackground(Color.WHITE);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(10, 15, 10, 15)
            ));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Kiểm tra nhóm có mã hóa không
            boolean isEncrypted = groupData.get("is_encrypted") != null 
                                  && (Boolean) groupData.get("is_encrypted");
            
            // Icon nhóm - đơn giản hóa, chỉ dùng 1 label
            JLabel iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(50, 50));
            iconLabel.setOpaque(true);
            iconLabel.setBackground(isEncrypted ? ENCRYPTED_COLOR : PRIMARY_COLOR);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            
            String groupName = (String) groupData.get("group_name");
            String initials = getGroupInitials(groupName);
            iconLabel.setText(initials);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            iconLabel.setForeground(Color.WHITE);
            
            // Thông tin nhóm
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            
            // Tên nhóm (không cần badge mã hóa vì đã có màu xanh)
            JLabel nameLabel = new JLabel(groupName);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setForeground(isEncrypted ? ENCRYPTED_COLOR : new Color(50, 50, 50));
            if (isEncrypted) {
                nameLabel.setToolTipText("Nhóm mã hóa đầu cuối (E2E)");
            }
            
            String role = (String) groupData.get("role");
            int memberCount = ((Number) groupData.get("member_count")).intValue();
            String subtitle = memberCount + " thành viên";
            if ("admin".equals(role)) {
                subtitle = "Quản trị viên • " + subtitle;
            }
            if (isEncrypted) {
                subtitle = "E2E • " + subtitle;
            }
            
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitleLabel.setForeground(new Color(120, 120, 120));
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(4));
            infoPanel.add(subtitleLabel);
            
            add(iconLabel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
            
            // Click handler
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openGroupChat();
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(new Color(240, 245, 250));
                    infoPanel.setBackground(new Color(240, 245, 250));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(Color.WHITE);
                    infoPanel.setBackground(Color.WHITE);
                }
                
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showGroupMenu(e.getX(), e.getY());
                    }
                }
            });
        }
        
        private String getGroupInitials(String groupName) {
            if (groupName == null || groupName.isEmpty()) {
                return "G";
            }
            
            String[] words = groupName.trim().split("\\s+");
            if (words.length == 1) {
                return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
            } else {
                return (words[0].substring(0, 1) + words[words.length - 1].substring(0, 1)).toUpperCase();
            }
        }
        
        private void openGroupChat() {
            int groupId = ((Number) groupData.get("id")).intValue();
            String groupName = (String) groupData.get("group_name");
            String role = (String) groupData.get("role");
            boolean isAdmin = "admin".equals(role);
            boolean isEncrypted = groupData.get("is_encrypted") != null 
                                  && (Boolean) groupData.get("is_encrypted");
            
            // Mở GroupChatPanel với thông tin mã hóa
            mainFrame.openGroupChat(groupId, groupName, isAdmin, isEncrypted);
        }
        
        private void showGroupMenu(int x, int y) {
            int groupId = ((Number) groupData.get("id")).intValue();
            String role = (String) groupData.get("role");
            boolean isAdmin = "admin".equals(role);
            
            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(Color.WHITE);
            menu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            
            // Xem thông tin
            JMenuItem infoItem = createMenuItem("Thông tin nhóm");
            infoItem.addActionListener(e -> showGroupInfo());
            menu.add(infoItem);
            
            // Thêm thành viên
            JMenuItem addMemberItem = createMenuItem("Thêm thành viên");
            addMemberItem.addActionListener(e -> showAddMemberDialog());
            menu.add(addMemberItem);
            
            if (isAdmin) {
                menu.addSeparator();
                
                // Đổi tên nhóm
                JMenuItem renameItem = createMenuItem("Đổi tên nhóm");
                renameItem.addActionListener(e -> showRenameDialog());
                menu.add(renameItem);
                
                // Quản lý thành viên
                JMenuItem manageMembersItem = createMenuItem("Quản lý thành viên");
                manageMembersItem.addActionListener(e -> showManageMembersDialog());
                menu.add(manageMembersItem);
            }
            
            menu.addSeparator();
            
            // Rời nhóm
            JMenuItem leaveItem = createMenuItem("Rời nhóm");
            leaveItem.setForeground(Color.RED);
            leaveItem.addActionListener(e -> handleLeaveGroup());
            menu.add(leaveItem);
            
            menu.show(this, x, y);
        }
        
        private JMenuItem createMenuItem(String text) {
            JMenuItem item = new JMenuItem(text);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            item.setBorder(new EmptyBorder(8, 12, 8, 12));
            return item;
        }
        
        private void showGroupInfo() {
            int groupId = ((Number) groupData.get("id")).intValue();
            
            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
                @Override
                protected Map<String, Object> doInBackground() {
                    return groupService.getGroupInfo(groupId);
                }
                
                @Override
                protected void done() {
                    try {
                        Map<String, Object> info = get();
                        if (info != null) {
                            String groupName = (String) info.get("group_name");
                            String createdBy = (String) info.get("created_by");
                            int memberCount = ((Number) info.get("member_count")).intValue();
                            
                            String message = "Tên nhóm: " + groupName + "\n";
                            message += "Người tạo: " + createdBy + "\n";
                            message += "Số thành viên: " + memberCount;
                            
                            JOptionPane.showMessageDialog(GroupListPanel.this,
                                message,
                                "Thông tin nhóm",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            worker.execute();
        }
        
        private void showRenameDialog() {
            int groupId = ((Number) groupData.get("id")).intValue();
            String currentName = (String) groupData.get("group_name");
            
            String newName = JOptionPane.showInputDialog(GroupListPanel.this,
                "Nhập tên nhóm mới:",
                currentName);
            
            if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentName)) {
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
                                JOptionPane.showMessageDialog(GroupListPanel.this,
                                    "Đổi tên nhóm thành công!",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                                loadGroups();
                            } else {
                                JOptionPane.showMessageDialog(GroupListPanel.this,
                                    "Không thể đổi tên nhóm. Bạn phải là admin!",
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
        
        private void showAddMemberDialog() {
            int groupId = ((Number) groupData.get("id")).intValue();
            
            JOptionPane.showMessageDialog(GroupListPanel.this,
                "Chức năng thêm thành viên đang được phát triển...",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void showManageMembersDialog() {
            int groupId = ((Number) groupData.get("id")).intValue();
            
            JOptionPane.showMessageDialog(GroupListPanel.this,
                "Chức năng quản lý thành viên đang được phát triển...",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void handleLeaveGroup() {
            int groupId = ((Number) groupData.get("id")).intValue();
            String groupName = (String) groupData.get("group_name");
            String role = (String) groupData.get("role");
            
            int confirm = JOptionPane.showConfirmDialog(GroupListPanel.this,
                "Bạn có chắc muốn rời nhóm \"" + groupName + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Nếu là admin, kiểm tra số lượng admin
                if ("admin".equals(role)) {
                    SwingWorker<Integer, Void> checkWorker = new SwingWorker<>() {
                        @Override
                        protected Integer doInBackground() {
                            return groupService.countAdmins(groupId);
                        }
                        
                        @Override
                        protected void done() {
                            try {
                                int adminCount = get();
                                if (adminCount <= 1) {
                                    JOptionPane.showMessageDialog(GroupListPanel.this,
                                        "Không thể rời nhóm vì bạn là admin duy nhất!\nHãy gán quyền admin cho người khác trước.",
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                } else {
                                    performLeaveGroup(groupId);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    checkWorker.execute();
                } else {
                    performLeaveGroup(groupId);
                }
            }
        }
        
        private void performLeaveGroup(int groupId) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return groupService.removeMember(groupId, mainFrame.getUsername(), mainFrame.getUsername());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(GroupListPanel.this,
                                "Đã rời nhóm thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                            loadGroups();
                        } else {
                            JOptionPane.showMessageDialog(GroupListPanel.this,
                                "Không thể rời nhóm!",
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
}
