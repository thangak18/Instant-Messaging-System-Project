package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Settings Dialog - Hiển thị menu cài đặt
 */
public class SettingsDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    
    private ZaloMainFrame mainFrame;
    
    public SettingsDialog(ZaloMainFrame mainFrame) {
        super(mainFrame, "Cài đặt", true);
        this.mainFrame = mainFrame;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 500);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Cài đặt");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Menu options with ScrollPane
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 1. Xem thông tin tài khoản
        JPanel viewProfileItem = createMenuItem(
            "ℹ️", 
            "Xem thông tin tài khoản", 
            "Xem thông tin cá nhân chi tiết",
            () -> openViewProfileDialog()
        );
        
        // 2. Cập nhật thông tin
        JPanel updateInfoItem = createMenuItem(
            "👤", 
            "Cập nhật thông tin tài khoản", 
            "Thay đổi tên, email, mật khẩu",
            () -> openUpdateProfileDialog()
        );
        
        // 3. Danh sách người đã chặn
        JPanel blockedUsersItem = createMenuItem(
            "🚫", 
            "Danh sách người đã chặn", 
            "Xem và bỏ chặn người dùng",
            () -> openBlockedUsersDialog()
        );
        
        // 4. Đăng xuất
        JPanel logoutItem = createMenuItem(
            "🚪", 
            "Đăng xuất tài khoản", 
            "Thoát khỏi ứng dụng",
            () -> logout()
        );
        
        menuPanel.add(viewProfileItem);
        menuPanel.add(createSeparator());
        menuPanel.add(updateInfoItem);
        menuPanel.add(createSeparator());
        menuPanel.add(blockedUsersItem);
        menuPanel.add(createSeparator());
        menuPanel.add(logoutItem);
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createMenuItem(String icon, String title, String subtitle, Runnable action) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subtitleLabel);
        
        item.add(iconLabel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        
        // Click handler
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
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
    
    private JPanel createSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(new Color(230, 230, 230));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setPreferredSize(new Dimension(0, 1));
        return separator;
    }
    
    private void openViewProfileDialog() {
        dispose();
        ViewProfileDialog dialog = new ViewProfileDialog(mainFrame);
        dialog.setVisible(true);
    }
    
    private void openUpdateProfileDialog() {
        dispose();
        UpdateProfileDialog dialog = new UpdateProfileDialog(mainFrame);
        dialog.setVisible(true);
    }
    
    private void openBlockedUsersDialog() {
        dispose();
        BlockedUsersDialog dialog = new BlockedUsersDialog(mainFrame);
        dialog.setVisible(true);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Disconnect socket
            if (mainFrame.getSocketClient() != null) {
                mainFrame.getSocketClient().disconnect();
            }
            
            // Đóng dialog trước
            dispose();
            
            // Đóng main frame
            mainFrame.setVisible(false);
            mainFrame.dispose();
            
            // Open login frame
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}
