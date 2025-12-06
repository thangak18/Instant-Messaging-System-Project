package admin.gui;

import admin.service.LoginHistoryDAO;
import admin.service.StatisticsDAO;
import admin.service.UserDAO;
import admin.service.UserDAO.SearchType;
import admin.socket.LoginHistory;
import admin.socket.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Quản lý danh sách người dùng - Backend Integration
 * Yêu cầu: a) Lọc và sắp xếp, b) CRUD, c) Khóa/mở khóa,
 * d) Cập nhật mật khẩu, e) Lịch sử đăng nhập, f) Danh sách bạn bè
 */
public class UserManagementPanel extends JPanel {
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color INFO_CYAN = new Color(23, 162, 184);

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JComboBox<String> searchTypeCombo;

    // Backend DAO
    private UserDAO userDAO;
    private LoginHistoryDAO loginHistoryDAO;
    private StatisticsDAO statisticsDAO;
    private List<User> currentUsers = new ArrayList<>();
    private String lastSortOption;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UserManagementPanel() {
        try {
            this.userDAO = new UserDAO();
            this.loginHistoryDAO = new LoginHistoryDAO();
            this.statisticsDAO = new StatisticsDAO();
            initComponents();
            setupLayout();
            lastSortOption = (String) sortCombo.getSelectedItem();
            loadUsersFromDatabase(); // Load từ database thay vì sample data
            setupEventHandlers();
        } catch (Exception e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // Yêu cầu: Thông tin đầy đủ
        String[] columns = { "ID", "Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh",
                "Giới tính", "Email", "Trạng thái", "Ngày tạo" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        userTable.setAutoCreateRowSorter(true);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(Color.WHITE);
        userTable.getTableHeader().setForeground(Color.BLACK);

        // Yêu cầu a: Lọc theo tên/tên đăng nhập/trạng thái
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[] { "Tìm theo tên", "Tìm theo tên đăng nhập", "Tìm theo email" });
        statusFilter = new JComboBox<>(new String[] { "Tất cả", "Hoạt động", "Bị khóa", "Đã xóa" });

        // Yêu cầu a: Sắp xếp theo tên/ngày tạo
        sortCombo = new JComboBox<>(new String[] { 
                "Sắp xếp theo tên (A-Z)", 
                "Sắp xếp theo tên (Z-A)", 
                "Sắp xếp theo ngày tạo (Mới nhất)",
                "Sắp xếp theo ngày tạo (Cũ nhất)" 
        });

        // Adjust column widths
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40); // ID
        columnModel.getColumn(1).setPreferredWidth(100); // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(120); // Họ tên
        columnModel.getColumn(3).setPreferredWidth(150); // Địa chỉ
        columnModel.getColumn(4).setPreferredWidth(80); // Ngày sinh
        columnModel.getColumn(5).setPreferredWidth(60); // Giới tính
        columnModel.getColumn(6).setPreferredWidth(150); // Email
        columnModel.getColumn(7).setPreferredWidth(80); // Trạng thái
        columnModel.getColumn(8).setPreferredWidth(90); // Ngày tạo
    }

    /**
     * Load users từ database
     */
    private void loadUsersFromDatabase() {
        try {
            List<User> users = userDAO.getAllUsers();
            sortUsers(users, lastSortOption);
            currentUsers = users;
            displayUsers(currentUsers);
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            String detailedMsg = "Lỗi load dữ liệu: " + errorMsg;

            // Thêm hướng dẫn nếu là lỗi cấu hình
            if (errorMsg != null && (errorMsg.contains("chưa được cấu hình") ||
                    errorMsg.contains("YOUR_") ||
                    errorMsg.contains("configuration"))) {
                detailedMsg += "\n\n" +
                        "⚠️ Database chưa được cấu hình!\n\n" +
                        "Cách sửa:\n" +
                        "1. Chạy: ./configure_db.sh\n" +
                        "2. Hoặc sửa file: release/config.properties\n" +
                        "3. Thay YOUR_PROJECT_REF và YOUR_PASSWORD_HERE\n" +
                        "   bằng thông tin Supabase thực tế";
            }

            showError(detailedMsg);
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị danh sách users lên table
     */
    private void displayUsers(List<User> users) {
        currentUsers = new ArrayList<>(users);
        tableModel.setRowCount(0);

        for (User user : currentUsers) {
            Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress() != null ? user.getAddress() : "",
                    user.getBirthDate() != null ? user.getBirthDate().format(dateFormatter) : "",
                    user.getGender() != null ? user.getGender() : "",
                    user.getEmail(),
                    formatStatus(user.getStatus()),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(dateTimeFormatter) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Search and Filter panel (Yêu cầu a)
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        ImageIcon userIcon = loadIcon("user", 20, 20);
        JLabel titleLabel = new JLabel("Danh sách người dùng");
        if (userIcon != null) {
            titleLabel.setIcon(userIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        tablePanel.add(titleLabel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Action buttons panel (Yêu cầu b, c, d, e, f)
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(15, 15, 15, 15)));

        ImageIcon searchTitleIcon = loadIcon("search", 20, 20);
        JLabel titleLabel = new JLabel("Tìm kiếm & Lọc người dùng");
        if (searchTitleIcon != null) {
            titleLabel.setIcon(searchTitleIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);

        // Row 1: Search
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.setOpaque(false);
        row1.add(new JLabel("Loại tìm kiếm:"));
        row1.add(searchTypeCombo);
        row1.add(new JLabel("Từ khóa:"));
        row1.add(searchField);

        // Row 2: Filter and Sort
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.setOpaque(false);
        row2.add(new JLabel("Trạng thái:"));
        row2.add(statusFilter);
        row2.add(new JLabel("Sắp xếp:"));
        row2.add(sortCombo);

        // Nút duy nhất để tìm kiếm và lọc
        row2.add(createStyledButton("🔍 Tìm kiếm + Lọc", ZALO_BLUE));

        // Nút đặt lại
        row2.add(createStyledButton("↺ Đặt lại", ZALO_BLUE));

        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setOpaque(false);
        formPanel.add(row1, BorderLayout.NORTH);
        formPanel.add(row2, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Panel chứa 2 hàng nút - phân chia cân đối
        JPanel buttonsContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonsContainer.setOpaque(false);

        // Row 1: CRUD operations (5 nút: Thêm, Sửa, Xóa, Khóa, Mở khóa)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row1.setOpaque(false);

        JButton addBtn = createButtonWithIcon("Thêm người dùng", "add.png", INFO_CYAN);
        addBtn.addActionListener(e -> showAddUserDialog());

        JButton editBtn = createButtonWithIcon("Sửa thông tin", "edit.png", INFO_CYAN);
        editBtn.addActionListener(e -> showEditUserDialog());

        JButton deleteBtn = createButtonWithIcon("Xóa người dùng", "delete.png", INFO_CYAN);
        deleteBtn.addActionListener(e -> showDeleteUserDialog());

        JButton lockBtn = createButtonWithIcon("Khóa tài khoản", "lock.png", INFO_CYAN);
        lockBtn.addActionListener(e -> showLockAccountDialog());

        JButton unlockBtn = createButtonWithIcon("Mở khóa", "unlock.png", INFO_CYAN);
        unlockBtn.addActionListener(e -> showUnlockAccountDialog());

        row1.add(addBtn);
        row1.add(editBtn);
        row1.add(deleteBtn);
        row1.add(lockBtn);
        row1.add(unlockBtn);

        // Row 2: Các chức năng bổ sung (3 nút: Đổi mật khẩu, Lịch sử, Danh sách bạn bè)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row2.setOpaque(false);

        JButton passwordBtn = createButtonWithIcon("Đổi mật khẩu", "password.png", INFO_CYAN);
        passwordBtn.addActionListener(e -> showChangePasswordDialog());

        JButton resetPwdBtn = createButtonWithIcon("Reset mật khẩu", "reset.png", INFO_CYAN);
        resetPwdBtn.addActionListener(e -> showResetPasswordDialog());

        JButton historyBtn = createButtonWithIcon("Lịch sử", "history.png", INFO_CYAN);
        historyBtn.addActionListener(e -> showLoginHistoryDialog());

        JButton friendsBtn = createButtonWithIcon("Danh sách bạn", "contact.png", INFO_CYAN);
        friendsBtn.addActionListener(e -> showFriendsListDialog());

        JButton exportBtn = createButtonWithIcon("Xuất CSV", "export.png", INFO_CYAN);
        exportBtn.addActionListener(e -> exportUsersToCSV());

        row2.add(passwordBtn);
        row2.add(resetPwdBtn);
        row2.add(historyBtn);
        row2.add(friendsBtn);
        row2.add(exportBtn);

        buttonsContainer.add(row1);
        buttonsContainer.add(row2);

        mainPanel.add(buttonsContainer, BorderLayout.CENTER);

        return mainPanel;
    }

    private void setupEventHandlers() {
        // Yêu cầu b: Thêm người dùng
        addActionToButton("➕ Thêm người dùng", e -> showAddUserDialog());

        // Yêu cầu b: Sửa thông tin
        addActionToButton("✏️ Sửa thông tin", e -> showEditUserDialog());

        // Yêu cầu b: Xóa người dùng
        addActionToButton("🗑️ Xóa người dùng", e -> showDeleteUserDialog());

        // Yêu cầu c: Khóa tài khoản
        addActionToButton("🔒 Khóa tài khoản", e -> showLockAccountDialog());

        // Yêu cầu c: Mở khóa tài khoản
        addActionToButton("🔓 Mở khóa", e -> showUnlockAccountDialog());

        // Yêu cầu d: Cập nhật mật khẩu
        addActionToButton("🔑 Đổi mật khẩu", e -> showChangePasswordDialog());

        // Yêu cầu 1.5: Reset mật khẩu (random)
        addActionToButton("🔄 Reset mật khẩu", e -> showResetPasswordDialog());

        addActionToButton("📜 Lịch sử đăng nhập", e -> showLoginHistoryDialog());
        addActionToButton("👥 Danh sách bạn bè", e -> showFriendsListDialog());
        addActionToButton("📊 Xuất CSV", e -> handleExportCSV());

        // Nút duy nhất: Tìm kiếm + Lọc
        addActionToButton("🔍 Tìm kiếm + Lọc", e -> applyAllFilters(false));

        // Nút đặt lại
        addActionToButton("↺ Đặt lại", e -> handleReset());
    }

    // ==================== EVENT HANDLERS ====================

    // Yêu cầu b: Thêm người dùng
    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm người dùng mới", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        String[] labels = { "Tên đăng nhập:", "Mật khẩu:", "Họ tên:", "Địa chỉ:",
                "Ngày sinh:", "Giới tính:", "Email:" };

        JTextField birthDateField = new JTextField(20);
        birthDateField.setForeground(Color.GRAY);
        birthDateField.setText("dd/MM/yyyy");

        // Thêm placeholder behavior
        birthDateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (birthDateField.getText().equals("dd/MM/yyyy")) {
                    birthDateField.setText("");
                    birthDateField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (birthDateField.getText().isEmpty()) {
                    birthDateField.setForeground(Color.GRAY);
                    birthDateField.setText("dd/MM/yyyy");
                }
            }
        });

        JComponent[] fields = {
                new JTextField(20),
                new JPasswordField(20),
                new JTextField(20),
                new JTextField(20),
                birthDateField,
                new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" }),
                new JTextField(20)
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            formPanel.add(fields[i], gbc);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = createStyledButton("💾 Lưu", SUCCESS_GREEN);
        JButton cancelBtn = createStyledButton("❌ Hủy", DANGER_RED);

        saveBtn.addActionListener(e -> {
            try {
                // Lấy dữ liệu từ các trường
                String username = ((JTextField) fields[0]).getText().trim();
                String password = new String(((JPasswordField) fields[1]).getPassword());
                String fullName = ((JTextField) fields[2]).getText().trim();
                String address = ((JTextField) fields[3]).getText().trim();
                String birthDateStr = ((JTextField) fields[4]).getText().trim();
                // Bỏ qua placeholder text
                if (birthDateStr.equals("dd/MM/yyyy")) {
                    birthDateStr = "";
                }
                String gender = (String) ((JComboBox<?>) fields[5]).getSelectedItem();
                String email = ((JTextField) fields[6]).getText().trim();

                // Validate các trường bắt buộc
                if (username.isEmpty()) {
                    showWarning("Tên đăng nhập không được để trống!");
                    return;
                }
                
                // Validate username length
                if (username.length() < 3 || username.length() > 50) {
                    showWarning("Tên đăng nhập phải từ 3 đến 50 ký tự!");
                    return;
                }
                
                // Validate username format (chỉ cho phép chữ, số, dấu gạch dưới)
                if (!username.matches("^[a-zA-Z0-9_]+$")) {
                    showWarning("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới!");
                    return;
                }
                
                // Check duplicate username
                try {
                    if (userDAO.usernameExists(username)) {
                        showWarning("Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.");
                        return;
                    }
                } catch (SQLException ex) {
                    showError("Lỗi kiểm tra tên đăng nhập: " + ex.getMessage());
                    return;
                }
                
                if (password.isEmpty()) {
                    showWarning("Mật khẩu không được để trống!");
                    return;
                }
                
                // Validate password strength
                if (password.length() < 6) {
                    showWarning("Mật khẩu phải có ít nhất 6 ký tự!");
                    return;
                }
                
                if (fullName.isEmpty()) {
                    showWarning("Họ tên không được để trống!");
                    return;
                }
                
                // Validate full name length
                if (fullName.length() > 100) {
                    showWarning("Họ tên không được vượt quá 100 ký tự!");
                    return;
                }
                
                if (email.isEmpty()) {
                    showWarning("Email không được để trống!");
                    return;
                }
                
                // Validate email length
                if (email.length() > 255) {
                    showWarning("Email không được vượt quá 255 ký tự!");
                    return;
                }

                // Validate email format
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showWarning("Email không đúng định dạng!\nVí dụ: user@example.com");
                    return;
                }
                
                // Check duplicate email
                try {
                    if (userDAO.emailExists(email)) {
                        showWarning("Email đã tồn tại! Vui lòng sử dụng email khác.");
                        return;
                    }
                } catch (SQLException ex) {
                    showError("Lỗi kiểm tra email: " + ex.getMessage());
                    return;
                }

                // Validate và parse ngày sinh
                LocalDate birthDate = null;
                if (!birthDateStr.isEmpty()) {
                    try {
                        birthDate = LocalDate.parse(birthDateStr, dateFormatter);

                        // Kiểm tra ngày sinh không được trong tương lai
                        if (birthDate.isAfter(LocalDate.now())) {
                            showWarning("Ngày sinh không được ở tương lai!");
                            return;
                        }

                        // Kiểm tra tuổi hợp lý (ví dụ: từ 1 đến 150 tuổi)
                        int age = LocalDate.now().getYear() - birthDate.getYear();
                        if (age < 1 || age > 150) {
                            showWarning("Ngày sinh không hợp lệ! Tuổi phải từ 1 đến 150.");
                            return;
                        }
                    } catch (DateTimeParseException ex) {
                        showWarning(
                                "Ngày sinh không đúng định dạng!\nVui lòng nhập theo định dạng: dd/MM/yyyy\nVí dụ: 15/03/1990");
                        return;
                    }
                }

                // Tạo User object
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setFullName(fullName);
                newUser.setAddress(address.isEmpty() ? null : address);
                newUser.setBirthDate(birthDate);
                newUser.setGender(gender);
                newUser.setEmail(email);
                newUser.setStatus("active");
                newUser.setCreatedAt(LocalDateTime.now());

                // Lưu vào database
                boolean success = userDAO.addUser(newUser);
                if (success) {
                    showSuccess("Thêm người dùng thành công!");
                    loadUsersFromDatabase(); // Reload danh sách
                    dialog.dispose();
                } else {
                    showError("Không thể thêm người dùng!\n\n" +
                            "Có thể do:\n" +
                            "- Lỗi kết nối database\n" +
                            "- Dữ liệu không hợp lệ\n" +
                            "Vui lòng thử lại hoặc liên hệ admin.");
                }
            } catch (SQLException ex) {
                String errorMsg = ex.getMessage();
                String detailedMsg = "Lỗi khi thêm người dùng: " + errorMsg;
                
                // Kiểm tra lỗi duplicate (nếu có)
                if (errorMsg != null && (errorMsg.contains("duplicate") || 
                                         errorMsg.contains("unique") ||
                                         errorMsg.contains("UNIQUE"))) {
                    detailedMsg = "Tên đăng nhập hoặc email đã tồn tại!\n" +
                                 "Vui lòng chọn tên đăng nhập hoặc email khác.";
                } else {
                    detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                                  "- Kết nối database\n" +
                                  "- Thông tin nhập vào\n" +
                                  "Hoặc liên hệ admin để được hỗ trợ.";
                }
                
                showError(detailedMsg);
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Yêu cầu b: Sửa thông tin
    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần sửa!");
            return;
        }

        // Lấy userId để load dữ liệu đầy đủ từ database
        int userId = (int) userTable.getValueAt(selectedRow, 0);

        try {
            // Load user data từ database
            User user = userDAO.getUserById(userId);
            if (user == null) {
                showError("Không tìm thấy người dùng!");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Sửa thông tin: " + user.getUsername(), true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(500, 600);
            dialog.setLocationRelativeTo(this);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Form fields với dữ liệu hiện tại
            String[] labels = { "Tên đăng nhập:", "Họ tên:", "Địa chỉ:",
                    "Ngày sinh:", "Giới tính:", "Email:" };

            // Tạo các field và load dữ liệu
            JTextField usernameField = new JTextField(user.getUsername(), 20);
            usernameField.setEnabled(false); // Không cho sửa username
            usernameField.setBackground(Color.LIGHT_GRAY);

            JTextField fullNameField = new JTextField(user.getFullName(), 20);
            JTextField addressField = new JTextField(
                    user.getAddress() != null ? user.getAddress() : "", 20);

            // Ngày sinh với placeholder
            JTextField birthDateField = new JTextField(20);
            if (user.getBirthDate() != null) {
                birthDateField.setText(user.getBirthDate().format(dateFormatter));
                birthDateField.setForeground(Color.BLACK);
            } else {
                birthDateField.setText("dd/MM/yyyy");
                birthDateField.setForeground(Color.GRAY);
            }

            // Thêm placeholder behavior cho ngày sinh
            birthDateField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (birthDateField.getText().equals("dd/MM/yyyy")) {
                        birthDateField.setText("");
                        birthDateField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (birthDateField.getText().isEmpty()) {
                        birthDateField.setForeground(Color.GRAY);
                        birthDateField.setText("dd/MM/yyyy");
                    }
                }
            });

            JComboBox<String> genderCombo = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
            if (user.getGender() != null) {
                genderCombo.setSelectedItem(user.getGender());
            }

            JTextField emailField = new JTextField(user.getEmail(), 20);

            JComponent[] fields = {
                    usernameField,
                    fullNameField,
                    addressField,
                    birthDateField,
                    genderCombo,
                    emailField
            };

            // Add fields to form
            for (int i = 0; i < labels.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.weightx = 0;
                JLabel label = new JLabel(labels[i]);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                formPanel.add(label, gbc);

                gbc.gridx = 1;
                gbc.weightx = 1;
                formPanel.add(fields[i], gbc);
            }

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton saveBtn = createStyledButton("💾 Lưu", SUCCESS_GREEN);
            JButton cancelBtn = createStyledButton("❌ Hủy", DANGER_RED);

            saveBtn.addActionListener(e -> {
                try {
                    // Lấy dữ liệu từ các trường
                    String fullName = fullNameField.getText().trim();
                    String address = addressField.getText().trim();
                    String birthDateStr = birthDateField.getText().trim();
                    // Bỏ qua placeholder text
                    if (birthDateStr.equals("dd/MM/yyyy")) {
                        birthDateStr = "";
                    }
                    String gender = (String) genderCombo.getSelectedItem();
                    String email = emailField.getText().trim();

                    // Validate các trường bắt buộc
                    if (fullName.isEmpty()) {
                        showWarning("Họ tên không được để trống!");
                        return;
                    }
                    
                    // Validate full name length
                    if (fullName.length() > 100) {
                        showWarning("Họ tên không được vượt quá 100 ký tự!");
                        return;
                    }
                    
                    if (email.isEmpty()) {
                        showWarning("Email không được để trống!");
                        return;
                    }
                    
                    // Validate email length
                    if (email.length() > 255) {
                        showWarning("Email không được vượt quá 255 ký tự!");
                        return;
                    }

                    // Validate email format
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        showWarning("Email không đúng định dạng!\nVí dụ: user@example.com");
                        return;
                    }
                    
                    // Check duplicate email (trừ user hiện tại)
                    try {
                        if (userDAO.emailExists(email, user.getId())) {
                            showWarning("Email đã được sử dụng bởi người dùng khác!\nVui lòng sử dụng email khác.");
                            return;
                        }
                    } catch (SQLException ex) {
                        showError("Lỗi kiểm tra email: " + ex.getMessage());
                        return;
                    }

                    // Validate và parse ngày sinh
                    LocalDate birthDate = null;
                    if (!birthDateStr.isEmpty()) {
                        try {
                            birthDate = LocalDate.parse(birthDateStr, dateFormatter);

                            // Kiểm tra ngày sinh không được trong tương lai
                            if (birthDate.isAfter(LocalDate.now())) {
                                showWarning("Ngày sinh không được ở tương lai!");
                                return;
                            }

                            // Kiểm tra tuổi hợp lý (ví dụ: từ 1 đến 150 tuổi)
                            int age = LocalDate.now().getYear() - birthDate.getYear();
                            if (age < 1 || age > 150) {
                                showWarning("Ngày sinh không hợp lệ! Tuổi phải từ 1 đến 150.");
                                return;
                            }
                        } catch (DateTimeParseException ex) {
                            showWarning(
                                    "Ngày sinh không đúng định dạng!\nVui lòng nhập theo định dạng: dd/MM/yyyy\nVí dụ: 15/03/1990");
                            return;
                        }
                    }

                    // Cập nhật User object
                    user.setFullName(fullName);
                    user.setAddress(address.isEmpty() ? null : address);
                    user.setBirthDate(birthDate);
                    user.setGender(gender);
                    user.setEmail(email);

                    // Lưu vào database
                    boolean success = userDAO.updateUser(user);
                    if (success) {
                        showSuccess("Cập nhật thông tin thành công!");
                        loadUsersFromDatabase(); // Reload danh sách
                        dialog.dispose();
                    } else {
                        showError("Không thể cập nhật thông tin!\n\n" +
                                "Có thể do:\n" +
                                "- Lỗi kết nối database\n" +
                                "- Dữ liệu không hợp lệ\n" +
                                "Vui lòng thử lại hoặc liên hệ admin.");
                    }
                } catch (SQLException ex) {
                    String errorMsg = ex.getMessage();
                    String detailedMsg = "Lỗi khi cập nhật thông tin: " + errorMsg;
                    
                    // Kiểm tra lỗi duplicate (nếu có)
                    if (errorMsg != null && (errorMsg.contains("duplicate") || 
                                             errorMsg.contains("unique") ||
                                             errorMsg.contains("UNIQUE"))) {
                        detailedMsg = "Email đã được sử dụng bởi người dùng khác!\n" +
                                     "Vui lòng chọn email khác.";
                    } else {
                        detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                                      "- Kết nối database\n" +
                                      "- Thông tin nhập vào\n" +
                                      "Hoặc liên hệ admin để được hỗ trợ.";
                    }
                    
                    showError(detailedMsg);
                    ex.printStackTrace();
                }
            });

            cancelBtn.addActionListener(e -> dialog.dispose());

            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);

            dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (SQLException e) {
            showError("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng tất cả các filter: từ khóa, trạng thái, sắp xếp
     * 
     * @param requireKeyword true nếu yêu cầu nhập từ khóa (hiện tại luôn là false -
     *                       không bắt buộc)
     */
    private void applyAllFilters(boolean requireKeyword) {
        try {
            String keyword = searchField.getText().trim();

            // Nếu gọi từ nút Tìm kiếm, yêu cầu phải nhập từ khóa
            if (requireKeyword && keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
                return;
            }

            // Bước 1: Lấy TẤT CẢ users từ database
            List<User> users = userDAO.getAllUsers();

            // Bước 2: Lọc theo trạng thái (nếu không chọn "Tất cả")
            String statusValue = (String) statusFilter.getSelectedItem();
            if (!"Tất cả".equals(statusValue)) {
                users.removeIf(user -> !matchesStatus(user, statusValue));
            }

            // Bước 3: Lọc theo từ khóa (nếu có)
            if (!keyword.isEmpty()) {
                SearchType searchType = resolveSearchType((String) searchTypeCombo.getSelectedItem());
                users.removeIf(user -> !matchesKeyword(user, keyword, searchType));
            }

            // Bước 3: Sắp xếp
            lastSortOption = (String) sortCombo.getSelectedItem();
            sortUsers(users, lastSortOption);

            // Hiển thị kết quả
            displayUsers(users);

            if (requireKeyword) {
                showSuccess("Tìm thấy " + users.size() + " kết quả");
            } else {
                showSuccess("Đã lọc " + users.size() + " người dùng");
            }
        } catch (SQLException e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đặt lại tất cả bộ lọc về trạng thái mặc định
     */
    private void handleReset() {
        // Reset các trường nhập liệu
        searchField.setText("");
        searchTypeCombo.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0); // "Tất cả"
        sortCombo.setSelectedIndex(0); // "Sắp xếp theo tên (A-Z)"

        // Load lại danh sách từ database
        loadUsersFromDatabase();

        showSuccess("Đã đặt lại bộ lọc!");
    }

    // Yêu cầu b: Xóa người dùng
    private void showDeleteUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần xóa!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();
        String currentStatus = userTable.getValueAt(selectedRow, 7).toString();

        // Kiểm tra nếu user đã bị xóa rồi
        if ("Đã xóa".equals(currentStatus)) {
            showWarning("Người dùng này đã bị xóa trước đó!");
            return;
        }

        // Tạo dialog xác nhận với thông tin chi tiết
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel("<html><b>Bạn có chắc muốn xóa người dùng: " + username + "?</b><br><br>" +
                "⚠️ Lưu ý: Người dùng sẽ được đánh dấu là 'Đã xóa' thay vì xóa hoàn toàn<br>" +
                "để tránh mất dữ liệu tin nhắn và lịch sử.</html>");
        panel.add(messageLabel, BorderLayout.CENTER);

        int confirm = showStyledConfirmDialog(this, panel,
                "Xác nhận xóa");

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Soft delete: Đổi status thành "deleted" thay vì xóa thật
                boolean success = userDAO.updateUserStatus(userId, "deleted");
                if (success) {
                    showSuccess("Đã đánh dấu xóa người dùng thành công!");
                    loadUsersFromDatabase();
                } else {
                    showError("Không thể xóa người dùng");
                }
            } catch (SQLException e) {
                // Nếu vẫn muốn xóa hoàn toàn, hiển thị thông báo lỗi chi tiết
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("foreign key constraint")) {
                    showError("Không thể xóa người dùng!\n\n" +
                            "Lý do: Người dùng này có dữ liệu liên quan (tin nhắn, bạn bè, v.v.)\n" +
                            "Người dùng đã được đánh dấu là 'Đã xóa' thay thế.");
                    // Vẫn thử soft delete
                    try {
                        userDAO.updateUserStatus(userId, "deleted");
                        loadUsersFromDatabase();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    showError("Lỗi xóa người dùng: " + errorMsg);
                }
                e.printStackTrace();
            }
        }
    }

    // Yêu cầu c: Khóa tài khoản
    private void showLockAccountDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần khóa!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();

        int confirm = showStyledConfirmDialog(this,
                "Khóa tài khoản: " + username + "?",
                "Xác nhận khóa");

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userDAO.updateUserStatus(userId, "locked");
                if (success) {
                    showSuccess("Đã khóa tài khoản thành công!");
                    loadUsersFromDatabase();
                } else {
                    showError("Không thể khóa tài khoản");
                }
            } catch (SQLException e) {
                showError("Lỗi khóa tài khoản: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Yêu cầu c: Mở khóa tài khoản
    private void showUnlockAccountDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần mở khóa!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();

        int confirm = showStyledConfirmDialog(this,
                "Mở khóa tài khoản: " + username + "?",
                "Xác nhận mở khóa");

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userDAO.updateUserStatus(userId, "active");
                if (success) {
                    showSuccess("Đã mở khóa tài khoản thành công!");
                    loadUsersFromDatabase();
                } else {
                    showError("Không thể mở khóa tài khoản");
                }
            } catch (SQLException e) {
                showError("Lỗi mở khóa tài khoản: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Yêu cầu d: Đổi mật khẩu
    private void showChangePasswordDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần đổi mật khẩu!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();

        JPasswordField newPassword = new JPasswordField(20);
        JPasswordField confirmPassword = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Người dùng:"));
        panel.add(new JLabel(username));
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(newPassword);
        panel.add(new JLabel("Xác nhận:"));
        panel.add(confirmPassword);

        int result = showStyledConfirmDialog(this, panel,
                "Đổi mật khẩu");

        if (result == JOptionPane.OK_OPTION) {
            String newPwd = new String(newPassword.getPassword());
            String confirmPwd = new String(confirmPassword.getPassword());

            if (newPwd.isEmpty()) {
                showWarning("Mật khẩu không được để trống!");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showWarning("Mật khẩu xác nhận không khớp!");
                return;
            }

            try {
                boolean success = userDAO.updatePassword(userId, newPwd);
                if (success) {
                    showSuccess("Đổi mật khẩu thành công!");
                } else {
                    showError("Không thể đổi mật khẩu");
                }
            } catch (SQLException e) {
                showError("Lỗi đổi mật khẩu: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Yêu cầu 1.5: Reset mật khẩu (random)
    private void showResetPasswordDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng cần reset mật khẩu!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();

        int confirm = showStyledConfirmDialog(this,
                "Reset mật khẩu cho user: " + username + "?\nMật khẩu mới sẽ được tự động tạo.",
                "Xác nhận Reset");

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String newPassword = generateRandomPassword();
            boolean success = userDAO.updatePassword(userId, newPassword);

            if (success) {
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                JLabel messageLabel = new JLabel("<html><b>Mật khẩu mới cho " + username + ":</b></html>");
                JTextField passwordField = new JTextField(newPassword);
                passwordField.setEditable(false);
                passwordField.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));

                JButton copyBtn = new JButton("📋 Copy");
                copyBtn.addActionListener(e -> {
                    java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(
                            newPassword);
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(stringSelection, null);
                    JOptionPane.showMessageDialog(this, "Đã copy vào clipboard!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                });

                panel.add(messageLabel, BorderLayout.NORTH);
                panel.add(passwordField, BorderLayout.CENTER);
                panel.add(copyBtn, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(this, panel, "Reset Mật Khẩu Thành Công!",
                        JOptionPane.INFORMATION_MESSAGE);
                showSuccess("Reset mật khẩu thành công!");
            } else {
                showError("Không thể reset mật khẩu");
            }
        } catch (SQLException e) {
            showError("Lỗi reset mật khẩu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String allChars = upperCase + lowerCase + digits + special;
        java.util.Random random = new java.util.Random();
        int length = 10;
        StringBuilder password = new StringBuilder();
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        return new String(passwordArray);
    }

    // Helper methods for showing messages
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showLoginHistoryDialog() {
        try {
            // Lấy TẤT CẢ lịch sử đăng nhập
            List<LoginHistory> historyList = loginHistoryDAO.getAllLoginHistory();
            if (historyList.isEmpty()) {
                showWarning("Chưa có lịch sử đăng nhập nào trong hệ thống");
                return;
            }

            // Lấy TẤT CẢ users MỘT LẦN vào Map để tránh query nhiều lần
            List<User> allUsers = userDAO.getAllUsers();
            Map<Integer, User> userMap = new HashMap<>();
            for (User user : allUsers) {
                userMap.put(user.getId(), user);
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Lịch sử đăng nhập - Tất cả người dùng", true);
            dialog.setSize(1100, 600);
            dialog.setLocationRelativeTo(this);

            String[] columns = { "ID", "Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh",
                    "Giới tính", "Email", "Thời gian", "Địa chỉ IP", "Thiết bị" };
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // Duyệt qua từng bản ghi lịch sử và lấy thông tin user từ Map
            for (LoginHistory history : historyList) {
                User user = userMap.get(history.getUserId());
                if (user != null) {
                    model.addRow(new Object[] {
                            user.getId(),
                            user.getUsername(),
                            user.getFullName() != null ? user.getFullName() : "",
                            user.getAddress() != null ? user.getAddress() : "",
                            user.getBirthDate() != null ? user.getBirthDate().format(dateFormatter) : "",
                            user.getGender() != null ? user.getGender() : "",
                            user.getEmail() != null ? user.getEmail() : "",
                            history.getLoginTime() != null ? history.getLoginTime().format(datetimeFormatter) : "",
                            history.getIpAddress() != null ? history.getIpAddress() : "N/A",
                            history.getUserAgent() != null ? history.getUserAgent() : ""
                    });
                }
            }

            JTable table = new JTable(model);
            table.setRowHeight(24);
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

            // Điều chỉnh độ rộng cột
            table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
            table.getColumnModel().getColumn(2).setPreferredWidth(120); // Họ tên
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Địa chỉ
            table.getColumnModel().getColumn(4).setPreferredWidth(90); // Ngày sinh
            table.getColumnModel().getColumn(5).setPreferredWidth(70); // Giới tính
            table.getColumnModel().getColumn(6).setPreferredWidth(150); // Email
            table.getColumnModel().getColumn(7).setPreferredWidth(130); // Thời gian
            table.getColumnModel().getColumn(8).setPreferredWidth(100); // IP
            table.getColumnModel().getColumn(9).setPreferredWidth(200); // Thiết bị

            // Sắp xếp theo thời gian mới nhất
            table.setAutoCreateRowSorter(true);

            dialog.add(new JScrollPane(table), BorderLayout.CENTER);

            JButton closeBtn = createStyledButton("Đóng", new Color(108, 117, 125));
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeBtn);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Lỗi lấy lịch sử đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFriendsListDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Vui lòng chọn người dùng!");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = userTable.getValueAt(selectedRow, 1).toString();

        try {
            // Lấy danh sách bạn bè (có thể không đầy đủ thông tin)
            List<User> friends = statisticsDAO.getFriendsOfUser(userId);
            if (friends.isEmpty()) {
                showWarning("Người dùng chưa có bạn bè");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Danh sách bạn bè - " + username, true);
            dialog.setSize(1000, 500);
            dialog.setLocationRelativeTo(this);

            String[] columns = { "ID", "Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh",
                    "Giới tính", "Email", "Trạng thái", "Ngày tạo" };
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // Lấy đầy đủ thông tin từng friend bằng getUserById
            for (User friend : friends) {
                try {
                    // Lấy đầy đủ thông tin user từ database
                    User fullUserInfo = userDAO.getUserById(friend.getId());
                    if (fullUserInfo != null) {
                        model.addRow(new Object[] {
                                fullUserInfo.getId(),
                                fullUserInfo.getUsername(),
                                fullUserInfo.getFullName() != null ? fullUserInfo.getFullName() : "",
                                fullUserInfo.getAddress() != null ? fullUserInfo.getAddress() : "",
                                fullUserInfo.getBirthDate() != null ? fullUserInfo.getBirthDate().format(dateFormatter)
                                        : "",
                                fullUserInfo.getGender() != null ? fullUserInfo.getGender() : "",
                                fullUserInfo.getEmail() != null ? fullUserInfo.getEmail() : "",
                                formatStatus(fullUserInfo.getStatus()),
                                fullUserInfo.getCreatedAt() != null
                                        ? fullUserInfo.getCreatedAt().format(datetimeFormatter)
                                        : ""
                        });
                    }
                } catch (SQLException e) {
                    System.err.println("Lỗi lấy thông tin user ID: " + friend.getId());
                }
            }

            JTable table = new JTable(model);
            table.setRowHeight(24);
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

            // Điều chỉnh độ rộng cột
            table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
            table.getColumnModel().getColumn(2).setPreferredWidth(120); // Họ tên
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Địa chỉ
            table.getColumnModel().getColumn(4).setPreferredWidth(90); // Ngày sinh
            table.getColumnModel().getColumn(5).setPreferredWidth(70); // Giới tính
            table.getColumnModel().getColumn(6).setPreferredWidth(150); // Email
            table.getColumnModel().getColumn(7).setPreferredWidth(90); // Trạng thái
            table.getColumnModel().getColumn(8).setPreferredWidth(130); // Ngày tạo

            dialog.add(new JScrollPane(table), BorderLayout.CENTER);

            JButton closeBtn = createStyledButton("Đóng", new Color(108, 117, 125));
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeBtn);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Lỗi lấy danh sách bạn bè: " + e.getMessage());
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        try {
            String path = "icons/" + iconName + ".png";
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
        }
        return null;
    }

    private JButton createButtonWithIcon(String text, String iconName, Color color) {
        ImageIcon icon = loadIcon(iconName, 16, 16);
        JButton button;
        if (icon != null) {
            button = new JButton(text, icon);
        } else {
            button = new JButton(text);
        }
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addActionToButton(String buttonText, ActionListener action) {
        Component[] components = getAllComponents(this);
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().equals(buttonText)) {
                    btn.addActionListener(action);
                    break;
                }
            }
        }
    }

    private void sortUsers(List<User> users, String sortOption) {
        if (users == null || sortOption == null) {
            return;
        }

        Comparator<User> comparator;
        switch (sortOption) {
            case "Sắp xếp theo tên (A-Z)":
                // Sử dụng Collator cho tiếng Việt để sắp xếp chính xác theo bảng chữ cái
                Collator viCollatorAZ = Collator.getInstance(new Locale("vi", "VN"));
                viCollatorAZ.setStrength(Collator.SECONDARY); // Phân biệt dấu nhưng không phân biệt hoa/thường
                comparator = Comparator
                        .comparing((User user) -> user.getFullName() != null ? user.getFullName() : "",
                                Comparator.nullsLast(viCollatorAZ));
                break;
            case "Sắp xếp theo tên (Z-A)":
                // Sử dụng Collator cho tiếng Việt và đảo ngược
                Collator viCollatorZA = Collator.getInstance(new Locale("vi", "VN"));
                viCollatorZA.setStrength(Collator.SECONDARY);
                comparator = Comparator
                        .comparing((User user) -> user.getFullName() != null ? user.getFullName() : "",
                                Comparator.nullsFirst(viCollatorZA))
                        .reversed();
                break;
            case "Sắp xếp theo ngày tạo (Cũ nhất)":
                comparator = Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
                break;
            case "Sắp xếp theo ngày tạo (Mới nhất)":
            default:
                comparator = Comparator.comparing(User::getCreatedAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
                break;
        }

        users.sort(comparator);
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        if ("deleted".equalsIgnoreCase(status)) {
            return "Đã xóa";
        }
        return "locked".equalsIgnoreCase(status) ? "Bị khóa" : "Hoạt động";
    }

    private SearchType resolveSearchType(String selected) {
        if (selected == null) {
            return SearchType.ALL;
        }
        if (selected.contains("đăng nhập")) {
            return SearchType.USERNAME;
        }
        if (selected.contains("email")) {
            return SearchType.EMAIL;
        }
        if (selected.contains("tên")) {
            return SearchType.FULL_NAME;
        }
        return SearchType.ALL;
    }

    private boolean matchesStatus(User user, String statusSelection) {
        if ("Tất cả".equals(statusSelection)) {
            return true;
        }

        String userStatus = user.getStatus();

        // So sánh theo logic GIỐNG với formatStatus()
        if (statusSelection.equals("Hoạt động")) {
            // "Hoạt động" = BẤT KỲ status nào NGOẠI TRỪ "locked" và "deleted"
            return !"locked".equalsIgnoreCase(userStatus) && !"deleted".equalsIgnoreCase(userStatus);
        } else if (statusSelection.equals("Bị khóa")) {
            return "locked".equalsIgnoreCase(userStatus);
        } else if (statusSelection.equals("Đã xóa")) {
            return "deleted".equalsIgnoreCase(userStatus);
        }

        return false;
    }

    private boolean matchesKeyword(User user, String keyword, SearchType searchType) {
        String lowerKeyword = keyword.toLowerCase();
        switch (searchType) {
            case USERNAME:
                return user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword);
            case FULL_NAME:
                return user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerKeyword);
            case EMAIL:
                return user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerKeyword);
            default:
                return (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword)) ||
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerKeyword)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerKeyword));
        }
    }

    private Component[] getAllComponents(Container container) {
        java.util.ArrayList<Component> list = new java.util.ArrayList<>();
        Component[] components = container.getComponents();
        for (Component component : components) {
            list.add(component);
            if (component instanceof Container) {
                Component[] subComponents = getAllComponents((Container) component);
                for (Component subComponent : subComponents) {
                    list.add(subComponent);
                }
            }
        }
        return list.toArray(new Component[0]);
    }

    /**
     * Xuất danh sách người dùng ra file CSV
     */
    private void handleExportCSV() {
        try {
            // Lấy dữ liệu từ database
            List<User> users = userDAO.getAllUsers();
            if (users.isEmpty()) {
                showWarning("Không có dữ liệu để xuất!");
                return;
            }

            // Chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("DanhSachNguoiDung.csv"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            // Ghi vào file CSV
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(filePath),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                // Write BOM for Excel UTF-8 recognition
                writer.write('\ufeff');

                // Ghi header
                writer.println("ID,Tên đăng nhập,Họ tên,Địa chỉ,Ngày sinh,Giới tính,Email,Trạng thái,Ngày tạo");

                // Ghi dữ liệu
                java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy");

                for (User user : users) {
                    String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            user.getId(),
                            escapeCsv(user.getUsername()),
                            escapeCsv(user.getFullName()),
                            escapeCsv(user.getAddress()),
                            user.getBirthDate() != null ? user.getBirthDate().format(dateFormatter) : "",
                            escapeCsv(user.getGender()),
                            escapeCsv(user.getEmail()),
                            formatStatus(user.getStatus()),
                            user.getCreatedAt() != null ? user.getCreatedAt().format(dateFormatter) : "");
                    writer.println(line);
                }
            }

            showSuccess("Đã xuất " + users.size() + " người dùng vào file:\n" + filePath);

        } catch (Exception e) {
            showError("Lỗi xuất file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escape special characters for CSV
     */
    private String escapeCsv(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }

    /**
     * Show confirm dialog with red cancel button
     */
    private int showStyledConfirmDialog(Component parent, Object message, String title) {
        // Create custom dialog
        final JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Message panel - handle both String and Component
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        if (message instanceof Component) {
            // If message is already a Component (like JPanel), add it directly
            messagePanel.add((Component) message, BorderLayout.CENTER);
        } else {
            // If message is String, wrap in JLabel
            JLabel messageLabel = new JLabel("<html>" + message.toString() + "</html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            messagePanel.add(messageLabel, BorderLayout.CENTER);
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Result holder
        final int[] result = { JOptionPane.CLOSED_OPTION };

        // Yes button (green)
        JButton yesButton = new JButton("Có");
        yesButton.setBackground(new Color(40, 167, 69)); // Green
        yesButton.setForeground(Color.WHITE);
        yesButton.setFont(new Font("Arial", Font.BOLD, 12));
        yesButton.setOpaque(true);
        yesButton.setBorderPainted(false);
        yesButton.setFocusPainted(false);
        yesButton.setPreferredSize(new Dimension(80, 35));
        yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        // No button (RED)
        JButton noButton = new JButton("Hủy");
        noButton.setBackground(new Color(220, 53, 69));
        noButton.setForeground(Color.WHITE);
        noButton.setFont(new Font("Arial", Font.BOLD, 12));
        noButton.setOpaque(true);
        noButton.setBorderPainted(false);
        noButton.setFocusPainted(false);
        noButton.setPreferredSize(new Dimension(80, 35));
        noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        dialog.add(messagePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * Export users to CSV
     */
    private void exportUsersToCSV() {
        try {
            if (userTable.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("DanhSachNguoiDung.csv"));

            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(filePath),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                writer.write('\ufeff');
                writer.println(
                        "ID,Username,Họ tên,Email,Số điện thoại,Giới tính,Ngày sinh,Địa chỉ,Trạng thái,Ngày tạo");

                for (int row = 0; row < userTable.getRowCount(); row++) {
                    writer.printf("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            userTable.getValueAt(row, 0),
                            userTable.getValueAt(row, 1),
                            userTable.getValueAt(row, 2),
                            userTable.getValueAt(row, 3),
                            userTable.getValueAt(row, 4),
                            userTable.getValueAt(row, 5),
                            userTable.getValueAt(row, 6),
                            userTable.getValueAt(row, 7),
                            userTable.getValueAt(row, 8),
                            userTable.getValueAt(row, 9));
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + userTable.getRowCount() + " người dùng vào:\n" + filePath,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}
