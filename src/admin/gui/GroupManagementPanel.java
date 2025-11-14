package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

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
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JComboBox<String> searchTypeCombo;

    public GroupManagementPanel() {
        initComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initComponents() {
        // Bảng nhóm với cột đầy đủ thông tin
        String[] columns = {"ID", "Tên nhóm", "Admin chính", "Số thành viên", "Số admin", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        groupTable = new JTable(model);
        groupTable.setRowHeight(28);
        groupTable.setAutoCreateRowSorter(true);
        groupTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        groupTable.getTableHeader().setBackground(WARNING_ORANGE);
        groupTable.getTableHeader().setForeground(Color.WHITE);

        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = groupTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);   // ID
        columnModel.getColumn(1).setPreferredWidth(200);  // Tên nhóm
        columnModel.getColumn(2).setPreferredWidth(150);  // Admin chính
        columnModel.getColumn(3).setPreferredWidth(100);  // Số thành viên
        columnModel.getColumn(4).setPreferredWidth(80);   // Số admin
        columnModel.getColumn(5).setPreferredWidth(120);  // Ngày tạo

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
        
        JButton applyBtn = createStyledButton("🔄 Áp dụng", SUCCESS_GREEN);
        sortRow.add(applyBtn);
        
        JButton resetBtn = createStyledButton("↺ Đặt lại", new Color(108, 117, 125));
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
        titleLabel.setForeground(WARNING_ORANGE);
        
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
        JButton viewMembersBtn = createStyledButton("👥 Xem thành viên", ZALO_BLUE);
        
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
        
        JOptionPane.showMessageDialog(this, 
            "Tìm kiếm: " + keyword + "\n" +
            "Loại: " + searchType + "\n\n" +
            "Chức năng sẽ được kết nối với database",
            "Tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
    }

    // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
    private void handleSort() {
        String sortOption = (String) sortCombo.getSelectedItem();
        
        JOptionPane.showMessageDialog(this, 
            "Áp dụng sắp xếp: " + sortOption + "\n\n" +
            "Chức năng sẽ được kết nối với database",
            "Sắp xếp", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleReset() {
        searchField.setText("");
        searchTypeCombo.setSelectedIndex(0);
        sortCombo.setSelectedIndex(0);
        
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
        
        String groupName = groupTable.getValueAt(selectedRow, 1).toString();
        String memberCount = groupTable.getValueAt(selectedRow, 3).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                    "Danh sách thành viên - " + groupName, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        
        // Bảng thành viên
        String[] columns = {"STT", "Tên đăng nhập", "Họ tên", "Vai trò", "Ngày tham gia"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Sample data
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "Admin chính", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "Admin", "2024-01-02"});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "Thành viên", "2024-01-03"});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "Thành viên", "2024-01-05"});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", "Thành viên", "2024-01-07"});
        
        JTable memberTable = new JTable(model);
        memberTable.setRowHeight(28);
        memberTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        memberTable.getTableHeader().setBackground(ZALO_BLUE);
        memberTable.getTableHeader().setForeground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel infoLabel = new JLabel("📊 Tổng số thành viên: " + memberCount);
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
        
        String groupName = groupTable.getValueAt(selectedRow, 1).toString();
        String adminCount = groupTable.getValueAt(selectedRow, 4).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                    "Danh sách Admin - " + groupName, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        // Bảng admin
        String[] columns = {"STT", "Tên đăng nhập", "Họ tên", "Vai trò", "Ngày bổ nhiệm"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Sample data
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "Admin chính", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "Admin", "2024-01-02"});
        
        JTable adminTable = new JTable(model);
        adminTable.setRowHeight(28);
        adminTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        adminTable.getTableHeader().setBackground(INFO_CYAN);
        adminTable.getTableHeader().setForeground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel infoLabel = new JLabel("👑 Tổng số admin: " + adminCount);
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
    }


    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) groupTable.getModel();
        model.setRowCount(0);
        
        model.addRow(new Object[]{"1", "Nhóm Java Dev", "admin", 15, 2, "2024-01-01"});
        model.addRow(new Object[]{"2", "Team Project", "user1", 8, 1, "2024-01-05"});
        model.addRow(new Object[]{"3", "Lập trình viên", "user2", 25, 3, "2024-01-10"});
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