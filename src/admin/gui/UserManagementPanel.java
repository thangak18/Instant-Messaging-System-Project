package admin.gui;

import admin.dao.UserDAO;
import admin.model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Quản lý danh sách người dùng - Backend Integration
 * Yêu cầu: a) Lọc và sắp xếp, b) CRUD, c) Khóa/mở khóa, 
 * d) Cập nhật mật khẩu, e) Lịch sử đăng nhập, f) Danh sách bạn bè
 */
public class UserManagementPanel extends JPanel {
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);
    private static final Color INFO_CYAN = new Color(23, 162, 184);
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JComboBox<String> searchTypeCombo;
    
    // Backend DAO
    private UserDAO userDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UserManagementPanel() {
        try {
            this.userDAO = new UserDAO();
            initComponents();
            setupLayout();
            loadUsersFromDatabase(); // Load từ database thay vì sample data
            setupEventHandlers();
        } catch (Exception e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // Yêu cầu: Thông tin đầy đủ
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh", 
                           "Giới tính", "Email", "Trạng thái", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        userTable.setAutoCreateRowSorter(true);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(ZALO_BLUE);
        userTable.getTableHeader().setForeground(Color.WHITE);
        
        // Yêu cầu a: Lọc theo tên/tên đăng nhập/trạng thái
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[]{"Tìm theo tên", "Tìm theo tên đăng nhập", "Tìm theo email"});
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Bị khóa"});
        
        // Yêu cầu a: Sắp xếp theo tên/ngày tạo
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo ngày tạo (Mới nhất)", 
                                                  "Sắp xếp theo ngày tạo (Cũ nhất)"});
        
        // Adjust column widths
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);   // ID
        columnModel.getColumn(1).setPreferredWidth(100);  // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(120);  // Họ tên
        columnModel.getColumn(3).setPreferredWidth(150);  // Địa chỉ
        columnModel.getColumn(4).setPreferredWidth(80);   // Ngày sinh
        columnModel.getColumn(5).setPreferredWidth(60);   // Giới tính
        columnModel.getColumn(6).setPreferredWidth(150);  // Email
        columnModel.getColumn(7).setPreferredWidth(80);   // Trạng thái
        columnModel.getColumn(8).setPreferredWidth(90);   // Ngày tạo
    }
    
    /**
     * Load users từ database
     */
    private void loadUsersFromDatabase() {
        try {
            List<User> users = userDAO.getAllUsers();
            displayUsers(users);
        } catch (SQLException e) {
            showError("Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị danh sách users lên table
     */
    private void displayUsers(List<User> users) {
        tableModel.setRowCount(0); // Clear table
        
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getAddress() != null ? user.getAddress() : "",
                user.getBirthDate() != null ? user.getBirthDate().format(dateFormatter) : "",
                user.getGender() != null ? user.getGender() : "",
                user.getEmail(),
                user.getStatus().equals("active") ? "Hoạt động" : "Bị khóa",
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
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel("👤 Danh sách người dùng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
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
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("🔍 Tìm kiếm & Lọc người dùng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);

        // Row 1: Search
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.setOpaque(false);
        row1.add(new JLabel("Loại tìm kiếm:"));
        row1.add(searchTypeCombo);
        row1.add(new JLabel("Từ khóa:"));
        row1.add(searchField);
        row1.add(createStyledButton("🔍 Tìm kiếm", ZALO_BLUE));
        
        // Row 2: Filter and Sort
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.setOpaque(false);
        row2.add(new JLabel("Trạng thái:"));
        row2.add(statusFilter);
        row2.add(new JLabel("Sắp xếp:"));
        row2.add(sortCombo);
        row2.add(createStyledButton("🔄 Áp dụng", SUCCESS_GREEN));

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
        
        JButton addBtn = createStyledButton("➕ Thêm người dùng", SUCCESS_GREEN);
        JButton editBtn = createStyledButton("✏️ Sửa thông tin", ZALO_BLUE);
        JButton deleteBtn = createStyledButton("🗑️ Xóa người dùng", DANGER_RED);
        JButton lockBtn = createStyledButton("🔒 Khóa tài khoản", WARNING_ORANGE);
        JButton unlockBtn = createStyledButton("🔓 Mở khóa", SUCCESS_GREEN);
        
        row1.add(addBtn);
        row1.add(editBtn);
        row1.add(deleteBtn);
        row1.add(lockBtn);
        row1.add(unlockBtn);
        
        // Row 2: Các chức năng bổ sung (3 nút: Đổi mật khẩu, Lịch sử, Danh sách bạn bè)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row2.setOpaque(false);
        
        JButton passwordBtn = createStyledButton("🔑 Đổi mật khẩu", INFO_CYAN);
        JButton historyBtn = createStyledButton("📜 Lịch sử đăng nhập", new Color(108, 117, 125));
        JButton friendsBtn = createStyledButton("👥 Danh sách bạn bè", new Color(255, 99, 132));
        
        row2.add(passwordBtn);
        row2.add(historyBtn);
        row2.add(friendsBtn);
        
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
        
        // TODO: Implement these features later
        // addActionToButton("📜 Lịch sử đăng nhập", e -> showLoginHistoryDialog());
        // addActionToButton("👥 Danh sách bạn bè", e -> showFriendsListDialog());
        
        // Yêu cầu a: Tìm kiếm
        addActionToButton("🔍 Tìm kiếm", e -> handleSearch());
        
        // Yêu cầu a: Áp dụng filter và sort
        addActionToButton("🔄 Áp dụng", e -> handleFilterAndSort());
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
        String[] labels = {"Tên đăng nhập:", "Mật khẩu:", "Họ tên:", "Địa chỉ:", 
                          "Ngày sinh:", "Giới tính:", "Email:"};
        JComponent[] fields = {
            new JTextField(20),
            new JPasswordField(20),
            new JTextField(20),
            new JTextField(20),
            new JTextField(20),
            new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"}),
            new JTextField(20)
        };
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            formPanel.add(label, gbc);
            
            gbc.gridx = 1; gbc.weightx = 1;
            formPanel.add(fields[i], gbc);
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = createStyledButton("💾 Lưu", SUCCESS_GREEN);
        JButton cancelBtn = createStyledButton("❌ Hủy", DANGER_RED);
        
        saveBtn.addActionListener(e -> {
            // TODO: Save to database
            JOptionPane.showMessageDialog(dialog, "Thêm người dùng thành công!");
            dialog.dispose();
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần sửa!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected user data
        String username = userTable.getValueAt(selectedRow, 1).toString();
        String fullName = userTable.getValueAt(selectedRow, 2).toString();
        
        JOptionPane.showMessageDialog(this, 
            "Chức năng sửa thông tin cho người dùng: " + username + "\nSẽ được triển khai với database",
            "Sửa thông tin", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Yêu cầu a: Tìm kiếm
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }
        
        try {
            List<User> users = userDAO.searchUsers(keyword);
            displayUsers(users);
            showSuccess("Tìm thấy " + users.size() + " kết quả");
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Yêu cầu a: Lọc và sắp xếp
    private void handleFilterAndSort() {
        try {
            String statusValue = (String) statusFilter.getSelectedItem();
            List<User> users;
            
            if ("Tất cả".equals(statusValue)) {
                users = userDAO.getAllUsers();
            } else {
                String status = statusValue.equals("Hoạt động") ? "active" : "locked";
                users = userDAO.getUsersByStatus(status);
            }
            
            displayUsers(users);
            showSuccess("Đã lọc " + users.size() + " người dùng");
        } catch (SQLException e) {
            showError("Lỗi lọc dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa người dùng: " + username + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userDAO.deleteUser(userId);
                if (success) {
                    showSuccess("Xóa người dùng thành công!");
                    loadUsersFromDatabase(); // Reload table
                } else {
                    showError("Không thể xóa người dùng");
                }
            } catch (SQLException e) {
                showError("Lỗi xóa người dùng: " + e.getMessage());
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
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Khóa tài khoản: " + username + "?",
            "Xác nhận khóa", JOptionPane.YES_NO_OPTION);
        
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
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Mở khóa tài khoản: " + username + "?",
            "Xác nhận mở khóa", JOptionPane.YES_NO_OPTION);
        
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
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Đổi mật khẩu", JOptionPane.OK_CANCEL_OPTION);
        
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
    
    // Helper methods for showing messages
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", 
            JOptionPane.ERROR_MESSAGE);
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
}