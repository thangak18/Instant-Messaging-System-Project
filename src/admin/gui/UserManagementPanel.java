package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Quản lý danh sách người dùng - Đầy đủ chức năng
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
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JComboBox<String> searchTypeCombo;

    public UserManagementPanel() {
        initComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initComponents() {
        // Yêu cầu: Thông tin đầy đủ
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh", 
                           "Giới tính", "Email", "Trạng thái", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(model);
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
        
        // Yêu cầu e: Lịch sử đăng nhập
        addActionToButton("📜 Lịch sử đăng nhập", e -> showLoginHistoryDialog());
        
        // Yêu cầu f: Danh sách bạn bè
        addActionToButton("👥 Danh sách bạn bè", e -> showFriendsListDialog());
        
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
    
    // Yêu cầu b: Xóa người dùng
    private void showDeleteUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần xóa!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa người dùng: " + username + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) userTable.getModel();
            model.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Đã xóa người dùng thành công!");
        }
    }
    
    // Yêu cầu c: Khóa tài khoản
    private void showLockAccountDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần khóa!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        String currentStatus = userTable.getValueAt(selectedRow, 7).toString();
        
        // Kiểm tra xem tài khoản đã bị khóa chưa
        if (currentStatus.equals("Bị khóa")) {
            JOptionPane.showMessageDialog(this, 
                "Tài khoản " + username + " đã bị khóa rồi!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn khóa tài khoản: " + username + "?\n\n" +
            "Người dùng sẽ không thể đăng nhập sau khi bị khóa.",
            "Xác nhận khóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            userTable.setValueAt("Bị khóa", selectedRow, 7);
            JOptionPane.showMessageDialog(this, 
                "Đã khóa tài khoản " + username + " thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Yêu cầu c: Mở khóa tài khoản
    private void showUnlockAccountDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần mở khóa!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        String currentStatus = userTable.getValueAt(selectedRow, 7).toString();
        
        // Kiểm tra xem tài khoản có đang bị khóa không
        if (currentStatus.equals("Hoạt động")) {
            JOptionPane.showMessageDialog(this, 
                "Tài khoản " + username + " đang hoạt động bình thường!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn mở khóa tài khoản: " + username + "?\n\n" +
            "Người dùng sẽ có thể đăng nhập trở lại sau khi được mở khóa.",
            "Xác nhận mở khóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            userTable.setValueAt("Hoạt động", selectedRow, 7);
            JOptionPane.showMessageDialog(this, 
                "Đã mở khóa tài khoản " + username + " thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Yêu cầu d: Cập nhật mật khẩu
    private void showChangePasswordDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                    "Đổi mật khẩu - " + username, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        formPanel.add(new JLabel("Mật khẩu mới:"));
        JPasswordField newPassField = new JPasswordField();
        formPanel.add(newPassField);
        
        formPanel.add(new JLabel("Xác nhận mật khẩu:"));
        JPasswordField confirmPassField = new JPasswordField();
        formPanel.add(confirmPassField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = createStyledButton("💾 Lưu", SUCCESS_GREEN);
        JButton cancelBtn = createStyledButton("❌ Hủy", DANGER_RED);
        
        saveBtn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu không được để trống!");
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp!");
                return;
            }
            
            // TODO: Update password in database
            JOptionPane.showMessageDialog(dialog, "Đổi mật khẩu thành công!");
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    // Yêu cầu e: Xem lịch sử đăng nhập
    private void showLoginHistoryDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                    "Lịch sử đăng nhập - " + username, true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        
        String[] columns = {"Thời gian", "IP Address", "Thiết bị", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Sample data
        model.addRow(new Object[]{"2024-01-15 10:30:00", "192.168.1.100", "Windows 10", "Thành công"});
        model.addRow(new Object[]{"2024-01-14 14:20:00", "192.168.1.101", "iPhone 12", "Thành công"});
        model.addRow(new Object[]{"2024-01-13 08:15:00", "192.168.1.100", "Windows 10", "Thành công"});
        
        JTable historyTable = new JTable(model);
        historyTable.setRowHeight(25);
        
        dialog.add(new JScrollPane(historyTable));
        dialog.setVisible(true);
    }
    
    // Yêu cầu f: Danh sách bạn bè
    private void showFriendsListDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!", 
                                         "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = userTable.getValueAt(selectedRow, 1).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                    "Danh sách bạn bè - " + username, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        String[] columns = {"Tên đăng nhập", "Họ tên", "Ngày kết bạn", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Sample data
        model.addRow(new Object[]{"user2", "Trần Thị B", "2024-01-10", "Bạn bè"});
        model.addRow(new Object[]{"user3", "Lê Văn C", "2024-01-12", "Bạn bè"});
        model.addRow(new Object[]{"user4", "Phạm Thị D", "2024-01-14", "Bạn bè"});
        
        JTable friendsTable = new JTable(model);
        friendsTable.setRowHeight(25);
        
        dialog.add(new JScrollPane(friendsTable));
        dialog.setVisible(true);
    }
    
    // Yêu cầu a: Tìm kiếm
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();
        
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Đang tìm kiếm: " + keyword + "\nLoại: " + searchType + "\n\nChức năng sẽ được triển khai với database",
            "Tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Yêu cầu a: Lọc và sắp xếp
    private void handleFilterAndSort() {
        String status = (String) statusFilter.getSelectedItem();
        String sortOption = (String) sortCombo.getSelectedItem();
        
        JOptionPane.showMessageDialog(this, 
            "Áp dụng lọc:\nTrạng thái: " + status + "\nSắp xếp: " + sortOption + 
            "\n\nChức năng sẽ được triển khai với database",
            "Lọc & Sắp xếp", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "Hà Nội", "1990-01-01", 
                                  "Nam", "admin@chat.com", "Hoạt động", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "TP HCM", "1995-05-15", 
                                  "Nam", "user1@gmail.com", "Hoạt động", "2024-01-02"});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "Đà Nẵng", "1998-08-20", 
                                  "Nữ", "user2@gmail.com", "Bị khóa", "2024-01-03"});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "Hải Phòng", "1992-03-10", 
                                  "Nam", "user3@gmail.com", "Hoạt động", "2024-01-04"});
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