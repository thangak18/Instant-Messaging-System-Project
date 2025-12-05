package admin.gui;

import admin.service.SpamReportDAO;
import admin.socket.SpamReport;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Giao diện quản lý báo cáo spam - ĐẦY ĐỦ CHỨC NĂNG
 * Yêu cầu: a) Sắp xếp, b) Lọc theo thời gian, c) Lọc theo tên, d) Khóa tài
 * khoản
 */
public class SpamReportPanel extends JPanel {
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);
    private static final Color INFO_CYAN = new Color(23, 162, 184);

    private JTable spamTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JComboBox<String> timeFilterCombo;
    private JComboBox<String> sortCombo;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;

    // Statistics labels
    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel resolvedLabel;
    private JLabel rejectedLabel;

    // Backend
    private SpamReportDAO spamReportDAO;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public SpamReportPanel() {
        this.spamReportDAO = new SpamReportDAO();
        initComponents();
        setupLayout();
        loadSpamReportsFromDatabase();
        setupEventHandlers();
    }

    private void initComponents() {
        // Bảng với cột đầy đủ thông tin
        String[] columns = { "ID", "Người báo cáo", "Người bị báo cáo", "Lý do", "Trạng thái", "Ngày báo cáo" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spamTable = new JTable(tableModel);
        spamTable.setRowHeight(28);
        spamTable.setAutoCreateRowSorter(true);
        spamTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        spamTable.getTableHeader().setBackground(Color.WHITE);
        spamTable.getTableHeader().setForeground(Color.BLACK);

        // Điều chỉnh độ rộng cột
        TableColumnModel columnModel = spamTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(130); // Người báo cáo
        columnModel.getColumn(2).setPreferredWidth(150); // Người bị báo cáo
        columnModel.getColumn(3).setPreferredWidth(200); // Lý do
        columnModel.getColumn(4).setPreferredWidth(100); // Trạng thái
        columnModel.getColumn(5).setPreferredWidth(120); // Ngày báo cáo

        // Yêu cầu c: Lọc theo tên đăng nhập
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[] {
                "Tìm người bị báo cáo",
                "Tìm người báo cáo"
        });

        // Yêu cầu b: Lọc theo thời gian
        timeFilterCombo = new JComboBox<>(new String[] {
                "Tất cả thời gian",
                "Hôm nay",
                "7 ngày qua",
                "30 ngày qua",
                "Tháng này"
        });

        statusFilter = new JComboBox<>(new String[] {
                "Tất cả trạng thái",
                "Chờ xử lý",
                "Đã xử lý",
                "Từ chối"
        });

        // Yêu cầu a: Sắp xếp theo thời gian/tên đăng nhập
        sortCombo = new JComboBox<>(new String[] {
                "Sắp xếp theo thời gian (Mới nhất)",
                "Sắp xếp theo thời gian (Cũ nhất)",
                "Sắp xếp theo người bị báo cáo (A-Z)",
                "Sắp xếp theo người bị báo cáo (Z-A)",
                "Sắp xếp theo người báo cáo (A-Z)",
                "Sắp xếp theo người báo cáo (Z-A)"
        });
    }

    /**
     * Load báo cáo spam từ database
     */
    private void loadSpamReportsFromDatabase() {
        try {
            List<SpamReport> reports = spamReportDAO.getAllSpamReports();
            displaySpamReports(reports);
            updateStatistics(reports);
        } catch (SQLException e) {
            showError("Lỗi load dữ liệu báo cáo spam: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị danh sách báo cáo spam lên table
     */
    private void displaySpamReports(List<SpamReport> reports) {
        tableModel.setRowCount(0); // Clear table

        for (SpamReport report : reports) {
            Object[] row = {
                    report.getId(),
                    report.getReporterName(),
                    report.getReportedUserName(),
                    report.getReason(),
                    report.getStatus(),
                    report.getCreatedAt() != null ? report.getCreatedAt().format(dateTimeFormatter) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Search and Filter panel (Yêu cầu a, b, c)
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Button panel (Yêu cầu d)
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

        JLabel titleLabel = new JLabel("🔍 Tìm kiếm & Lọc báo cáo spam");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Row 1: Tìm kiếm (Yêu cầu c)
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchRow.add(new JLabel("Loại tìm kiếm:"));
        searchTypeCombo.setPreferredSize(new Dimension(170, 30));
        searchRow.add(searchTypeCombo);

        searchRow.add(new JLabel("Từ khóa:"));
        searchField.setPreferredSize(new Dimension(200, 30));
        searchRow.add(searchField);

        panel.add(searchRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 2: Lọc (Yêu cầu b)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterRow.add(new JLabel("Thời gian:"));
        timeFilterCombo.setPreferredSize(new Dimension(140, 30));
        filterRow.add(timeFilterCombo);

        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Trạng thái:"));
        statusFilter.setPreferredSize(new Dimension(130, 30));
        filterRow.add(statusFilter);

        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Sắp xếp:"));
        sortCombo.setPreferredSize(new Dimension(240, 30));
        filterRow.add(sortCombo);

        panel.add(filterRow);
        panel.add(Box.createVerticalStrut(5));

        // Row 3: Action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton searchFilterBtn = createStyledButton("Tìm kiếm + Lọc", ZALO_BLUE);
        actionRow.add(searchFilterBtn);

        JButton resetBtn = createStyledButton("↺ Đặt lại", ZALO_BLUE);
        actionRow.add(resetBtn);

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

        JLabel titleLabel = new JLabel("🔔 Danh sách báo cáo spam");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        // Khởi tạo instance variables nếu chưa có
        if (this.pendingLabel == null) {
            this.pendingLabel = new JLabel("⏳ Pending: 0");
        }
        this.pendingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        this.pendingLabel.setForeground(WARNING_ORANGE);

        if (this.resolvedLabel == null) {
            this.resolvedLabel = new JLabel("✅ Resolved: 0");
        }
        this.resolvedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        this.resolvedLabel.setForeground(SUCCESS_GREEN);

        if (this.rejectedLabel == null) {
            this.rejectedLabel = new JLabel("❌ Rejected: 0");
        }
        this.rejectedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        this.rejectedLabel.setForeground(DANGER_RED);

        if (this.totalLabel == null) {
            this.totalLabel = new JLabel("📊 Tổng: 0");
        }
        this.totalLabel.setFont(new Font("Arial", Font.BOLD, 12));

        statsPanel.add(this.pendingLabel);
        statsPanel.add(this.resolvedLabel);
        statsPanel.add(this.rejectedLabel);
        statsPanel.add(this.totalLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(spamTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);

        JButton processBtn = createStyledButton("✅ Xử lý báo cáo", INFO_CYAN);

        // Yêu cầu d: Khóa tài khoản người dùng
        JButton lockAccountBtn = createStyledButton("🔒 Khóa tài khoản", INFO_CYAN);
        JButton exportBtn = createStyledButton("📊 Xuất CSV", INFO_CYAN);

        panel.add(processBtn);
        panel.add(lockAccountBtn);
        panel.add(exportBtn);

        return panel;
    }

    private void setupEventHandlers() {
        // Nút duy nhất: Tìm kiếm + Lọc
        addActionToButton("Tìm kiếm + Lọc", e -> handleSearchAndFilter());

        // Đặt lại
        addActionToButton("↺ Đặt lại", e -> handleReset());

        // Xử lý báo cáo
        addActionToButton("✅ Xử lý báo cáo", e -> processReport());

        // Yêu cầu d: Khóa tài khoản
        addActionToButton("🔒 Khóa tài khoản", e -> lockUserAccount());

        // Xuất CSV
        addActionToButton("📊 Xuất CSV", e -> handleExportCSV());
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Xử lý tìm kiếm và lọc kết hợp
     * - Nếu có từ khóa: tìm kiếm theo từ khóa
     * - Nếu không có từ khóa: lấy tất cả (áp dụng filter)
     * - Áp dụng filter thời gian, trạng thái, sắp xếp
     */
    private void handleSearchAndFilter() {
        try {
            String keyword = searchField.getText().trim();
            String searchTypeSelected = (String) searchTypeCombo.getSelectedItem();
            String searchType = "Tìm người báo cáo".equals(searchTypeSelected) ? "reporter" : "reported";
            String timeFilter = (String) timeFilterCombo.getSelectedItem();
            String status = (String) statusFilter.getSelectedItem();
            String sortOption = (String) sortCombo.getSelectedItem();

            // Lấy dữ liệu (có hoặc không có keyword)
            List<SpamReport> reports = spamReportDAO.searchSpamReports(
                    searchType, keyword.isEmpty() ? null : keyword, timeFilter, status, sortOption);
            displaySpamReports(reports);
            updateStatistics(reports);

            // Thông báo kết quả
            String message = !keyword.isEmpty()
                    ? "Tìm thấy " + reports.size() + " kết quả"
                    : "Đã lọc " + reports.size() + " báo cáo";
            JOptionPane.showMessageDialog(this, message,
                    "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void handleReset() {
        searchField.setText("");
        searchTypeCombo.setSelectedIndex(0);
        timeFilterCombo.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        sortCombo.setSelectedIndex(0);
        loadSpamReportsFromDatabase();

        JOptionPane.showMessageDialog(this,
                "Đã đặt lại tất cả bộ lọc!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatistics(List<SpamReport> reports) {
        int total = reports.size();
        int pending = 0;
        int resolved = 0;
        int rejected = 0;

        for (SpamReport report : reports) {
            String status = report.getStatus();
            if ("pending".equalsIgnoreCase(status)) {
                pending++;
            } else if ("resolved".equalsIgnoreCase(status)) {
                resolved++;
            } else if ("rejected".equalsIgnoreCase(status)) {
                rejected++;
            }
        }

        // Cập nhật labels
        if (pendingLabel != null) {
            pendingLabel.setText("⏳ Pending: " + pending);
        }
        if (resolvedLabel != null) {
            resolvedLabel.setText("✅ Resolved: " + resolved);
        }
        if (rejectedLabel != null) {
            rejectedLabel.setText("❌ Rejected: " + rejected);
        }
        if (totalLabel != null) {
            totalLabel.setText("📊 Tổng: " + total);
        }
    }

    private void processReport() {
        int selectedRow = spamTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn báo cáo cần xử lý!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reported = spamTable.getValueAt(selectedRow, 2).toString();

        int confirm = showStyledConfirmDialog(this,
                "Xác nhận xử lý báo cáo spam cho người dùng: " + reported + "?",
                "Xác nhận xử lý");

        if (confirm == JOptionPane.YES_OPTION) {
            spamTable.setValueAt("Đã xử lý", selectedRow, 4);
            JOptionPane.showMessageDialog(this,
                    "Đã xử lý báo cáo thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Yêu cầu d: Khóa tài khoản người dùng bị báo cáo spam
    private void lockUserAccount() {
        int selectedRow = spamTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn báo cáo!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportedUser = spamTable.getValueAt(selectedRow, 2).toString();
        String reason = spamTable.getValueAt(selectedRow, 3).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Khóa tài khoản - " + reportedUser, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Thông tin
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 10));
        infoPanel.setOpaque(false);

        JLabel userLabel = new JLabel("👤 Người dùng: " + reportedUser);
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel reasonLabel = new JLabel("📝 Lý do báo cáo: " + reason);
        reasonLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel warningLabel = new JLabel("⚠️ Cảnh báo: Hành động này sẽ khóa tài khoản người dùng!");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setForeground(DANGER_RED);

        infoPanel.add(userLabel);
        infoPanel.add(reasonLabel);
        infoPanel.add(warningLabel);

        // Ghi chú
        JPanel notePanel = new JPanel(new BorderLayout(5, 5));
        notePanel.setOpaque(false);

        JLabel noteLabel = new JLabel("Ghi chú lý do khóa:");
        noteLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JTextArea noteArea = new JTextArea(3, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        notePanel.add(noteLabel, BorderLayout.NORTH);
        notePanel.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        contentPanel.add(infoPanel, BorderLayout.NORTH);
        contentPanel.add(notePanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton lockBtn = createStyledButton("🔒 Khóa tài khoản", DANGER_RED);
        JButton cancelBtn = createStyledButton("❌ Hủy", NEUTRAL_GRAY);

        lockBtn.addActionListener(e -> {
            String note = noteArea.getText().trim();
            if (note.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng nhập ghi chú lý do khóa!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = showStyledConfirmDialog(dialog,
                    "Bạn có chắc chắn muốn khóa tài khoản " + reportedUser + "?",
                    "Xác nhận khóa tài khoản");

            if (confirm == JOptionPane.YES_OPTION) {
                // TODO: Cập nhật database - khóa tài khoản và cập nhật trạng thái báo cáo
                spamTable.setValueAt("Đã xử lý", selectedRow, 4);
                JOptionPane.showMessageDialog(dialog,
                        "Đã khóa tài khoản " + reportedUser + " thành công!\n" +
                                "Ghi chú: " + note,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(lockBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false); // Loại bỏ viền để màu sắc hiển thị đúng
        button.setContentAreaFilled(true); // Đảm bảo vùng nội dung được tô màu
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

    /**
     * Xuất danh sách báo cáo spam ra file CSV
     */
    private void handleExportCSV() {
        try {
            // Lấy dữ liệu từ database
            List<SpamReport> reports = spamReportDAO.getAllSpamReports();
            if (reports.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có dữ liệu để xuất!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file CSV");
            fileChooser.setSelectedFile(new java.io.File("BaoCaoSpam.csv"));

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
                writer.println("ID,Người báo cáo,Người bị báo cáo,Lý do,Trạng thái,Ngày báo cáo");

                // Ghi dữ liệu
                for (SpamReport report : reports) {
                    String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            report.getId(),
                            escapeCsv(report.getReporterName()),
                            escapeCsv(report.getReportedUserName()),
                            escapeCsv(report.getReason()),
                            escapeCsv(report.getStatus()),
                            report.getCreatedAt() != null ? report.getCreatedAt().format(dateTimeFormatter) : "");
                    writer.println(line);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + reports.size() + " báo cáo vào file:\n" + filePath,
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

    /**
     * Show confirm dialog with red cancel button
     */
    private int showStyledConfirmDialog(Component parent, Object message, String title) {
        final JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        if (message instanceof Component) {
            messagePanel.add((Component) message, BorderLayout.CENTER);
        } else {
            JLabel messageLabel = new JLabel("<html>" + message.toString() + "</html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            messagePanel.add(messageLabel, BorderLayout.CENTER);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        final int[] result = { JOptionPane.CLOSED_OPTION };

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
}