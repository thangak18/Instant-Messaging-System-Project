package user.gui;

import user.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Dialog cập nhật thông tin tài khoản đầy đủ
 */
public class UpdateProfileDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);

    private ZaloMainFrame mainFrame;
    private UserService userService;

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField addressField;
    private JSpinner birthDateSpinner;
    private JComboBox<String> genderComboBox;
    private JButton saveButton;

    public UpdateProfileDialog(ZaloMainFrame mainFrame) {
        super(mainFrame, "Cập nhật thông tin", true);
        this.mainFrame = mainFrame;
        this.userService = new UserService();

        initializeUI();
        loadCurrentInfo();
    }

    private void initializeUI() {
        setSize(550, 600);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Cập nhật thông tin tài khoản");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Form panel with ScrollPane
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));

        // Username (read-only)
        addLabel(formPanel, "Tên đăng nhập:");
        JLabel usernameValue = new JLabel(mainFrame.getUsername());
        usernameValue.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        usernameValue.setForeground(new Color(100, 100, 100));
        formPanel.add(usernameValue);
        formPanel.add(Box.createVerticalStrut(15));

        // Full name
        addLabel(formPanel, "Họ và tên:");
        fullNameField = createTextField();
        formPanel.add(fullNameField);
        formPanel.add(Box.createVerticalStrut(15));

        // Email
        addLabel(formPanel, "Email:");
        emailField = createTextField();
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(15));

        // Birth date
        addLabel(formPanel, "Ngày sinh:");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        birthDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(birthDateSpinner, "dd/MM/yyyy");
        birthDateSpinner.setEditor(dateEditor);
        birthDateSpinner.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        birthDateSpinner.setPreferredSize(new Dimension(200, 35));
        birthDateSpinner.setMaximumSize(new Dimension(200, 35));
        birthDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(birthDateSpinner);
        formPanel.add(Box.createVerticalStrut(15));

        // Gender
        addLabel(formPanel, "Giới tính:");
        genderComboBox = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
        genderComboBox.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        genderComboBox.setPreferredSize(new Dimension(150, 35));
        genderComboBox.setMaximumSize(new Dimension(150, 35));
        genderComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(genderComboBox);
        formPanel.add(Box.createVerticalStrut(15));

        // Address
        addLabel(formPanel, "Địa chỉ:");
        addressField = createTextField();
        formPanel.add(addressField);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());

        saveButton = new JButton("Lưu");
        saveButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        saveButton.setBackground(PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true); // Required for macOS
        saveButton.setContentAreaFilled(true); // Ensure background is painted
        saveButton.addActionListener(e -> saveChanges());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void loadCurrentInfo() {
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
                        String fullName = (String) userInfo.get("full_name");
                        String email = (String) userInfo.get("email");
                        String address = (String) userInfo.get("address");
                        java.sql.Date dob = (java.sql.Date) userInfo.get("dob");
                        String gender = (String) userInfo.get("gender");

                        fullNameField.setText(fullName != null ? fullName : "");
                        emailField.setText(email != null ? email : "");
                        addressField.setText(address != null ? address : "");

                        if (dob != null) {
                            birthDateSpinner.setValue(new java.util.Date(dob.getTime()));
                        }

                        if (gender != null) {
                            if (gender.equalsIgnoreCase("Nam"))
                                genderComboBox.setSelectedIndex(0);
                            else if (gender.equalsIgnoreCase("Nữ"))
                                genderComboBox.setSelectedIndex(1);
                            else
                                genderComboBox.setSelectedIndex(2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void saveChanges() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        java.util.Date birthDateUtil = (java.util.Date) birthDateSpinner.getValue();
        java.sql.Date birthDate = new java.sql.Date(birthDateUtil.getTime());
        String gender = (String) genderComboBox.getSelectedItem();

        // Validate
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập họ và tên!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Đang lưu...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // Update profile
                return userService.updateUserProfile(
                        mainFrame.getUsername(), fullName, email, address, birthDate, gender);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();

                    if (success) {
                        JOptionPane.showMessageDialog(UpdateProfileDialog.this,
                                "Cập nhật thông tin thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(UpdateProfileDialog.this,
                                "Có lỗi xảy ra khi cập nhật thông tin!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        saveButton.setEnabled(true);
                        saveButton.setText("Lưu");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UpdateProfileDialog.this,
                            "Lỗi: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    saveButton.setEnabled(true);
                    saveButton.setText("Lưu");
                }
            }
        };

        worker.execute();
    }
}
