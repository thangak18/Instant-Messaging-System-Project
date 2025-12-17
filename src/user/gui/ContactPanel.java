package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Contact Panel - Thay thế ChatListPanel khi click icon Contact
 * Chứa 4 sections: Bạn bè, Nhóm, Lời mời kết bạn, Lời mời nhóm
 */
public class ContactPanel extends JPanel {
    
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final int PANEL_WIDTH = 350;
    
    private ZaloMainFrame mainFrame;
    
    public ContactPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }
    
    private void initializeUI() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeader();
        
        // Menu options (chỉ 4 menu items, không có content panel)
        JPanel menuPanel = createMenuPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Danh bạ");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 20));
        
        // Right panel with create group button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        // Create Group button
        JButton createGroupButton = new JButton("Tạo Nhóm");
        createGroupButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
        createGroupButton.setForeground(Color.WHITE);
        createGroupButton.setBackground(PRIMARY_COLOR);
        createGroupButton.setBorderPainted(false);
        createGroupButton.setFocusPainted(false);
        createGroupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createGroupButton.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        
        createGroupButton.addActionListener(e -> {
            CreateGroupDialog dialog = new CreateGroupDialog(mainFrame);
            dialog.setVisible(true);
        });
        
        rightPanel.add(createGroupButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        // Menu items (đã bỏ Lời mời vào nhóm)
        panel.add(createMenuItem("👥", "Danh sách bạn bè", "FRIENDS", 0));
        panel.add(createMenuItem("👨‍👩‍👧‍👦", "Danh sách nhóm", "GROUPS", 0));
        panel.add(createMenuItem("👋", "Lời mời kết bạn", "FRIEND_REQUESTS", 0));
        
        return panel;
    }
    
    private JPanel createMenuItem(String icon, String label, String panelKey, int badgeCount) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon + Label
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(UIHelper.getEmojiFontName(), Font.PLAIN, 24));
        
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        
        leftPanel.add(iconLabel);
        leftPanel.add(textLabel);
        
        // Badge (notification count)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        
        if (badgeCount > 0) {
            JLabel badge = new JLabel(String.valueOf(badgeCount));
            badge.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 11));
            badge.setForeground(Color.WHITE);
            badge.setBackground(new Color(255, 59, 48));
            badge.setOpaque(true);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            rightPanel.add(badge);
        }
        
        item.add(leftPanel, BorderLayout.WEST);
        item.add(rightPanel, BorderLayout.EAST);
        
        // Click handler - Thay thế ChatContentPanel bên phải
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mainFrame.showContactContent(panelKey);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                item.setBackground(new Color(240, 242, 245));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });
        
        return item;
    }
    
}
