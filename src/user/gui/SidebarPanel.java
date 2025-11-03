package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Sidebar Panel - Navigation bên trái (60px width)
 * Sử dụng PNG/SVG icons từ Flaticon
 */
public class SidebarPanel extends JPanel {
    
    private static final Color SIDEBAR_COLOR = new Color(0, 132, 255);
    private static final int SIDEBAR_WIDTH = 60;
    
    private ZaloMainFrame mainFrame;
    
    public SidebarPanel(ZaloMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }
    
    private void initializeUI() {
        setBackground(SIDEBAR_COLOR);
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        setLayout(new BorderLayout());
        
        // Top section - User avatar
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // User avatar button
        JButton avatarButton = createIconButton("icons/user.png", "Tài khoản", 36);
        topPanel.add(avatarButton);
        topPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Middle section - Navigation icons
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        
        JButton chatButton = createIconButton("icons/chat.png", "Tin nhắn", 28);
        JButton contactButton = createIconButton("icons/contact.png", "Danh bạ", 28);
        
        // Add click handlers
        chatButton.addActionListener(e -> mainFrame.showChatPanel());
        contactButton.addActionListener(e -> mainFrame.showContactPanel());
        
        navPanel.add(chatButton);
        navPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        navPanel.add(contactButton);
        
        // Bottom section - Settings
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JButton settingsButton = createIconButton("icons/settings.png", "Cài đặt", 28);
        bottomPanel.add(settingsButton);
        
        // Add all sections
        add(topPanel, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Tạo icon button từ file PNG/SVG
     */
    private JButton createIconButton(String iconPath, String tooltip, int iconSize) {
        JButton button = new JButton();
        
        try {
            // Load icon từ file
            ImageIcon originalIcon = new ImageIcon(iconPath);
            
            // Resize icon
            Image image = originalIcon.getImage();
            Image scaledImage = image.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            button.setIcon(scaledIcon);
            
        } catch (Exception e) {
            // Nếu không tìm thấy icon, dùng placeholder text
            button.setText("?");
            button.setFont(new Font("Segoe UI", Font.BOLD, 16));
            button.setForeground(Color.WHITE);
            System.err.println("⚠️ Không tìm thấy icon: " + iconPath);
        }
        
        button.setPreferredSize(new Dimension(50, 50));
        button.setMaximumSize(new Dimension(50, 50));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(true);
                button.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(false);
            }
        });
        
        return button;
    }
}
