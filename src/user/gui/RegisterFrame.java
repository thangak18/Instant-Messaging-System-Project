package user.gui;

import javax.swing.*;
import java.awt.*;
import user.service.UserService;
import java.util.Calendar;
import java.util.Date;

/**
 * Giao diện đăng ký - Modern UI với đầy đủ logic
 * Phong cách Zalo, validation, và xử lý database
 */
public class RegisterFrame extends JFrame {
    // Colors - Zalo Style
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    private static final Color PRIMARY_DARK = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    private static final Color PLACEHOLDER_COLOR = new Color(153, 153, 153);
    
    private JTextField usernameField, fullNameField, emailField, addressField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;
    private JSpinner birthDateSpinner;
    private JButton registerButton, backButton;
    private JLabel titleLabel, subtitleLabel;
    
    public RegisterFrame() {
        initializeComponents();
        setupLayout();
        applyModernStyle();
    }
    
    private void initializeComponents() {
        setTitle("Đăng ký tài khoản - InstantChat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 750);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Tiêu đề
        titleLabel = new JLabel("Tạo tài khoản mới", JLabel.CENTER);
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        subtitleLabel = new JLabel("Điền thông tin để bắt đầu", JLabel.CENTER);
        subtitleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        
        // Các trường nhập liệu với placeholder
        usernameField = createStyledTextField("Tên đăng nhập");
        passwordField = createStyledPasswordField("Mật khẩu");
        confirmPasswordField = createStyledPasswordField("Xác nhận mật khẩu");
        fullNameField = createStyledTextField("Họ và tên");
        emailField = createStyledTextField("Email");
        addressField = createStyledTextField("Địa chỉ");
        
        // Gender combo
        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        genderCombo.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        genderCombo.setPreferredSize(new Dimension(300, 45));
        genderCombo.setBackground(Color.WHITE);
        genderCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Birth date spinner
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 0, 1); // Default: 01/01/2000
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        birthDateSpinner = new JSpinner(dateModel);
        birthDateSpinner.setEditor(new JSpinner.DateEditor(birthDateSpinner, "dd/MM/yyyy"));
        birthDateSpinner.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        birthDateSpinner.setPreferredSize(new Dimension(300, 45));
        birthDateSpinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Buttons
        registerButton = createPrimaryButton("ĐĂNG KÝ");
        registerButton.addActionListener(e -> handleRegister());
        
        backButton = createSecondaryButton("Quay lại đăng nhập");
        backButton.addActionListener(e -> {
            dispose();
        });
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 40, 8, 40);
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 40, 3, 40);
        add(titleLabel, gbc);
        
        // Subtitle
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        add(subtitleLabel, gbc);
        
        // Username
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 40, 8, 40);
        add(usernameField, gbc);
        
        // Password
        gbc.gridy = 3;
        add(passwordField, gbc);
        
        // Confirm password
        gbc.gridy = 4;
        add(confirmPasswordField, gbc);
        
        // Full name
        gbc.gridy = 5;
        add(fullNameField, gbc);
        
        // Email
        gbc.gridy = 6;
        add(emailField, gbc);
        
        // Address
        gbc.gridy = 7;
        add(addressField, gbc);
        
        // Gender
        gbc.gridy = 8;
        add(genderCombo, gbc);
        
        // Birth date
        gbc.gridy = 9;
        add(birthDateSpinner, gbc);
        
        // Register button
        gbc.gridy = 10;
        gbc.insets = new Insets(15, 40, 8, 40);
        add(registerButton, gbc);
        
        // Back button
        gbc.gridy = 11;
        gbc.insets = new Insets(8, 40, 20, 40);
        add(backButton, gbc);
    }
    
    private void applyModernStyle() {
        addHoverEffect(registerButton, PRIMARY_COLOR, PRIMARY_DARK);
        addHoverEffect(backButton, Color.WHITE, new Color(240, 240, 240));
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
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
        field.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
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
        button.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
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
        button.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
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
    // LOGIC XỬ LÝ ĐĂNG KÝ
    // ========================================
    
    private void handleRegister() {
        // Lấy dữ liệu
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        String confirmPassword = String.valueOf(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        Date birthDate = (Date) birthDateSpinner.getValue();
        
        // Validation
        if (!validateInputs(username, password, confirmPassword, fullName, email, address, birthDate)) {
            return;
        }
        
        // Disable button
        registerButton.setEnabled(false);
        registerButton.setText("Đang đăng ký...");
        
        // Register async
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                UserService userService = new UserService();
                java.sql.Date sqlDate = new java.sql.Date(birthDate.getTime());
                return userService.registerUser(username, password, fullName, email, address, sqlDate, gender);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Đăng ký thành công!\nBạn có thể đăng nhập ngay bây giờ.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Đóng RegisterFrame
                        dispose();
                        
                    } else {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Đăng ký thất bại!\nTên đăng nhập hoặc email đã tồn tại.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Reset button
                        registerButton.setEnabled(true);
                        registerButton.setText("ĐĂNG KÝ");
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "Lỗi kết nối database!\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Reset button
                    registerButton.setEnabled(true);
                    registerButton.setText("ĐĂNG KÝ");
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean validateInputs(String username, String password, String confirmPassword, 
                                   String fullName, String email, String address, Date birthDate) {
        // Check placeholders
        if (username.equals("Tên đăng nhập") || username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            return false;
        }
        
        if (password.equals("Mật khẩu") || password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            return false;
        }
        
        if (confirmPassword.equals("Xác nhận mật khẩu") || confirmPassword.isEmpty()) {
            showError("Vui lòng xác nhận mật khẩu!");
            return false;
        }
        
        if (fullName.equals("Họ và tên") || fullName.isEmpty()) {
            showError("Vui lòng nhập họ và tên!");
            return false;
        }
        
        if (email.equals("Email") || email.isEmpty()) {
            showError("Vui lòng nhập email!");
            return false;
        }
        
        if (address.equals("Địa chỉ") || address.isEmpty()) {
            showError("Vui lòng nhập địa chỉ!");
            return false;
        }
        
        // Validate username (alphanumeric, 3-20 chars)
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            showError("Tên đăng nhập phải từ 3-20 ký tự, chỉ chứa chữ, số và dấu gạch dưới!");
            return false;
        }
        
        // Validate password (min 6 chars)
        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            return false;
        }
        
        // Check password match
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return false;
        }
        
        // Validate email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Email không hợp lệ!");
            return false;
        }
        
        // Check age (>= 13 years old)
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        
        if (age < 13) {
            showError("Bạn phải từ 13 tuổi trở lên để đăng ký!");
            return false;
        }
        
        if (age > 100) {
            showError("Ngày sinh không hợp lệ!");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
    }
    
    public static void main(String[] args) {
        UIHelper.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            new RegisterFrame().setVisible(true);
        });
    }
}
