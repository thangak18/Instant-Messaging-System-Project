// package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện đăng ký - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class RegisterFrame extends JFrame {
    private JTextField usernameField, fullNameField, emailField, addressField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;
    private JSpinner birthDateSpinner;
    private JButton registerButton, cancelButton;
    
    public RegisterFrame() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setTitle("Đăng ký tài khoản - Chat System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Các trường nhập liệu
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        fullNameField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        
        // Spinner cho ngày sinh
        SpinnerDateModel dateModel = new SpinnerDateModel();
        birthDateSpinner = new JSpinner(dateModel);
        birthDateSpinner.setEditor(new JSpinner.DateEditor(birthDateSpinner, "dd/MM/yyyy"));
        
        // Các nút chức năng
        registerButton = new JButton("Đăng ký");
        cancelButton = new JButton("Hủy");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel chính
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tiêu đề
        JLabel titleLabel = new JLabel("ĐĂNG KÝ TÀI KHOẢN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);
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
        
        // Xác nhận mật khẩu
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(confirmPasswordField, gbc);
        
        // Họ tên
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Họ tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(fullNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        mainPanel.add(emailField, gbc);
        
        // Địa chỉ
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        mainPanel.add(addressField, gbc);
        
        // Giới tính
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7;
        mainPanel.add(genderCombo, gbc);
        
        // Ngày sinh
        gbc.gridx = 0; gbc.gridy = 8;
        mainPanel.add(new JLabel("Ngày sinh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8;
        mainPanel.add(birthDateSpinner, gbc);
        
        // Nút đăng ký
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        mainPanel.add(registerButton, gbc);
        
        // Nút hủy
        gbc.gridx = 1; gbc.gridy = 9;
        mainPanel.add(cancelButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RegisterFrame().setVisible(true);
        });
    }
}
