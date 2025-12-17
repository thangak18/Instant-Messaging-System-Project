package user.gui;

import javax.swing.*;
import java.awt.*;
import user.service.UserService;

/**
 * Settings Dialog - Hiển thị menu cài đặt
 */
public class SettingsDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);

    private ZaloMainFrame mainFrame;
    private UserService userService = new UserService();

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
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 20));
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
                () -> openViewProfileDialog());

        // 2. Cập nhật thông tin
        JPanel updateInfoItem = createMenuItem(
                "👤",
                "Cập nhật thông tin tài khoản",
                "Thay đổi tên, email, ngày sinh",
                () -> openUpdateProfileDialog());

        // 3. Đổi mật khẩu
        JPanel changePasswordItem = createMenuItem(
                "🔑",
                "Đổi mật khẩu",
                "Thay đổi mật khẩu đăng nhập",
                () -> openChangePasswordDialog());

        // 4. Danh sách người đã chặn
        JPanel blockedUsersItem = createMenuItem(
                "🚫",
                "Danh sách người đã chặn",
                "Xem và bỏ chặn người dùng",
                () -> openBlockedUsersDialog());

        // 5. Đăng xuất
        JPanel logoutItem = createMenuItem(
                "🚪",
                "Đăng xuất tài khoản",
                "Thoát khỏi ứng dụng",
                () -> logout());

        menuPanel.add(viewProfileItem);
        menuPanel.add(createSeparator());
        menuPanel.add(updateInfoItem);
        menuPanel.add(createSeparator());
        menuPanel.add(changePasswordItem);
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
        iconLabel.setFont(new Font(UIHelper.getEmojiFontName(), Font.PLAIN, 28));

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 15));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 12));
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

    private void openChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Đổi mật khẩu", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🔑 Đổi mật khẩu");
        titleLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Mật khẩu hiện tại
        JLabel currentLabel = new JLabel("Mật khẩu hiện tại");
        currentLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        currentPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        currentPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Mật khẩu mới
        JLabel newLabel = new JLabel("Mật khẩu mới");
        newLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        newLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        newPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        newPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Xác nhận mật khẩu mới
        JLabel confirmLabel = new JLabel("Xác nhận mật khẩu mới");
        confirmLabel.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 13));
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        formPanel.add(currentLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(currentPasswordField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(newLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(newPasswordField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(confirmLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(confirmPasswordField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = new JButton("Đổi mật khẩu");
        saveButton.setFont(new Font(UIHelper.getDefaultFontName(), Font.BOLD, 14));
        saveButton.setBackground(PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(130, 38));
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setOpaque(true); // Required for macOS
        saveButton.setContentAreaFilled(true); // Ensure background is painted
        saveButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validate
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng điền đầy đủ thông tin!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(dialog,
                        "Mật khẩu mới phải có ít nhất 6 ký tự!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog,
                        "Mật khẩu mới và xác nhận không khớp!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (currentPassword.equals(newPassword)) {
                JOptionPane.showMessageDialog(dialog,
                        "Mật khẩu mới phải khác mật khẩu hiện tại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Thực hiện đổi mật khẩu
            saveButton.setEnabled(false);
            saveButton.setText("Đang xử lý...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                private String errorMessage = "";

                @Override
                protected Boolean doInBackground() {
                    // Kiểm tra mật khẩu hiện tại
                    boolean isCurrentPasswordValid = userService.verifyPassword(
                            mainFrame.getUsername(), currentPassword);

                    if (!isCurrentPasswordValid) {
                        errorMessage = "Mật khẩu hiện tại không đúng!";
                        return false;
                    }

                    // Đổi mật khẩu
                    return userService.changePassword(mainFrame.getUsername(), newPassword);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();

                        if (success) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Đổi mật khẩu thành công!",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                    errorMessage.isEmpty() ? "Không thể đổi mật khẩu!" : errorMessage,
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog,
                                "Có lỗi xảy ra: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        saveButton.setEnabled(true);
                        saveButton.setText("Đổi mật khẩu");
                    }
                }
            };

            worker.execute();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Wrap formPanel trong ScrollPane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

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
