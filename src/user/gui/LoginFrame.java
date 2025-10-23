// package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện đăng nhập - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, forgotPasswordButton;
    private JLabel titleLabel;
    
    public LoginFrame() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setTitle("Đăng nhập - Chat System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Các trường nhập liệu
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        // Các nút chức năng
        loginButton = new JButton("Đăng nhập");
        registerButton = new JButton("Đăng ký");
        forgotPasswordButton = new JButton("Quên mật khẩu?");
        
        // Label tiêu đề
        titleLabel = new JLabel("CHAT SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel chính
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Tên đăng nhập
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);
        
        // Mật khẩu
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);
        
        // Nút đăng nhập
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        mainPanel.add(loginButton, gbc);
        
        // Nút đăng ký
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        mainPanel.add(registerButton, gbc);
        
        // Nút quên mật khẩu
        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(forgotPasswordButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}
