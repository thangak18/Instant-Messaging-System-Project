package user.gui;

import javax.swing.*;
import java.awt.*;
import user.service.UserService;

/**
 * Giao diện đặt lại mật khẩu sau khi nhận mã từ email
 */
public class ResetPasswordFrame extends JFrame {
    // Colors - Zalo Style
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color PRIMARY_DARK = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    private static final Color PLACEHOLDER_COLOR = new Color(153, 153, 153);
    
    private String email;
    private String temporaryPassword;
    
    private JPasswordField tempPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton resetButton;
    private JLabel titleLabel, emailLabel;
    
    public ResetPasswordFrame(String email, String temporaryPassword) {
        this.email = email;
        this.temporaryPassword = temporaryPassword;
        
        initializeComponents();
        setupLayout();
        applyModernStyle();
    }
    
    private void initializeComponents() {
        setTitle("Đặt lại mật khẩu - InstantChat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Header
        titleLabel = new JLabel("🔐 Đặt lại mật khẩu", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        emailLabel = new JLabel("Email: " + email, JLabel.CENTER);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(PLACEHOLDER_COLOR);
        
        // Fields
        tempPasswordField = createStyledPasswordField("Mật khẩu tạm từ email");
        newPasswordField = createStyledPasswordField("Mật khẩu mới");
        confirmPasswordField = createStyledPasswordField("Xác nhận mật khẩu mới");
        
        // Button
        resetButton = createPrimaryButton("CẬP NHẬT MẬT KHẨU");
        resetButton.addActionListener(e -> handleResetPassword());
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 40, 10, 40);
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 40, 10, 40);
        add(titleLabel, gbc);
        
        // Email label
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 30, 40);
        add(emailLabel, gbc);
        
        // Info panel
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 40, 20, 40);
        add(createInfoPanel(), gbc);
        
        // Temp password field
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 40, 10, 40);
        add(createLabeledField("Mật khẩu tạm từ email:", tempPasswordField), gbc);
        
        // New password field
        gbc.gridy = 4;
        add(createLabeledField("Mật khẩu mới:", newPasswordField), gbc);
        
        // Confirm password field
        gbc.gridy = 5;
        add(createLabeledField("Xác nhận mật khẩu mới:", confirmPasswordField), gbc);
        
        // Reset button
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 40, 40, 40);
        add(resetButton, gbc);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(255, 248, 220)); // Light yellow
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel icon = new JLabel("ℹ️ Hướng dẫn:");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel step1 = new JLabel("1. Kiểm tra email để lấy mật khẩu tạm thời");
        step1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        step1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel step2 = new JLabel("2. Nhập mật khẩu tạm vào ô đầu tiên");
        step2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        step2.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel step3 = new JLabel("3. Nhập mật khẩu mới và xác nhận");
        step3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        step3.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(icon);
        panel.add(Box.createVerticalStrut(5));
        panel.add(step1);
        panel.add(step2);
        panel.add(step3);
        
        return panel;
    }
    
    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        
        return panel;
    }
    
    private void applyModernStyle() {
        addHoverEffect(resetButton, PRIMARY_COLOR, PRIMARY_DARK);
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        field.setPreferredSize(new Dimension(300, 40));
        field.setMaximumSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Placeholder effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(PLACEHOLDER_COLOR);
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setPreferredSize(new Dimension(300, 45));
        button.setMaximumSize(new Dimension(300, 45));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }
    
    // ========================================
    // EVENT HANDLERS
    // ========================================
    
    private void handleResetPassword() {
        String inputTempPassword = String.valueOf(tempPasswordField.getPassword());
        String newPassword = String.valueOf(newPasswordField.getPassword());
        String confirmPassword = String.valueOf(confirmPasswordField.getPassword());
        
        // Validate input
        if (!validateInput(inputTempPassword, newPassword, confirmPassword)) {
            return;
        }
        
        // Disable button
        resetButton.setEnabled(false);
        resetButton.setText("Đang cập nhật...");
        
        // Process in background
        SwingWorker<java.util.Map<String, Object>, Void> worker = new SwingWorker<java.util.Map<String, Object>, Void>() {
            @Override
            protected java.util.Map<String, Object> doInBackground() throws Exception {
                UserService userService = new UserService();
                return userService.resetPasswordWithTemporary(email, inputTempPassword, newPassword);
            }
            
            @Override
            protected void done() {
                try {
                    java.util.Map<String, Object> result = get();
                    
                    if ((boolean) result.get("success")) {
                        JOptionPane.showMessageDialog(ResetPasswordFrame.this,
                            "✅ Đổi mật khẩu thành công!\n\n" +
                            "Bạn có thể đăng nhập bằng mật khẩu mới.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Đóng frame này và mở LoginFrame
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            LoginFrame loginFrame = new LoginFrame();
                            loginFrame.setVisible(true);
                        });
                        
                    } else {
                        JOptionPane.showMessageDialog(ResetPasswordFrame.this,
                            result.get("message"),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Reset button
                        resetButton.setEnabled(true);
                        resetButton.setText("CẬP NHẬT MẬT KHẨU");
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ResetPasswordFrame.this,
                        "Lỗi: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    
                    resetButton.setEnabled(true);
                    resetButton.setText("CẬP NHẬT MẬT KHẨU");
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean validateInput(String tempPass, String newPass, String confirmPass) {
        // Check placeholders
        if (tempPass.equals("Mật khẩu tạm từ email") || tempPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập mật khẩu tạm từ email!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (newPass.equals("Mật khẩu mới") || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập mật khẩu mới!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (confirmPass.equals("Xác nhận mật khẩu mới") || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng xác nhận mật khẩu mới!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check temp password matches
        if (!tempPass.equals(temporaryPassword)) {
            JOptionPane.showMessageDialog(this,
                "❌ Mật khẩu tạm không đúng!\n\n" +
                "Vui lòng kiểm tra lại email.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check new password length
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Mật khẩu mới phải có ít nhất 6 ký tự!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check password match
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                "Mật khẩu xác nhận không khớp!",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
}
