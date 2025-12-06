package admin.gui;

import admin.service.StatisticsDAO;
import admin.socket.UserActivity;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Giao diện Báo cáo Người dùng hoạt động - ĐẦY ĐỦ CHỨC NĂNG
 * Yêu cầu: Chọn khoảng thời gian, a) Sắp xếp, b) Lọc theo tên, c) Lọc theo số
 * lượng hoạt động
 */
public class ActiveUserReportPanel extends JPanel {

    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);
    private static final Color INFO_CYAN = new Color(23, 162, 184);

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextField dateFromField;
    private JTextField dateToField;
    private JTextField searchNameField;
    private JComboBox<String> sortCombo;
    private JComboBox<String> comparisonCombo;
    private JTextField activityCountField;
    private JButton filterButton, resetButton, refreshButton, exportButton;
    private JLabel totalLabel;

    // Backend
    private StatisticsDAO statisticsDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ActiveUserReportPanel() {
        this.statisticsDAO = new StatisticsDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        // Mặc định hiển thị tất cả dữ liệu (tất cả các năm)
        // Để trống date fields để load tất cả users
        dateFromField.setText("");
        dateToField.setText("");
        loadDefaultData();
    }

    /**
     * Load dữ liệu mặc định khi mở panel
     * Load tất cả users từ tất cả các năm
     */
    private void loadDefaultData() {
        try {
            // Lấy tất cả users (không giới hạn năm) - truyền null để load tất cả
            List<UserActivity> activities = statisticsDAO.getUserActivitiesComprehensive(null, null, null, null, null,
                    null);
            displayActiveUsers(activities);
            updateStatistics();
        } catch (SQLException e) {
            // Ignore errors on initial load
        }
    }

    private void initializeComponents() {
        // Bảng hiển thị báo cáo
        String[] columns = { "ID", "Tên đăng nhập", "Họ tên", "Mở ứng dụng",
                "Chat với người", "Chat nhóm", "Ngày tạo" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(28);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setAutoCreateRowSorter(true);

        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(Color.WHITE);
        reportTable.getTableHeader().setForeground(Color.BLACK);

        // Chỉnh độ rộng cột
        TableColumnModel columnModel = reportTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(120); // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(150); // Họ tên
        columnModel.getColumn(3).setPreferredWidth(130); // Mở ứng dụng
        columnModel.getColumn(4).setPreferredWidth(130); // Chat với người
        columnModel.getColumn(5).setPreferredWidth(130); // Chat nhóm
        columnModel.getColumn(6).setPreferredWidth(120); // Ngày tạo

        // Chọn khoảng thời gian
        dateFromField = new JTextField(10);
        dateFromField.setToolTipText("Định dạng: YYYY-MM-DD");
        dateToField = new JTextField(10);
        dateToField.setToolTipText("Định dạng: YYYY-MM-DD");

        // Yêu cầu b: Lọc theo tên
        searchNameField = new JTextField(20);

        // Yêu cầu a: Sắp xếp theo tên/thời gian tạo
        sortCombo = new JComboBox<>(new String[] {
                "Sắp xếp theo tên (A-Z)",
                "Sắp xếp theo tên (Z-A)",
                "Sắp xếp theo thời gian tạo (Mới nhất)",
                "Sắp xếp theo thời gian tạo (Cũ nhất)"
        });

        // Yêu cầu c: Lọc theo số lượng hoạt động (=, >, <)
        comparisonCombo = new JComboBox<>(new String[] { "Tất cả", "=", ">", "<" });
        activityCountField = new JTextField(5);

        filterButton = createButtonWithIcon("Tìm kiếm và lọc", "search");
        resetButton = createButtonWithIcon("Đặt lại", "reset");
        refreshButton = createButtonWithIcon("Làm mới", "refresh");
        exportButton = createButtonWithIcon("Xuất CSV", "export");

        stylePrimaryButton(filterButton);
        stylePrimaryButton(resetButton);
        styleAddUserButtonSimple(refreshButton);
        styleAddUserButtonSimple(exportButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Panel tùy chọn
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

        ImageIcon calendarIcon = loadIcon("calendar", 20, 20);
        JLabel titleLabel = new JLabel("Tùy chọn báo cáo người dùng hoạt động");
        if (calendarIcon != null) {
            titleLabel.setIcon(calendarIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
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

        // Row 2: Lọc theo tên (Yêu cầu b)
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchRow.add(new JLabel("Lọc theo tên:"));
        searchNameField.setPreferredSize(new Dimension(200, 30));
        searchRow.add(searchNameField);

        panel.add(searchRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 3: Lọc theo số lượng hoạt động (Yêu cầu c) và Sắp xếp (Yêu cầu a)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterRow.add(new JLabel("Số lượng hoạt động:"));
        comparisonCombo.setPreferredSize(new Dimension(80, 30));
        filterRow.add(comparisonCombo);

        activityCountField.setPreferredSize(new Dimension(80, 30));
        filterRow.add(activityCountField);

        filterRow.add(Box.createHorizontalStrut(20));
        filterRow.add(new JLabel("Sắp xếp:"));
        sortCombo.setPreferredSize(new Dimension(260, 30));
        filterRow.add(sortCombo);

        panel.add(filterRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 4: Nút hành động
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

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        ImageIcon activityIcon = loadIcon("activity", 20, 20);
        JLabel titleLabel = new JLabel("Danh sách người dùng hoạt động");
        if (activityIcon != null) {
            titleLabel.setIcon(activityIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        // Khởi tạo instance variable nếu chưa có
        if (this.totalLabel == null) {
            this.totalLabel = new JLabel("Tổng số: 0");
        }
        this.totalLabel.setFont(new Font("Arial", Font.BOLD, 12));

        statsPanel.add(this.totalLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

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
     * Xử lý hiển thị báo cáo theo khoảng thời gian và các bộ lọc
     */
    private void handleFilterReport() {
        String fromDate = dateFromField.getText().trim();
        String toDate = dateToField.getText().trim();

        // Nếu cả 2 date fields đều trống, load tất cả users
        boolean loadAll = fromDate.isEmpty() && toDate.isEmpty();
        
        if (!loadAll) {
            // Kiểm tra đầu vào - nếu có 1 trong 2 thì phải có cả 2
            if (fromDate.isEmpty() || toDate.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ khoảng thời gian!\n" +
                                "Định dạng: YYYY-MM-DD\n" +
                                "Ví dụ: 2024-01-01\n\n" +
                                "Hoặc để trống cả 2 để xem tất cả",
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
        }

        // Validate số lượng hoạt động nếu có
        String comparison = (String) comparisonCombo.getSelectedItem();
        String activityCountText = activityCountField.getText().trim();
        Integer activityCount = null;

        if (!"Tất cả".equals(comparison)) {
            if (activityCountText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập số lượng hoạt động để so sánh!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                activityCount = Integer.parseInt(activityCountText);
                if (activityCount < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Số lượng hoạt động phải >= 0!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Số lượng hoạt động không hợp lệ!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Load dữ liệu từ database
        try {
            LocalDate startDate = null;
            LocalDate endDate = null;
            
            if (!loadAll) {
                startDate = LocalDate.parse(fromDate, inputFormatter);
                endDate = LocalDate.parse(toDate, inputFormatter);
            }
            
            String nameFilter = searchNameField.getText().trim();
            String sortOption = (String) sortCombo.getSelectedItem();

            // Get comprehensive user activities
            List<UserActivity> activities = statisticsDAO.getUserActivitiesComprehensive(
                    startDate, endDate,
                    nameFilter.isEmpty() ? null : nameFilter,
                    comparison, activityCount, sortOption);

            displayActiveUsers(activities);
            updateStatistics();

            String message;
            if (loadAll) {
                message = "Tìm thấy " + activities.size() + " người dùng (tất cả các năm)";
            } else {
                message = "Tìm thấy " + activities.size() + " người dùng\n" +
                        "Từ: " + fromDate + " đến: " + toDate;
            }
            JOptionPane.showMessageDialog(this, message,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (DateTimeParseException e) {
            showError("Lỗi định dạng ngày: " + e.getMessage());
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            String detailedMsg = "Lỗi load dữ liệu người dùng hoạt động: " + errorMsg;
            
            if (errorMsg != null && (errorMsg.contains("connection") || 
                                     errorMsg.contains("Connection"))) {
                detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                              "- Kết nối database\n" +
                              "- Khoảng thời gian đã chọn\n" +
                              "- File config.properties\n" +
                              "Hoặc liên hệ admin để được hỗ trợ.";
            }
            
            showError(detailedMsg);
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị danh sách hoạt động người dùng
     */
    private void displayActiveUsers(List<UserActivity> activities) {
        tableModel.setRowCount(0); // Clear table

        for (UserActivity activity : activities) {
            Object[] row = {
                    activity.getUserId(),
                    activity.getUsername(),
                    activity.getFullName(),
                    activity.getLoginCount(),
                    activity.getPrivateChatCount(),
                    activity.getGroupChatCount(),
                    activity.getCreatedAt() != null ? dateFormatter.format(activity.getCreatedAt()) : ""
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Load dữ liệu theo bộ lọc - deprecated
     */
    private void loadFilteredData(String fromDate, String toDate, String nameFilter,
            String activityType, String comparison,
            String activityCount, String sortOption) {
        // Deprecated - now using handleFilterReport with database
        handleFilterReport();
    }

    /**
     * Cập nhật thống kê
     */
    private void updateStatistics() {
        int totalCount = reportTable.getRowCount();
        if (totalLabel != null) {
            totalLabel.setText("📈 Tổng số: " + totalCount);
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
     * Đặt lại bộ lọc
     */
    private void handleReset() {
        dateFromField.setText("");
        dateToField.setText("");
        searchNameField.setText("");
        comparisonCombo.setSelectedIndex(0);
        activityCountField.setText("");
        sortCombo.setSelectedIndex(0);

        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
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
            handleFilterReport();
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
        if (reportTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("NguoiDungHoatDong.csv"));

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
                writer.println("ID,Tên đăng nhập,Họ tên,Mở ứng dụng,Chat với người,Chat nhóm,Ngày tạo");

                // Ghi dữ liệu từ table
                for (int i = 0; i < reportTable.getRowCount(); i++) {
                    String line = String.format("%s,\"%s\",\"%s\",%s,%s,%s,\"%s\"",
                            reportTable.getValueAt(i, 0), // ID
                            escapeCsv(reportTable.getValueAt(i, 1)), // Username
                            escapeCsv(reportTable.getValueAt(i, 2)), // Full name
                            reportTable.getValueAt(i, 3), // Login count
                            reportTable.getValueAt(i, 4), // Private chat count
                            reportTable.getValueAt(i, 5), // Group chat count
                            escapeCsv(reportTable.getValueAt(i, 6)) // Created date
                    );
                    writer.println(line);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + reportTable.getRowCount() + " người dùng vào file:\n" + filePath,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi xuất file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    private JButton createButtonWithIcon(String text, String iconName) {
        JButton button = new JButton(text);
        ImageIcon icon = loadIcon(iconName, 16, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setHorizontalTextPosition(JButton.RIGHT);
            button.setIconTextGap(8);
        }
        button.setPreferredSize(new java.awt.Dimension(200, 35));
        return button;
    }

}