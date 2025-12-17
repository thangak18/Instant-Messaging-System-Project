package user.gui;

import user.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Dialog hiển thị trang cá nhân của user
 */
public class UserProfileDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    
    private UserService userService;
    private String username;
    private Map<String, Object> userInfo;
    
    public UserProfileDialog(Frame parent, String username) {
        super(parent, "Trang cá nhân", true);
        this.username = username;
        this.userService = new UserService();
        
        loadUserInfo();
        initComponents();
    }
    
    private void loadUserInfo() {
        userInfo = userService.getUserInfo(username);
    }
    
    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));
        
        // Header với avatar và tên
        JPanel headerPanel = createHeaderPanel();
        
        // Content với thông tin chi tiết
        JPanel contentPanel = createContentPanel();
        
        // Footer với nút đóng
        JPanel footerPanel = createFooterPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        // Avatar
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(100, 100));
        avatarLabel.setMaximumSize(new Dimension(100, 100));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon("icons/user.png");
            Image scaled = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            avatarLabel.setText("👤");
            avatarLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 60));
            avatarLabel.setForeground(Color.WHITE);
        }
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Tên
        String fullName = userInfo != null ? (String) userInfo.get("full_name") : "";
        JLabel nameLabel = new JLabel(fullName != null && !fullName.isEmpty() ? fullName : username);
        nameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username
        JLabel usernameLabel = new JLabel("@" + username);
        usernameLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        usernameLabel.setForeground(new Color(230, 240, 255));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(avatarLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(usernameLabel);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        if (userInfo == null) {
            JLabel errorLabel = new JLabel("Không thể tải thông tin người dùng");
            errorLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
            
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(scrollPane, BorderLayout.CENTER);
            return wrapper;
        }
        
        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Email
        String email = (String) userInfo.get("email");
        if (email != null && !email.isEmpty()) {
            panel.add(createInfoRow("Email", email));
            panel.add(Box.createVerticalStrut(15));
        }
        
        // Giới tính
        String gender = (String) userInfo.get("gender");
        if (gender != null && !gender.isEmpty()) {
            String genderDisplay = gender.equals("male") ? "Nam" : gender.equals("female") ? "Nữ" : "Khác";
            panel.add(createInfoRow("Giới tính", genderDisplay));
            panel.add(Box.createVerticalStrut(15));
        }
        
        // Ngày sinh
        java.sql.Date dob = (java.sql.Date) userInfo.get("dob");
        if (dob != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            panel.add(createInfoRow("Ngày sinh", sdf.format(dob)));
            panel.add(Box.createVerticalStrut(15));
        }
        
        // Địa chỉ
        String address = (String) userInfo.get("address");
        if (address != null && !address.isEmpty()) {
            panel.add(createInfoRow("Địa chỉ", address));
            panel.add(Box.createVerticalStrut(15));
        }
        
        // Ngày tham gia
        java.sql.Timestamp createdAt = (java.sql.Timestamp) userInfo.get("created_at");
        if (createdAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            panel.add(createInfoRow("Ngày tham gia", sdf.format(createdAt)));
        }
        
        // Add glue to push content to top
        panel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }
    
    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 13));
        labelComp.setForeground(new Color(100, 100, 100));
        labelComp.setPreferredSize(new Dimension(120, 25));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        valueComp.setForeground(Color.BLACK);
        
        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);
        
        return row;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(PRIMARY_COLOR);
        closeButton.setOpaque(true);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(120, 38));
        closeButton.addActionListener(e -> dispose());
        
        panel.add(closeButton);
        
        return panel;
    }
}
