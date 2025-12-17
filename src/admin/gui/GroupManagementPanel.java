package admin.gui;

import admin.service.GroupDAO;
import admin.service.UserDAO;
import admin.socket.ChatGroup;
import admin.socket.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện quản lý nhóm chat - ĐẦY ĐỦ CHỨC NĂNG
 * Yêu cầu: a) Sắp xếp, b) Lọc, c) Xem thành viên, d) Xem admin
 */
public class GroupManagementPanel extends JPanel {
    // Định nghĩa màu
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color INFO_CYAN = new Color(23, 162, 184);

    // Components
    private JTable groupTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JComboBox<String> searchTypeCombo;
    private JLabel totalLabel;

    // Backend
    private GroupDAO groupDAO;
    private UserDAO userDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GroupManagementPanel() {
        this.groupDAO = new GroupDAO();
        this.userDAO = new UserDAO();
        initComponents();
        setupLayout();
        loadGroupsFromDatabase();
        setupEventHandlers();
    }

    private void initComponents() {
        // Bảng nhóm với cột đầy đủ thông tin
        String[] columns = { "ID", "Tên nhóm", "Admin (Số lượng)", "Số thành viên", "Ngày tạo" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        groupTable = new JTable(tableModel);
        groupTable.setRowHeight(28);
        groupTable.setAutoCreateRowSorter(true);
        groupTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        groupTable.getTableHeader().setBackground(Color.WHITE);
        groupTable.getTableHeader().setForeground(Color.BLACK);

        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = groupTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Tên nhóm
        columnModel.getColumn(2).setPreferredWidth(200); // Admin (Số lượng)
        columnModel.getColumn(3).setPreferredWidth(100); // Số thành viên
        columnModel.getColumn(4).setPreferredWidth(120); // Ngày tạo

        // Yêu cầu b: Tìm kiếm/lọc theo tên
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[] {
                "Tìm theo tên nhóm",
                "Tìm theo admin"
        });

        // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
        sortCombo = new JComboBox<>(new String[] {
                "Sắp xếp theo tên (A-Z)",
                "Sắp xếp theo tên (Z-A)",
                "Sắp xếp theo ngày tạo (Mới nhất)",
                "Sắp xếp theo ngày tạo (Cũ nhất)",
        });
    }

    /**
     * Load danh sách nhóm từ database - ASYNC với loading indicator
     */
    private void loadGroupsFromDatabase() {
        // Clear table và hiển thị loading
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[] { "⏳", "Đang tải dữ liệu...", "", "", "" });

        System.out.println("🔍 [GroupManagementPanel] Loading groups from database...");

        SwingWorker<List<ChatGroup>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ChatGroup> doInBackground() {
                try {
                    System.out.println("⏳ [GroupManagementPanel] Calling groupDAO.getAllGroups()...");
                    List<ChatGroup> groups = groupDAO.getAllGroups();
                    System.out.println("✅ [GroupManagementPanel] Loaded " + groups.size() + " groups");
                    return groups;
                } catch (SQLException e) {
                    System.err.println("❌ [GroupManagementPanel] Database error: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    currentGroups = get();
                    applySorting();

                    // Update label tổng số nhóm
                    if (totalLabel != null) {
                        totalLabel.setText("📊 Tổng số nhóm: " + currentGroups.size());
                    }

                    System.out.println("📊 [GroupManagementPanel] Displaying " + currentGroups.size() + " groups");
                    displayGroups(currentGroups);

                } catch (Exception e) {
                    // Clear loading message
                    tableModel.setRowCount(0);

                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    String detailedMsg = "Lỗi load dữ liệu nhóm chat: " + errorMsg;

                    if (errorMsg != null && (errorMsg.contains("connection") ||
                            errorMsg.contains("Connection"))) {
                        detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                                "- Kết nối database\n" +
                                "- File config.properties\n" +
                                "Hoặc liên hệ admin để được hỗ trợ.";
                    }

                    System.err.println("❌ [GroupManagementPanel] Error in done(): " + errorMsg);
                    showError(detailedMsg);
                    e.printStackTrace();

                    // Hiển thị lỗi trong table
                    tableModel.addRow(new Object[] { "❌", "Lỗi tải dữ liệu", errorMsg, "", "" });
                }
            }
        };

        worker.execute();
    }

    /**
     * Hiển thị danh sách nhóm lên table - TỐI ƯU (không gọi database trong loop)
     */
    private void displayGroups(List<ChatGroup> groups) {
        tableModel.setRowCount(0); // Clear table

        for (ChatGroup group : groups) {
            // Sử dụng adminCount đã được load sẵn từ database (không gọi countGroupAdmins
            // nữa!)
            int adminCount = group.getAdminCount();

            String adminDisplay = (group.getCreatorName() != null ? group.getCreatorName() : "N/A")
                    + " (+" + (adminCount - 1) + " admin khác)";
            if (adminCount == 1) {
                adminDisplay = group.getCreatorName() != null ? group.getCreatorName() : "N/A";
            }

            Object[] row = {
                    group.getId(),
                    group.getGroupName() != null ? group.getGroupName() : "",
                    adminDisplay,
                    group.getMemberCount(),
                    group.getCreatedAt() != null ? group.getCreatedAt().format(dateFormatter) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Search and Filter panel (Yêu cầu a, b)
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Button panel (Yêu cầu c, d)
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(15, 15, 15, 15)));

        // Title with search icon (PNG, not emoji)
        ImageIcon searchIconTitle = loadIcon("search", 18, 18);
        JLabel titleLabel;
        if (searchIconTitle != null) {
            titleLabel = new JLabel("Tìm kiếm & Lọc nhóm chat", searchIconTitle, JLabel.LEFT);
            titleLabel.setIconTextGap(6);
        } else {
            titleLabel = new JLabel("Tìm kiếm & Lọc nhóm chat");
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Row 1: Search (Yêu cầu b)
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchRow.add(new JLabel("Loại tìm kiếm:"));
        searchTypeCombo.setPreferredSize(new Dimension(150, 30));
        searchRow.add(searchTypeCombo);

        searchRow.add(new JLabel("Từ khóa:"));
        searchField.setPreferredSize(new Dimension(250, 30));
        searchRow.add(searchField);

        panel.add(searchRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 2: Sort (Yêu cầu a)
        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sortRow.setOpaque(false);
        sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        sortRow.add(new JLabel("Sắp xếp:"));
        sortCombo.setPreferredSize(new Dimension(280, 30));
        sortRow.add(sortCombo);

        // Nút tìm kiếm và lọc với icon
        ImageIcon searchIcon = loadIcon("search", 16, 16);
        JButton searchFilterBtn = createStyledButtonWithIcon("Tìm kiếm + Lọc", ZALO_BLUE, searchIcon);
        searchFilterBtn.addActionListener(e -> handleSearchAndFilter());
        sortRow.add(searchFilterBtn);

        // Nút đặt lại với icon
        ImageIcon resetIcon = loadIcon("reset", 16, 16);
        JButton resetBtn = createStyledButtonWithIcon("Đặt lại", ZALO_BLUE, resetIcon);
        resetBtn.addActionListener(e -> handleReset());
        sortRow.add(resetBtn);

        panel.add(sortRow);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        // Header with statistics
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Title with icon
        ImageIcon chatIcon = loadIcon("chat", 20, 20);
        JLabel titleLabel;
        if (chatIcon != null) {
            titleLabel = new JLabel("Danh sách nhóm chat", chatIcon, JLabel.LEFT);
            titleLabel.setIconTextGap(8);
        } else {
            titleLabel = new JLabel("Danh sách nhóm chat");
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        // Khởi tạo instance variable nếu chưa có
        if (this.totalLabel == null) {
            this.totalLabel = new JLabel("📊 Tổng số nhóm: 0");
        }
        this.totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsPanel.add(this.totalLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(groupTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);

        // Yêu cầu c: Xem danh sách thành viên - với icon
        ImageIcon membersIcon = loadIcon("user", 16, 16);
        JButton viewMembersBtn = createStyledButtonWithIcon("Xem thành viên", INFO_CYAN, membersIcon);

        // Yêu cầu d: Xem danh sách admin - với icon
        ImageIcon adminIcon = loadIcon("admin", 16, 16);
        if (adminIcon == null) {
            adminIcon = loadIcon("crown", 16, 16);
        }
        JButton viewAdminsBtn = createStyledButtonWithIcon("Danh sách admin", INFO_CYAN, adminIcon);

        // Xuất CSV - với icon
        ImageIcon exportIcon = loadIcon("export", 16, 16);
        if (exportIcon == null) {
            exportIcon = loadIcon("download", 16, 16);
        }
        JButton exportBtn = createStyledButtonWithIcon("Xuất CSV", INFO_CYAN, exportIcon);

        panel.add(viewMembersBtn);
        panel.add(viewAdminsBtn);
        panel.add(exportBtn);

        return panel;
    }

    private void setupEventHandlers() {
        // Nút duy nhất: Tìm kiếm + Lọc
        addActionToButton("Tìm kiếm + Lọc", e -> handleSearchAndFilter());

        // Đặt lại
        addActionToButton("Đặt lại", e -> handleReset());

        // Yêu cầu c: Xem thành viên
        addActionToButton("Xem thành viên", e -> showMembersDialog());

        // Yêu cầu d: Xem admin
        addActionToButton("Danh sách admin", e -> showAdminsDialog());

        // Xuất CSV
        addActionToButton("Xuất CSV", e -> handleExportCSV());
    }

    // Cache danh sách nhóm để sắp xếp
    private List<ChatGroup> currentGroups = new ArrayList<>();

    // ==================== EVENT HANDLERS ====================

    /**
     * Xử lý tìm kiếm và lọc kết hợp
     * - Nếu có từ khóa: tìm kiếm theo từ khóa
     * - Nếu không có từ khóa: lấy tất cả nhóm
     * - Sau đó áp dụng sắp xếp
     */
    private void handleSearchAndFilter() {
        String keyword = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();

        try {
            // Bước 1: Lấy danh sách groups (có hoặc không có từ khóa)
            List<ChatGroup> groups;
            if (!keyword.isEmpty()) {
                boolean searchByAdmin = "Tìm theo admin".equals(searchType);
                groups = groupDAO.searchGroups(keyword, searchByAdmin);
            } else {
                groups = groupDAO.getAllGroups();
            }

            // Bước 2: Cập nhật danh sách hiện tại
            currentGroups = groups;

            // Bước 3: Áp dụng sắp xếp
            applySorting();

            // Bước 4: Hiển thị
            displayGroups(currentGroups);

            // Bước 5: Cập nhật label tổng số
            if (totalLabel != null) {
                totalLabel.setText("📊 Tổng số nhóm: " + currentGroups.size());
            }

            // Thông báo kết quả
            String message = !keyword.isEmpty()
                    ? "Tìm thấy " + groups.size() + " nhóm"
                    : "Đã lọc " + groups.size() + " nhóm";
            JOptionPane.showMessageDialog(this, message,
                    "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            String detailedMsg = "Lỗi tìm kiếm/lọc nhóm chat: " + errorMsg;

            if (errorMsg != null && (errorMsg.contains("connection") ||
                    errorMsg.contains("Connection"))) {
                detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                        "- Kết nối database\n" +
                        "- Thông tin tìm kiếm\n" +
                        "Hoặc liên hệ admin để được hỗ trợ.";
            }

            showError(detailedMsg);
            e.printStackTrace();
        }
    }

    private void applySorting() {
        String sortOption = (String) sortCombo.getSelectedItem();
        if (sortOption == null || currentGroups.isEmpty())
            return;

        java.util.Comparator<ChatGroup> comparator;
        switch (sortOption) {
            case "Sắp xếp theo tên (A-Z)":
                comparator = java.util.Comparator
                        .comparing(g -> g.getGroupName() != null ? g.getGroupName().toLowerCase() : "");
                break;
            case "Sắp xếp theo tên (Z-A)":
                comparator = java.util.Comparator
                        .comparing((ChatGroup g) -> g.getGroupName() != null ? g.getGroupName().toLowerCase() : "")
                        .reversed();
                break;
            case "Sắp xếp theo ngày tạo (Cũ nhất)":
                comparator = java.util.Comparator.comparing(ChatGroup::getCreatedAt,
                        java.util.Comparator.nullsLast(java.time.LocalDateTime::compareTo));
                break;
            case "Sắp xếp theo ngày tạo (Mới nhất)":
            default:
                comparator = java.util.Comparator.comparing(ChatGroup::getCreatedAt,
                        java.util.Comparator.nullsLast(java.time.LocalDateTime::compareTo)).reversed();
                break;
        }
        currentGroups.sort(comparator);
    }

    private void handleReset() {
        searchField.setText("");
        searchTypeCombo.setSelectedIndex(0);
        sortCombo.setSelectedIndex(0);
        loadGroupsFromDatabase();

        JOptionPane.showMessageDialog(this,
                "Đã đặt lại bộ lọc!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // Yêu cầu c: Xem danh sách thành viên 1 nhóm
    private void showMembersDialog() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhóm chat!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int groupId = (int) groupTable.getValueAt(selectedRow, 0);
        String groupName = groupTable.getValueAt(selectedRow, 1).toString();

        try {
            List<User> members = groupDAO.getGroupMembers(groupId);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Danh sách thành viên - " + groupName, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);

            // Bảng thành viên
            String[] columns = { "STT", "Tên đăng nhập", "Họ tên", "Ngày sinh", "Giới tính", "Email", "Trạng thái" };
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int stt = 1;
            for (User member : members) {
                try {
                    // Lấy đầy đủ thông tin user từ database
                    User fullUserInfo = userDAO.getUserById(member.getId());
                    if (fullUserInfo != null) {
                        model.addRow(new Object[] {
                                stt++,
                                fullUserInfo.getUsername(),
                                fullUserInfo.getFullName() != null ? fullUserInfo.getFullName() : "",
                                fullUserInfo.getBirthDate() != null ? fullUserInfo.getBirthDate().format(dateFormatter)
                                        : "",
                                fullUserInfo.getGender() != null ? fullUserInfo.getGender() : "",
                                fullUserInfo.getEmail() != null ? fullUserInfo.getEmail() : "",
                                "active".equals(fullUserInfo.getStatus()) ? "Hoạt động" : "Bị khóa"
                        });
                    }
                } catch (SQLException e) {
                    System.err.println("Lỗi lấy thông tin user ID: " + member.getId());
                }
            }

            JTable memberTable = new JTable(model);
            memberTable.setRowHeight(28);
            memberTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            memberTable.getTableHeader().setBackground(ZALO_BLUE);
            memberTable.getTableHeader().setForeground(Color.WHITE);

            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel infoLabel = new JLabel("📊 Tổng số thành viên: " + members.size());
            infoLabel.setFont(new Font("Arial", Font.BOLD, 13));
            infoLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

            contentPanel.add(infoLabel, BorderLayout.NORTH);
            contentPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = createStyledButton("Đóng", DANGER_RED);
            closeBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeBtn);

            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(contentPanel);
            dialog.setVisible(true);

        } catch (SQLException e) {
            showError("Lỗi lấy danh sách thành viên: " + e.getMessage());
        }
    }

    // Yêu cầu d: Xem danh sách admin 1 nhóm
    private void showAdminsDialog() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhóm chat!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int groupId = (int) groupTable.getValueAt(selectedRow, 0);
        String groupName = groupTable.getValueAt(selectedRow, 1).toString();

        try {
            List<User> admins = groupDAO.getGroupAdmins(groupId);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Danh sách Admin - " + groupName, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);

            // Bảng admin
            String[] columns = { "STT", "Tên đăng nhập", "Họ tên", "Ngày sinh", "Giới tính", "Email", "Trạng thái" };
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int stt = 1;
            for (User admin : admins) {
                try {
                    // Lấy đầy đủ thông tin user từ database
                    User fullUserInfo = userDAO.getUserById(admin.getId());
                    if (fullUserInfo != null) {
                        model.addRow(new Object[] {
                                stt++,
                                fullUserInfo.getUsername(),
                                fullUserInfo.getFullName() != null ? fullUserInfo.getFullName() : "",
                                fullUserInfo.getBirthDate() != null ? fullUserInfo.getBirthDate().format(dateFormatter)
                                        : "",
                                fullUserInfo.getGender() != null ? fullUserInfo.getGender() : "",
                                fullUserInfo.getEmail() != null ? fullUserInfo.getEmail() : "",
                                "active".equals(fullUserInfo.getStatus()) ? "Hoạt động" : "Bị khóa"
                        });
                    }
                } catch (SQLException e) {
                    System.err.println("Lỗi lấy thông tin user ID: " + admin.getId());
                }
            }

            JTable adminTable = new JTable(model);
            adminTable.setRowHeight(28);
            adminTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            adminTable.getTableHeader().setBackground(INFO_CYAN);
            adminTable.getTableHeader().setForeground(Color.WHITE);

            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel infoLabel = new JLabel("👑 Tổng số admin: " + admins.size());
            infoLabel.setFont(new Font("Arial", Font.BOLD, 13));
            infoLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

            contentPanel.add(infoLabel, BorderLayout.NORTH);
            contentPanel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = createStyledButton("Đóng", DANGER_RED);
            closeBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeBtn);

            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(contentPanel);
            dialog.setVisible(true);

        } catch (SQLException e) {
            showError("Lỗi lấy danh sách admin: " + e.getMessage());
        }
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

    /**
     * Tạo button với icon và text - Đã tối ưu độ rộng để không bị cắt chữ
     */
    private JButton createStyledButtonWithIcon(String text, Color color, ImageIcon icon) {
        JButton button;
        if (icon != null) {
            button = new JButton(text, icon);
            button.setIconTextGap(10); // Khoảng cách giữa icon và chữ
        } else {
            button = new JButton(text);
        }

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Margin bên trong nút (Top, Left, Bottom, Right)
        button.setMargin(new Insets(8, 20, 8, 20));

        // --- TÍNH TOÁN ĐỘ RỘNG ---

        // 1. Tính độ rộng lý thuyết của nội dung
        java.awt.FontMetrics fm = button.getFontMetrics(button.getFont());
        int textWidth = fm.stringWidth(text);
        int iconWidth = (icon != null) ? icon.getIconWidth() : 0;
        int iconGap = (icon != null) ? 10 : 0;

        // 2. Tính tổng độ rộng cần thiết
        // QUAN TRỌNG: +120px padding để bù cho margin và bo góc của giao diện Mac
        int calculatedWidth = textWidth + iconWidth + iconGap + 120;

        // 3. Quy định độ rộng tối thiểu là 160px để các nút ngắn (như "Đặt lại") không
        // bị quá bé
        int finalWidth = Math.max(calculatedWidth, 160);

        // 4. Áp dụng kích thước
        Dimension dim = new Dimension(finalWidth, 38);
        button.setPreferredSize(dim);
        button.setMinimumSize(dim);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        return button;
    }

    /**
     * Load icon từ file PNG
     */
    private ImageIcon loadIcon(String iconName, int width, int height) {
        try {
            String path = "icons/" + iconName + ".png";
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            // Icon không tồn tại, trả về null
        }
        return null;
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void addActionToButton(String buttonText, java.awt.event.ActionListener action) {
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

    /**
     * Xuất danh sách nhóm chat ra file CSV
     */
    private void handleExportCSV() {
        try {
            // Lấy dữ liệu từ currentGroups (data hiện tại đang hiển thị)
            if (currentGroups.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có dữ liệu để xuất!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("DanhSachNhomChat.csv"));

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
                writer.println("ID,Tên nhóm,Admin chính,Số thành viên,Ngày tạo");

                // Ghi dữ liệu
                for (ChatGroup group : currentGroups) {
                    String line = String.format("%d,\"%s\",\"%s\",%d,\"%s\"",
                            group.getId(),
                            escapeCsv(group.getGroupName()),
                            escapeCsv(group.getCreatorName()),
                            group.getMemberCount(),
                            group.getCreatedAt() != null ? group.getCreatedAt().format(dateFormatter) : "");
                    writer.println(line);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + currentGroups.size() + " nhóm vào file:\n" + filePath,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

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
}