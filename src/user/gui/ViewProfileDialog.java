package user.gui;

import user.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Dialog xem thông tin tài khoản (read-only) - Giao diện đẹp
 */
public class ViewProfileDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    
    private ZaloMainFrame mainFrame;
    private UserService userService;
    private JPanel infoContainer;
    
    public ViewProfileDialog(ZaloMainFrame mainFrame) {
        super(mainFrame, "Thông tin tài khoản", true);
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        
        initializeUI();
        loadUserInfo();
    }
    
    private void initializeUI() {
        setSize(500, 650);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        // Header với avatar
        JPanel headerPanel = createHeaderPanel();
        
        // Info container
        infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBackground(BG_COLOR);
        infoContainer.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(infoContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_COLOR);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        
        // Avatar
        JLabel avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username label (sẽ update sau)
        JLabel usernameLabel = new JLabel(mainFrame.getUsername(), SwingConstants.CENTER);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(avatarLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(usernameLabel);
        
        return headerPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton editButton = new JButton("✏️  Chỉnh sửa");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editButton.setBackground(PRIMARY_COLOR);
        editButton.setForeground(Color.WHITE);
        editButton.setPreferredSize(new Dimension(130, 38));
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> {
            dispose();
            UpdateProfileDialog updateDialog = new UpdateProfileDialog(mainFrame);
            updateDialog.setVisible(true);
        });
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setPreferredSize(new Dimension(100, 38));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);
        
        return buttonPanel;
    }
    
    private void loadUserInfo() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return userService.getUserInfo(mainFrame.getUsername());
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> userInfo = get();
                    
                    if (userInfo != null) {
                        infoContainer.removeAll();
                        
                        String username = (String) userInfo.get("username");
                        String fullName = (String) userInfo.get("full_name");
                        String email = (String) userInfo.get("email");
                        String address = (String) userInfo.get("address");
                        java.sql.Date dob = (java.sql.Date) userInfo.get("dob");
                        String gender = (String) userInfo.get("gender");
                        java.sql.Timestamp createdAt = (java.sql.Timestamp) userInfo.get("created_at");
                        
                        // Personal Info Section
                        addSectionTitle("Thông tin cá nhân");
                        
                        addInfoCard("👤 Tên đăng nhập", username != null ? username : "N/A");
                        addInfoCard("✨ Họ và tên", fullName != null && !fullName.isEmpty() ? fullName : "Chưa cập nhật");
                        
                        // Birth date
                        String birthDateStr = "Chưa cập nhật";
                        if (dob != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            birthDateStr = sdf.format(dob);
                        }
                        addInfoCard("🎂 Ngày sinh", birthDateStr);
                        
                        addInfoCard("⚧ Giới tính", gender != null && !gender.isEmpty() ? gender : "Chưa cập nhật");
                        
                        // Contact Info Section
                        infoContainer.add(Box.createVerticalStrut(15));
                        addSectionTitle("Thông tin liên hệ");
                        
                        addInfoCard("📧 Email", email != null && !email.isEmpty() ? email : "Chưa cập nhật");
                        addInfoCard("🏠 Địa chỉ", address != null && !address.isEmpty() ? address : "Chưa cập nhật");
                        
                        // Account Info Section
                        infoContainer.add(Box.createVerticalStrut(15));
                        addSectionTitle("Thông tin tài khoản");
                        
                        // Account created date
                        String createdAtStr = "N/A";
                        if (createdAt != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            createdAtStr = sdf.format(createdAt);
                        }
                        addInfoCard("📅 Ngày tạo tài khoản", createdAtStr);
                        
                        infoContainer.revalidate();
                        infoContainer.repaint();
                    } else {
                        JOptionPane.showMessageDialog(ViewProfileDialog.this,
                            "Không thể tải thông tin tài khoản!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ViewProfileDialog.this,
                        "Lỗi khi tải thông tin: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void addSectionTitle(String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionLabel.setForeground(new Color(80, 80, 80));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        
        infoContainer.add(sectionLabel);
    }
    
    private void addInfoCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComponent.setForeground(new Color(130, 130, 130));
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Value
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueComponent.setForeground(new Color(30, 30, 30));
        valueComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(labelComponent);
        card.add(Box.createVerticalStrut(6));
        card.add(valueComponent);
        
        // Set preferred and maximum size to prevent stretching
        int cardHeight = 70;
        card.setPreferredSize(new Dimension(450, cardHeight));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));
        
        infoContainer.add(card);
        infoContainer.add(Box.createVerticalStrut(10));
    }
}
