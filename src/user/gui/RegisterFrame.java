package user.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import user.service.UserService;

/**
 * Giao diện đăng ký tài khoản - Modern UI
 * Kết nối với database để lưu user mới
 */
public class RegisterFrame extends JFrame {
    
    // Colors - Zalo Style
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color PRIMARY_DARK = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    
    private JTextField usernameField, fullNameField, emailField, addressField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;
    private JSpinner birthDateSpinner;
    private JButton registerButton, cancelButton;
    
    private UserService userService;
    
    public RegisterFrame() {
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        applyModernStyle();
        addEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Đăng ký tài khoản - Chat System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Các trường nhập liệu
        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();
        fullNameField = createStyledTextField();
        emailField = createStyledTextField();
        addressField = createStyledTextField();
        
        // Combo box giới tính
        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        genderCombo.setPreferredSize(new Dimension(300, 40));
        
        // Spinner cho ngày sinh
        SpinnerDateModel dateModel = new SpinnerDateModel();
        birthDateSpinner = new JSpinner(dateModel);
        birthDateSpinner.setEditor(new JSpinner.DateEditor(birthDateSpinner, "dd/MM/yyyy"));
        birthDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        birthDateSpinner.setPreferredSize(new Dimension(300, 40));
        
        // Các nút chức năng
        registerButton = createPrimaryButton("ĐĂNG KÝ");
        cancelButton = createSecondaryButton("HỦY");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 20));
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(550, 80));
        
        JLabel titleLabel = new JLabel("ĐĂNG KÝ TÀI KHOẢN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Panel với ScrollPane
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Tên đăng nhập
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Tên đăng nhập: *"), gbc);
        gbc.gridy = row++;
        mainPanel.add(usernameField, gbc);
        
        // Mật khẩu
        gbc.gridy = row++;
        mainPanel.add(createLabel("Mật khẩu: *"), gbc);
        gbc.gridy = row++;
        mainPanel.add(passwordField, gbc);
        
        // Xác nhận mật khẩu
        gbc.gridy = row++;
        mainPanel.add(createLabel("Xác nhận mật khẩu: *"), gbc);
        gbc.gridy = row++;
        mainPanel.add(confirmPasswordField, gbc);
        
        // Họ tên
        gbc.gridy = row++;
        mainPanel.add(createLabel("Họ và tên: *"), gbc);
        gbc.gridy = row++;
        mainPanel.add(fullNameField, gbc);
        
        // Email
        gbc.gridy = row++;
        mainPanel.add(createLabel("Email: *"), gbc);
        gbc.gridy = row++;
        mainPanel.add(emailField, gbc);
        
        // Địa chỉ
        gbc.gridy = row++;
        mainPanel.add(createLabel("Địa chỉ:"), gbc);
        gbc.gridy = row++;
        mainPanel.add(addressField, gbc);
        
        // Giới tính
        gbc.gridy = row++;
        mainPanel.add(createLabel("Giới tính:"), gbc);
        gbc.gridy = row++;
        mainPanel.add(genderCombo, gbc);
        
        // Ngày sinh
        gbc.gridy = row++;
        mainPanel.add(createLabel("Ngày sinh:"), gbc);
        gbc.gridy = row++;
        mainPanel.add(birthDateSpinner, gbc);
        
        // Wrap mainPanel in ScrollPane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void applyModernStyle() {
        // Hover effects
        addHoverEffect(registerButton, PRIMARY_COLOR, PRIMARY_DARK);
        addHoverEffect(cancelButton, Color.WHITE, new Color(240, 240, 240));
    }
    
    private void addEventHandlers() {
        // Nút Đăng ký
        registerButton.addActionListener(e -> handleRegister());
        
        // Nút Hủy
        cancelButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy đăng ký?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        
        // Enter để submit
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleRegister();
                }
            }
        };
        
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
        confirmPasswordField.addKeyListener(enterListener);
        fullNameField.addKeyListener(enterListener);
        emailField.addKeyListener(enterListener);
    }
    
    // ========================================
    // EVENT HANDLER - ĐĂNG KÝ
    // ========================================
    
    private void handleRegister() {
        System.out.println(">>> REGISTER BUTTON CLICKED <<<");
        
        // Lấy dữ liệu từ form
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        String confirmPassword = String.valueOf(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        
        // Validate
        if (!validateInput(username, password, confirmPassword, fullName, email)) {
            return;
        }
        
        // Lấy ngày sinh
        java.util.Date utilDate = (java.util.Date) birthDateSpinner.getValue();
        Date birthDate = new Date(utilDate.getTime());
        
        // Hiển thị loading
        registerButton.setEnabled(false);
        registerButton.setText("Đang xử lý...");
        
        // Chạy trong thread riêng để không block UI
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Gọi UserService để đăng ký
                return userService.registerUser(
                    username, 
                    password, 
                    fullName, 
                    email, 
                    address, 
                    birthDate, 
                    gender
                );
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        // Đăng ký thành công
                        JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "🎉 Đăng ký tài khoản thành công!\n\n" +
                            "Username: " + username + "\n" +
                            "Bạn có thể đăng nhập ngay bây giờ!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Đóng RegisterFrame
                        dispose();
                        
                        // Mở LoginFrame (nếu chưa có)
                        SwingUtilities.invokeLater(() -> {
                            LoginFrame loginFrame = new LoginFrame();
                            loginFrame.setVisible(true);
                        });
                        
                    } else {
                        // Đăng ký thất bại (username/email đã tồn tại)
                        JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "❌ Đăng ký thất bại!\n\n" +
                            "Có thể do:\n" +
                            "• Tên đăng nhập đã tồn tại\n" +
                            "• Email đã được đăng ký\n" +
                            "• Lỗi kết nối database\n\n" +
                            "Vui lòng thử lại với thông tin khác.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        RegisterFrame.this,
                        "❌ Lỗi hệ thống: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    // Reset button
                    registerButton.setEnabled(true);
                    registerButton.setText("ĐĂNG KÝ");
                }
            }
        }.execute();
    }
    
    private boolean validateInput(String username, String password, String confirmPassword, 
                                   String fullName, String email) {
        // Kiểm tra trống
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            usernameField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            passwordField.requestFocus();
            return false;
        }
        
        if (confirmPassword.isEmpty()) {
            showError("Vui lòng xác nhận mật khẩu!");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ tên!");
            fullNameField.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            showError("Vui lòng nhập email!");
            emailField.requestFocus();
            return false;
        }
        
        // Kiểm tra username (chỉ chữ cái, số, underscore)
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            showError("Tên đăng nhập không hợp lệ!\n\n" +
                     "Yêu cầu:\n" +
                     "• Từ 3-20 ký tự\n" +
                     "• Chỉ chứa chữ cái, số và dấu gạch dưới (_)\n" +
                     "• Không có khoảng trắng hoặc ký tự đặc biệt");
            usernameField.requestFocus();
            return false;
        }
        
        // Kiểm tra mật khẩu khớp
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!\n\nVui lòng nhập lại.");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            showError("Mật khẩu quá ngắn!\n\nMật khẩu phải có ít nhất 6 ký tự.");
            passwordField.requestFocus();
            return false;
        }
        
        // Kiểm tra email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Email không hợp lệ!\n\nVí dụ: example@gmail.com");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
    }
    
    // ========================================
    // UI HELPER METHODS
    // ========================================
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR);
        return label;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 45));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(150, 45));
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }
}
