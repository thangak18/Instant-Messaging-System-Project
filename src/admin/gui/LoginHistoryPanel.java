package admin.gui;

import admin.service.LoginHistoryDAO;
import admin.socket.LoginHistory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giao diện xem lịch sử đăng nhập - Backend Integration
 */
public class LoginHistoryPanel extends JPanel {

    // Định nghĩa các màu chủ đạo
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton, exportButton;
    private JLabel totalLabel;
    
    // Backend DAO
    private LoginHistoryDAO loginHistoryDAO;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public LoginHistoryPanel() {
        try {
            this.loginHistoryDAO = new LoginHistoryDAO();
            initializeComponents();
            setupLayout();
            loadLoginHistoryFromDatabase();
            setupEventHandlers();
        } catch (Exception e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Bảng hiển thị lịch sử đăng nhập
        String[] columns = {"ID", "Thời gian", "Tên đăng nhập", "Họ tên"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(28);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setFillsViewportHeight(true);

        // Áp dụng màu sắc cho bảng
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(Color.WHITE);
        historyTable.getTableHeader().setForeground(Color.BLACK);

        // Thiết lập độ rộng cột
        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);   // ID
        columnModel.getColumn(1).setPreferredWidth(180);  // Thời gian
        columnModel.getColumn(2).setPreferredWidth(150);  // Tên đăng nhập
        columnModel.getColumn(3).setPreferredWidth(200);  // Họ tên
        
        // Các nút chức năng
        refreshButton = new JButton("🔄 Làm mới");
        exportButton = new JButton("📊 Xuất Excel");
        
        stylePrimaryButton(refreshButton);
        stylePrimaryButton(exportButton);
        
        // Label thống kê
        totalLabel = new JLabel("📊 Tổng số lượt: 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    /**
     * Load lịch sử đăng nhập từ database
     */
    private void loadLoginHistoryFromDatabase() {
        try {
            List<LoginHistory> histories = loginHistoryDAO.getAllLoginHistory();
            displayLoginHistories(histories);
            totalLabel.setText("📊 Tổng số lượt: " + histories.size());
        } catch (SQLException e) {
            showError("Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị danh sách lịch sử lên table
     */
    private void displayLoginHistories(List<LoginHistory> histories) {
        tableModel.setRowCount(0); // Clear table
        
        for (LoginHistory history : histories) {
            Object[] row = {
                history.getId(),
                history.getLoginTime() != null ? history.getLoginTime().format(dateTimeFormatter) : "",
                history.getUsername(),
                history.getFullName(),
                history.getIpAddress() != null ? history.getIpAddress() : "N/A"
            };
            tableModel.addRow(row);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Panel Bảng với thống kê
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel nút chức năng
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Tiêu đề bảng với thống kê
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel tableTitle = new JLabel("📋 Lịch sử đăng nhập");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 16));
        tableTitle.setForeground(ZALO_BLUE);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("📊 Tổng số lượt: 7");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        statsPanel.add(totalLabel);
        
        headerPanel.add(tableTitle, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

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
        // Xử lý làm mới
        refreshButton.addActionListener(e -> handleRefresh());
        
        // Xử lý xuất Excel
        exportButton.addActionListener(e -> handleExport());
    }

    private void handleRefresh() {
        loadLoginHistoryFromDatabase();
        JOptionPane.showMessageDialog(this, 
            "Đã làm mới dữ liệu!",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExport() {
        JOptionPane.showMessageDialog(this, 
            "Chức năng xuất Excel sẽ được triển khai!",
            "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
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
    
    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}