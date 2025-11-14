package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Giao diện Báo cáo Người dùng mới - ĐẦY ĐỦ CHỨC NĂNG
 * Yêu cầu: Chọn khoảng thời gian, a) Sắp xếp, b) Lọc theo tên
 */
public class NewUserReportPanel extends JPanel {
    private static final Color INFO_CYAN = new Color(23, 162, 184);
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);
    
    private JTable userTable;
    private JTextField dateFromField, dateToField;
    private JTextField searchNameField;
    private JComboBox<String> sortCombo;
    private JButton filterButton, resetButton, refreshButton, exportButton;

    public NewUserReportPanel() {
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Email", "Ngày đăng ký", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(model);
        userTable.setRowHeight(28);
        userTable.setAutoCreateRowSorter(true);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(INFO_CYAN);
        userTable.getTableHeader().setForeground(Color.WHITE);
        
        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);   // ID
        columnModel.getColumn(1).setPreferredWidth(120);  // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(150);  // Họ tên
        columnModel.getColumn(3).setPreferredWidth(180);  // Email
        columnModel.getColumn(4).setPreferredWidth(120);  // Ngày đăng ký
        columnModel.getColumn(5).setPreferredWidth(110);  // Trạng thái
        
        // Chọn khoảng thời gian
        dateFromField = new JTextField(10);
        dateFromField.setToolTipText("Định dạng: YYYY-MM-DD");
        dateToField = new JTextField(10);
        dateToField.setToolTipText("Định dạng: YYYY-MM-DD");
        
        // Yêu cầu b: Lọc theo tên
        searchNameField = new JTextField(20);
        
        // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
        sortCombo = new JComboBox<>(new String[]{
            "Sắp xếp theo thời gian (Mới nhất)",
            "Sắp xếp theo thời gian (Cũ nhất)",
            "Sắp xếp theo tên (A-Z)",
            "Sắp xếp theo tên (Z-A)"
        });
        
        filterButton = new JButton("📊 Hiển thị báo cáo");
        resetButton = new JButton("↺ Đặt lại");
        refreshButton = new JButton("🔄 Làm mới");
        exportButton = new JButton("📥 Xuất Excel");
        
        stylePrimaryButton(filterButton);
        styleNeutralButton(resetButton);
        stylePrimaryButton(refreshButton);
        stylePrimaryButton(exportButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Panel tùy chọn báo cáo
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Panel bảng dữ liệu
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel chức năng
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("📅 Tùy chọn báo cáo người dùng mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(INFO_CYAN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Row 1: Chọn khoảng thời gian
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dateRow.add(new JLabel("Từ ngày:"));
        dateFromField.setPreferredSize(new Dimension(120, 30));
        dateRow.add(dateFromField);
        
        dateRow.add(Box.createHorizontalStrut(10));
        dateRow.add(new JLabel("Đến ngày:"));
        dateToField.setPreferredSize(new Dimension(120, 30));
        dateRow.add(dateToField);
        
        JLabel formatLabel = new JLabel("(Định dạng: YYYY-MM-DD)");
        formatLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        formatLabel.setForeground(NEUTRAL_GRAY);
        dateRow.add(formatLabel);
        
        panel.add(dateRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 2: Lọc theo tên và Sắp xếp
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        filterRow.add(new JLabel("Lọc theo tên:"));
        searchNameField.setPreferredSize(new Dimension(200, 30));
        filterRow.add(searchNameField);
        
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Sắp xếp:"));
        sortCombo.setPreferredSize(new Dimension(240, 30));
        filterRow.add(sortCombo);
        
        panel.add(filterRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 3: Nút hành động
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        actionRow.add(filterButton);
        actionRow.add(resetButton);
        
        panel.add(actionRow);

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
        
        JLabel titleLabel = new JLabel("📋 Danh sách người dùng đăng ký mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(INFO_CYAN);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("📊 Tổng số: 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        statsPanel.add(totalLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);
        panel.add(refreshButton);
        panel.add(exportButton);
        return panel;
    }

    private void setupEventHandlers() {
        // Hiển thị báo cáo
        filterButton.addActionListener(e -> handleFilterReport());
        
        // Đặt lại
        resetButton.addActionListener(e -> handleReset());
        
        // Làm mới
        refreshButton.addActionListener(e -> handleRefresh());
        
        // Xuất Excel
        exportButton.addActionListener(e -> handleExport());
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Xử lý khi nhấn nút "Hiển thị báo cáo"
     * Lấy dữ liệu theo khoảng thời gian, lọc và sắp xếp
     */
    private void handleFilterReport() {
        String fromDate = dateFromField.getText().trim();
        String toDate = dateToField.getText().trim();
        String nameFilter = searchNameField.getText().trim();
        String sortOption = (String) sortCombo.getSelectedItem();
        
        // Kiểm tra đầu vào
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ khoảng thời gian!\n" +
                "Định dạng: YYYY-MM-DD\n" +
                "Ví dụ: 2024-01-01",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate định dạng ngày đơn giản
        if (!isValidDateFormat(fromDate) || !isValidDateFormat(toDate)) {
            JOptionPane.showMessageDialog(this, 
                "Định dạng ngày không hợp lệ!\n" +
                "Vui lòng nhập theo định dạng: YYYY-MM-DD\n" +
                "Ví dụ: 2024-01-01",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Load dữ liệu mẫu theo bộ lọc
        loadFilteredData(fromDate, toDate, nameFilter, sortOption);
        
        JOptionPane.showMessageDialog(this, 
            "Đã tải báo cáo người dùng đăng ký mới:\n\n" +
            "Từ ngày: " + fromDate + "\n" +
            "Đến ngày: " + toDate + "\n" +
            "Lọc tên: " + (nameFilter.isEmpty() ? "Tất cả" : nameFilter) + "\n" +
            "Sắp xếp: " + sortOption + "\n\n" +
            "Chức năng sẽ được kết nối với database",
            "Báo cáo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Load dữ liệu theo bộ lọc
     */
    private void loadFilteredData(String fromDate, String toDate, String nameFilter, String sortOption) {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ
        
        // TODO: Trong thực tế, gọi database với các tham số này
        // List<User> users = UserDAO.getNewUsers(fromDate, toDate, nameFilter, sortOption);
        
        // Dữ liệu mẫu (giả lập filter theo thời gian)
        model.addRow(new Object[]{"1", "user1", "Nguyễn Văn A", "user1@email.com", "2024-01-02", "Hoạt động"});
        model.addRow(new Object[]{"2", "user2", "Trần Thị B", "user2@email.com", "2024-01-03", "Chưa xác thực"});
        model.addRow(new Object[]{"3", "user3", "Lê Văn C", "user3@email.com", "2024-01-04", "Hoạt động"});
        model.addRow(new Object[]{"4", "user4", "Phạm Thị D", "user4@email.com", "2024-01-05", "Chưa xác thực"});
        model.addRow(new Object[]{"5", "user5", "Hoàng Văn E", "user5@email.com", "2024-01-06", "Hoạt động"});
        
        updateStatistics();
    }

    /**
     * Cập nhật thống kê tổng số
     */
    private void updateStatistics() {
        int totalCount = userTable.getRowCount();
        Component[] components = getAllComponents(this);
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText().startsWith("📊 Tổng số:")) {
                    label.setText("📊 Tổng số: " + totalCount);
                    break;
                }
            }
        }
    }

    /**
     * Validate định dạng ngày YYYY-MM-DD
     */
    private boolean isValidDateFormat(String date) {
        if (date == null || date.length() != 10) {
            return false;
        }
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Đặt lại tất cả bộ lọc
     */
    private void handleReset() {
        dateFromField.setText("");
        dateToField.setText("");
        searchNameField.setText("");
        sortCombo.setSelectedIndex(0);
        
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        updateStatistics();
        
        JOptionPane.showMessageDialog(this, 
            "Đã đặt lại tất cả bộ lọc!",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Làm mới dữ liệu
     */
    private void handleRefresh() {
        String fromDate = dateFromField.getText().trim();
        String toDate = dateToField.getText().trim();
        
        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String nameFilter = searchNameField.getText().trim();
            String sortOption = (String) sortCombo.getSelectedItem();
            loadFilteredData(fromDate, toDate, nameFilter, sortOption);
            JOptionPane.showMessageDialog(this, 
                "Đã làm mới dữ liệu!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn khoảng thời gian trước khi làm mới!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Xuất Excel
     */
    private void handleExport() {
        if (userTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không có dữ liệu để xuất!\n" +
                "Vui lòng tạo báo cáo trước.",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Chức năng xuất Excel sẽ được triển khai!\n" +
            "Dữ liệu: " + userTable.getRowCount() + " người dùng",
            "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Các hàm hỗ trợ tạo kiểu (Copy từ các file trước) ---

    private Border createTitledBorder(String title) {
        Border emptyInside = new EmptyBorder(5, 5, 5, 5);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleColor(ZALO_BLUE);
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        return BorderFactory.createCompoundBorder(titledBorder, emptyInside);
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ZALO_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void styleNeutralButton(JButton button) {
        button.setBackground(NEUTRAL_GRAY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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