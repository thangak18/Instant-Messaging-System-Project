package admin.gui;

import admin.service.StatisticsDAO;
import admin.socket.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

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
    private DefaultTableModel tableModel;
    private JTextField dateFromField, dateToField;
    private JTextField searchNameField;
    private JComboBox<String> sortCombo;
    private JButton filterButton, resetButton, refreshButton, exportButton;
    private JLabel totalLabel;

    // Backend
    private StatisticsDAO statisticsDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public NewUserReportPanel() {
        this.statisticsDAO = new StatisticsDAO();
        initComponents();
        setupLayout();
        setupEventHandlers();
        // Mặc định hiển thị dữ liệu năm 2025
        dateFromField.setText("2025-01-01");
        dateToField.setText("2025-12-31");
        loadDefaultData();
    }

    /**
     * Load dữ liệu mặc định khi mở panel
     */
    private void loadDefaultData() {
        try {
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 12, 31);
            List<User> users = statisticsDAO.getNewUsers(start, end, "", "Sắp xếp theo thời gian (Mới nhất)");
            displayNewUsers(users);
            updateStatistics();
        } catch (SQLException e) {
            // Ignore errors on initial load
        }
    }

    private void initComponents() {
        String[] columns = { "ID", "Tên đăng nhập", "Họ tên", "Email", "Ngày đăng ký", "Trạng thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(28);
        userTable.setAutoCreateRowSorter(true);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(Color.WHITE);
        userTable.getTableHeader().setForeground(Color.BLACK);

        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(120); // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(150); // Họ tên
        columnModel.getColumn(3).setPreferredWidth(180); // Email
        columnModel.getColumn(4).setPreferredWidth(120); // Ngày đăng ký
        columnModel.getColumn(5).setPreferredWidth(110); // Trạng thái

        // Chọn khoảng thời gian
        dateFromField = new JTextField(10);
        dateFromField.setToolTipText("Định dạng: YYYY-MM-DD");
        dateToField = new JTextField(10);
        dateToField.setToolTipText("Định dạng: YYYY-MM-DD");

        // Yêu cầu b: Lọc theo tên
        searchNameField = new JTextField(20);

        // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
        sortCombo = new JComboBox<>(new String[] {
                "Sắp xếp theo thời gian (Mới nhất)",
                "Sắp xếp theo thời gian (Cũ nhất)",
                "Sắp xếp theo tên (A-Z)",
                "Sắp xếp theo tên (Z-A)"
        });

        filterButton = new JButton("🔍 Tìm kiếm và lọc");
        resetButton = new JButton("↺ Đặt lại");
        refreshButton = new JButton("🔄 Làm mới");
        exportButton = new JButton("📊 Xuất CSV");

        stylePrimaryButton(filterButton);
        stylePrimaryButton(resetButton);
        styleAddUserButtonSimple(refreshButton);
        styleAddUserButtonSimple(exportButton);
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
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("📅 Tùy chọn báo cáo người dùng mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);
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
                new EmptyBorder(10, 10, 10, 10)));

        // Header with statistics
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📋 Danh sách người dùng đăng ký mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        // Khởi tạo instance variable nếu chưa có
        if (this.totalLabel == null) {
            this.totalLabel = new JLabel("📊 Tổng số: 0");
        }
        this.totalLabel.setFont(new Font("Arial", Font.BOLD, 12));

        statsPanel.add(this.totalLabel);

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

        // Kiểm tra đầu vào
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ khoảng thời gian!\n" +
                            "Định dạng: YYYY-MM-DD\n" +
                            "Ví dụ: 2024-01-01",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate định dạng ngày
        if (!isValidDateFormat(fromDate) || !isValidDateFormat(toDate)) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ!\n" +
                            "Vui lòng nhập theo định dạng: YYYY-MM-DD\n" +
                            "Ví dụ: 2024-01-01",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Load dữ liệu từ database
        try {
            LocalDate startDate = LocalDate.parse(fromDate, inputFormatter);
            LocalDate endDate = LocalDate.parse(toDate, inputFormatter);
            String nameFilter = searchNameField.getText().trim();
            String sortOption = (String) sortCombo.getSelectedItem();

            // Get new users by date range
            List<User> newUsers = statisticsDAO.getNewUsers(startDate, endDate, nameFilter, sortOption);

            // Display users
            displayNewUsers(newUsers);
            updateStatistics();

            JOptionPane.showMessageDialog(this,
                    "Đã tải " + newUsers.size() + " người dùng mới\n" +
                            "Từ: " + fromDate + " đến: " + toDate,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (DateTimeParseException e) {
            showError("Lỗi định dạng ngày: " + e.getMessage());
        } catch (SQLException e) {
            showError("Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị danh sách người dùng mới từ Map
     */
    private void displayNewUsersFromMap(List<Map<String, Object>> usersData) {
        tableModel.setRowCount(0); // Clear table

        for (Map<String, Object> userData : usersData) {
            Object[] row = {
                    userData.get("user_id"),
                    userData.get("username"),
                    userData.get("full_name"),
                    userData.get("email"),
                    userData.get("created_at"),
                    "active".equals(userData.get("status")) ? "Hoạt động" : "Bị khóa"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Hiển thị danh sách người dùng mới
     */
    private void displayNewUsers(List<User> users) {
        tableModel.setRowCount(0); // Clear table

        for (User user : users) {
            Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(dateFormatter) : "",
                    "active".equalsIgnoreCase(user.getStatus()) ? "Hoạt động" : "Bị khóa"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Load dữ liệu theo bộ lọc
     */
    private void loadFilteredData(String fromDate, String toDate, String nameFilter, String sortOption) {
        // Deprecated - replaced by handleFilterReport with database integration
        handleFilterReport();

        updateStatistics();
    }

    /**
     * Cập nhật thống kê tổng số
     */
    private void updateStatistics() {
        int totalCount = userTable.getRowCount();
        if (totalLabel != null) {
            totalLabel.setText("📊 Tổng số: " + totalCount);
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
        tableModel.setRowCount(0);
        updateStatistics();

        JOptionPane.showMessageDialog(this,
                "Đã đặt lại tất cả bộ lọc!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
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
     * Xuất CSV
     */
    private void handleExport() {
        if (userTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất!\n" +
                            "Vui lòng tạo báo cáo trước.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("NguoiDungMoi.csv"));

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
                writer.println("ID,Tên đăng nhập,Họ tên,Email,Ngày đăng ký,Trạng thái");

                // Ghi dữ liệu từ table
                for (int i = 0; i < userTable.getRowCount(); i++) {
                    String line = String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            userTable.getValueAt(i, 0), // ID
                            escapeCsv(userTable.getValueAt(i, 1)), // Username
                            escapeCsv(userTable.getValueAt(i, 2)), // Full name
                            escapeCsv(userTable.getValueAt(i, 3)), // Email
                            escapeCsv(userTable.getValueAt(i, 4)), // Created date
                            escapeCsv(userTable.getValueAt(i, 5)) // Status
                    );
                    writer.println(line);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + userTable.getRowCount() + " người dùng vào file:\n" + filePath,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            showError("Lỗi xuất file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escape special characters for CSV
     */
    private String escapeCsv(Object value) {
        if (value == null)
            return "";
        String str = value.toString();
        return str.replace("\"", "\"\"");
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

    private void styleAddUserButtonSimple(JButton button) {
        // Màu xanh ngọc (Teal/Cyan) gần giống trong ảnh: #1ABC9C hoặc #20B2AA
        // (LightSeaGreen)
        Color tealColor = new Color(32, 178, 170); // LightSeaGreen

        button.setBackground(tealColor);
        button.setForeground(Color.WHITE); // Màu chữ trắng

        // Phông chữ và kích thước (dựa trên ảnh, chữ có vẻ lớn và đậm)
        button.setFont(new Font("Arial", Font.BOLD, 14));

        button.setOpaque(true);
        button.setBorderPainted(false); // Bỏ viền
        button.setFocusPainted(false);

        // Căn lề để tạo khoảng đệm (padding) lớn hơn
        button.setMargin(new Insets(10, 20, 10, 20));

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