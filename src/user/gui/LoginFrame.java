package user.gui;

import javax.swing.*;
import java.awt.*;
import user.service.UserService;
import java.util.Map;

/**
 * Giao diện đăng nhập hiện đại - Phong cách Zalo
 * Modern UI với gradient background, rounded corners, và icons
 */
public class LoginFrame extends JFrame {
    // Colors - Zalo Style
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);      // Zalo Blue
    private static final Color PRIMARY_DARK = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    private static final Color PLACEHOLDER_COLOR = new Color(153, 153, 153);
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, forgotPasswordButton;
    private JLabel titleLabel, logoLabel, welcomeLabel;
    
    public LoginFrame() {
        System.out.println("=== LoginFrame Constructor Started ===");
        initializeComponents();
        setupLayout();
        applyModernStyle();
        System.out.println("=== LoginFrame Constructor Finished ===");
    }
    
    private void initializeComponents() {
        setTitle("Đăng nhập - InstantChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Logo và tiêu đề
        logoLabel = new JLabel("💬", JLabel.CENTER);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        
        titleLabel = new JLabel("InstantChat", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        welcomeLabel = new JLabel("Đăng nhập để tiếp tục", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(TEXT_COLOR);
        
        // Các trường nhập liệu với placeholder
        usernameField = createStyledTextField("Tên đăng nhập hoặc email");
        passwordField = createStyledPasswordField("Mật khẩu");
        
        // Nút đăng nhập - Primary Button
        loginButton = createPrimaryButton("ĐĂNG NHẬP");
        System.out.println("DEBUG: loginButton created = " + loginButton);
        loginButton.addActionListener(e -> {
            System.out.println(">>> LOGIN BUTTON CLICKED <<<");
            handleLogin();
        });
        System.out.println("DEBUG: loginButton listener added");
        
        // Nút đăng ký - Secondary Button
        registerButton = createSecondaryButton("Đăng ký tài khoản");
        registerButton.addActionListener(e -> {
            System.out.println(">>> REGISTER BUTTON CLICKED <<<");
            handleRegister();
        });
        
        // Nút quên mật khẩu - Link style
        forgotPasswordButton = createLinkButton("Quên mật khẩu?");
        forgotPasswordButton.addActionListener(e -> handleForgotPassword());
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 40, 10, 40);
        
        // Logo
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 40, 10, 40);
        add(logoLabel, gbc);
        
        // Title
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 5, 40);
        add(titleLabel, gbc);
        
        // Welcome text
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 30, 40);
        add(welcomeLabel, gbc);
        
        // Username field
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 40, 10, 40);
        add(usernameField, gbc);
        
        // Password field
        gbc.gridy = 4;
        add(passwordField, gbc);
        
        // Forgot password (right aligned)
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 40, 15, 40);
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        forgotPanel.setOpaque(false);
        forgotPanel.add(forgotPasswordButton);
        add(forgotPanel, gbc);
        
        // Login button
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 40, 15, 40);
        add(loginButton, gbc);
        
        // Divider with "hoặc"
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 40, 10, 40);
        add(createDividerPanel(), gbc);
        
        // Register button
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 40, 40, 40);
        add(registerButton, gbc);
    }
    
    private void applyModernStyle() {
        // Sẽ thêm các hiệu ứng hover và focus listeners
        addHoverEffect(loginButton, PRIMARY_COLOR, PRIMARY_DARK);
        addHoverEffect(registerButton, Color.WHITE, new Color(240, 240, 240));
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Placeholder effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(PLACEHOLDER_COLOR);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        field.setPreferredSize(new Dimension(300, 45));
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
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 45));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createDividerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Left line
        JSeparator leftLine = new JSeparator();
        leftLine.setPreferredSize(new Dimension(120, 1));
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(leftLine, gbc);
        
        // "hoặc" text
        JLabel orLabel = new JLabel(" hoặc ");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orLabel.setForeground(PLACEHOLDER_COLOR);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(orLabel, gbc);
        
        // Right line
        JSeparator rightLine = new JSeparator();
        rightLine.setPreferredSize(new Dimension(120, 1));
        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rightLine, gbc);
        
        return panel;
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
    
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        
        System.out.println("DEBUG: handleLogin called");
        System.out.println("DEBUG: usernameOrEmail = '" + usernameOrEmail + "'");
        
        // Kiểm tra placeholder
        if (usernameOrEmail.equals("Tên đăng nhập hoặc email") || usernameOrEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tên đăng nhập hoặc email!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.equals("Mật khẩu") || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập mật khẩu!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Hiển thị loading
        loginButton.setEnabled(false);
        loginButton.setText("Đang đăng nhập...");
        
        // Xác thực với database (async)
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                UserService userService = new UserService();
                return userService.login(usernameOrEmail, password);
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> result = get();
                    
                    if ((boolean) result.get("success")) {
                        // Đăng nhập thành công
                        String username = (String) result.get("username");
                        String fullName = (String) result.get("full_name");
                        
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Đăng nhập thành công!\nChào mừng " + fullName,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Đóng LoginFrame và mở UserMainFrame
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            UserMainFrame mainFrame = new UserMainFrame(username);
                            mainFrame.setVisible(true);
                        });
                        
                    } else {
                        // Đăng nhập thất bại
                        String message = (String) result.get("message");
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            message,
                            "Đăng nhập thất bại",
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Reset button
                        loginButton.setEnabled(true);
                        loginButton.setText("ĐĂNG NHẬP");
                        passwordField.setText("");
                        passwordField.setForeground(PLACEHOLDER_COLOR);
                        passwordField.setEchoChar((char) 0);
                        passwordField.setText("Mật khẩu");
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Lỗi kết nối đến database!\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Reset button
                    loginButton.setEnabled(true);
                    loginButton.setText("ĐĂNG NHẬP");
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleRegister() {
        // Mở RegisterFrame
        SwingUtilities.invokeLater(() -> {
            RegisterFrame registerFrame = new RegisterFrame();
            registerFrame.setVisible(true);
        });
        
        // Đóng LoginFrame (tùy chọn)
        // this.dispose();
    }
    
    private void handleForgotPassword() {
        String email = JOptionPane.showInputDialog(this,
            "Nhập email của bạn để khôi phục mật khẩu:",
            "Quên mật khẩu",
            JOptionPane.QUESTION_MESSAGE);
        
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            
            // Validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this,
                    "Email không hợp lệ! Vui lòng nhập đúng định dạng.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show loading
            JDialog loadingDialog = new JDialog(this, "Đang xử lý...", true);
            JLabel loadingLabel = new JLabel("⏳ Đang gửi mật khẩu tạm thời...", JLabel.CENTER);
            loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            loadingLabel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
            loadingDialog.add(loadingLabel);
            loadingDialog.setSize(300, 120);
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            final String finalEmail = email;
            
            // Process in background
            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<Map<String, Object>, Void>() {
                @Override
                protected Map<String, Object> doInBackground() throws Exception {
                    UserService userService = new UserService();
                    return userService.sendResetPasswordEmail(finalEmail);
                }
                
                @Override
                protected void done() {
                    loadingDialog.dispose();
                    
                    try {
                        Map<String, Object> result = get();
                        
                        if ((boolean) result.get("success")) {
                            String temporaryPassword = (String) result.get("temporary_password");
                            
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                "✅ Mật khẩu tạm thời đã được gửi đến email!\n\n" +
                                "Vui lòng kiểm tra email và làm theo hướng dẫn.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // Mở ResetPasswordFrame
                            SwingUtilities.invokeLater(() -> {
                                ResetPasswordFrame resetFrame = new ResetPasswordFrame(finalEmail, temporaryPassword);
                                resetFrame.setVisible(true);
                            });
                            
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                result.get("message"),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Lỗi: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            
            // Show loading dialog (will be closed when worker is done)
            loadingDialog.setVisible(true);
        }
    }
}
