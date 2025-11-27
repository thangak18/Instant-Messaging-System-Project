package admin.gui;

import admin.service.GroupDAO;
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
    
    // Backend
    private GroupDAO groupDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GroupManagementPanel() {
        this.groupDAO = new GroupDAO();
        initComponents();
        setupLayout();
        loadGroupsFromDatabase();
        setupEventHandlers();
    }

    private void initComponents() {
        // Bảng nhóm với cột đầy đủ thông tin
        String[] columns = {"ID", "Tên nhóm", "Admin chính", "Số thành viên", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        groupTable = new JTable(tableModel);
        groupTable.setRowHeight(28);
        groupTable.setAutoCreateRowSorter(true);
        groupTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        groupTable.getTableHeader().setBackground(Color.WHITE);
        groupTable.getTableHeader().setForeground(Color.BLACK);

        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = groupTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);   // ID
        columnModel.getColumn(1).setPreferredWidth(250);  // Tên nhóm
        columnModel.getColumn(2).setPreferredWidth(150);  // Admin chính
        columnModel.getColumn(3).setPreferredWidth(100);  // Số thành viên
        columnModel.getColumn(4).setPreferredWidth(120);  // Ngày tạo

        // Yêu cầu b: Tìm kiếm/lọc theo tên
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[]{
            "Tìm theo tên nhóm", 
            "Tìm theo admin"
        });

        // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
        sortCombo = new JComboBox<>(new String[]{
            "Sắp xếp theo tên (A-Z)",
            "Sắp xếp theo tên (Z-A)",
            "Sắp xếp theo ngày tạo (Mới nhất)",
            "Sắp xếp theo ngày tạo (Cũ nhất)",
        });
    }
    
    /**
     * Load danh sách nhóm từ database
     */
    private void loadGroupsFromDatabase() {
        try {
            currentGroups = groupDAO.getAllGroups();
            applySorting();
            displayGroups(currentGroups);
        } catch (SQLException e) {
            showError("Lỗi load dữ liệu nhóm: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị danh sách nhóm lên table
     */
    private void displayGroups(List<ChatGroup> groups) {
        tableModel.setRowCount(0); // Clear table
        
        for (ChatGroup group : groups) {
            Object[] row = {
                group.getId(),
                group.getGroupName(),
                group.getCreatorName(),
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
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("🔍 Tìm kiếm & Lọc nhóm chat");
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
        
        JButton searchBtn = createStyledButton("🔍 Tìm kiếm", ZALO_BLUE);
        searchRow.add(searchBtn);
        
        panel.add(searchRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 2: Sort (Yêu cầu a)
        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sortRow.setOpaque(false);
        sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sortRow.add(new JLabel("Sắp xếp:"));
        sortCombo.setPreferredSize(new Dimension(280, 30));
        sortRow.add(sortCombo);
        
        JButton applyBtn = createStyledButton("🔄 Áp dụng", ZALO_BLUE);
        sortRow.add(applyBtn);
        
        JButton resetBtn = createStyledButton("↺ Đặt lại", ZALO_BLUE);
        sortRow.add(resetBtn);
        
        panel.add(sortRow);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Header with statistics
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("👥 Danh sách nhóm chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("📊 Tổng số nhóm: 3");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsPanel.add(totalLabel);
        
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

        // Yêu cầu c: Xem danh sách thành viên
        JButton viewMembersBtn = createStyledButton("👥 Xem thành viên", INFO_CYAN);
        
        // Yêu cầu d: Xem danh sách admin
        JButton viewAdminsBtn = createStyledButton("👑 Xem danh sách admin", INFO_CYAN);
        
        

        panel.add(viewMembersBtn);
        panel.add(viewAdminsBtn);


        return panel;
    }

    private void setupEventHandlers() {
        // Yêu cầu b: Tìm kiếm
        addActionToButton("🔍 Tìm kiếm", e -> handleSearch());
        
        // Yêu cầu a: Áp dụng sắp xếp
        addActionToButton("🔄 Áp dụng", e -> handleSort());
        
        // Đặt lại
        addActionToButton("↺ Đặt lại", e -> handleReset());
        
        // Yêu cầu c: Xem thành viên
        addActionToButton("👥 Xem thành viên", e -> showMembersDialog());
        
        // Yêu cầu d: Xem admin
        addActionToButton("👑 Xem danh sách admin", e -> showAdminsDialog());
        
    }

    // Cache danh sách nhóm để sắp xếp
    private List<ChatGroup> currentGroups = new ArrayList<>();

    // ==================== EVENT HANDLERS ====================
    
    // Yêu cầu b: Tìm kiếm/lọc theo tên
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();
        
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập từ khóa tìm kiếm!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            boolean searchByAdmin = "Tìm theo admin".equals(searchType);
            List<ChatGroup> groups = groupDAO.searchGroups(keyword, searchByAdmin);
            currentGroups = groups;
            applySorting();
            displayGroups(currentGroups);
            
            JOptionPane.showMessageDialog(this, 
                "Tìm thấy " + groups.size() + " nhóm",
                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
    private void handleSort() {
        applySorting();
        displayGroups(currentGroups);
    }
    
    private void applySorting() {
        String sortOption = (String) sortCombo.getSelectedItem();
        if (sortOption == null || currentGroups.isEmpty()) return;
        
        java.util.Comparator<ChatGroup> comparator;
        switch (sortOption) {
            case "Sắp xếp theo tên (A-Z)":
                comparator = java.util.Comparator.comparing(g -> 
                    g.getGroupName() != null ? g.getGroupName().toLowerCase() : "");
                break;
            case "Sắp xếp theo tên (Z-A)":
                comparator = java.util.Comparator.comparing((ChatGroup g) -> 
                    g.getGroupName() != null ? g.getGroupName().toLowerCase() : "").reversed();
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
            String[] columns = {"STT", "Tên đăng nhập", "Họ tên", "Email", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            
            int stt = 1;
            for (User member : members) {
                model.addRow(new Object[]{
                    stt++,
                    member.getUsername(),
                    member.getFullName(),
                    member.getEmail(),
                    "active".equals(member.getStatus()) ? "Hoạt động" : "Bị khóa"
                });
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
            JButton closeBtn = createStyledButton("❌ Đóng", DANGER_RED);
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
            String[] columns = {"STT", "Tên đăng nhập", "Họ tên", "Email", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            
            int stt = 1;
            for (User admin : admins) {
                model.addRow(new Object[]{
                    stt++,
                    admin.getUsername(),
                    admin.getFullName(),
                    admin.getEmail(),
                    "active".equals(admin.getStatus()) ? "Hoạt động" : "Bị khóa"
                });
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
            JButton closeBtn = createStyledButton("❌ Đóng", DANGER_RED);
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
}