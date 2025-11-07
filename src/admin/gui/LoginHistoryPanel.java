package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Giao diện xem lịch sử đăng nhập - Phiên bản đơn giản
 * Chỉ hiển thị bảng lịch sử, không có tìm kiếm/lọc
 */
public class LoginHistoryPanel extends JPanel {

    // Định nghĩa các màu chủ đạo
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);

    private JTable historyTable;
    private JButton refreshButton, exportButton;

    public LoginHistoryPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // Bảng hiển thị lịch sử đăng nhập - chỉ 4 cột cơ bản
        String[] columns = {"ID", "Thời gian", "Tên đăng nhập", "Họ tên"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(model);
        historyTable.setRowHeight(28);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setFillsViewportHeight(true);

        // Áp dụng màu sắc cho bảng
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(ZALO_BLUE);
        historyTable.getTableHeader().setForeground(Color.WHITE);

        // Thiết lập độ rộng cột
        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);   // ID
        columnModel.getColumn(1).setPreferredWidth(200);  // Thời gian
        columnModel.getColumn(2).setPreferredWidth(150);  // Tên đăng nhập
        columnModel.getColumn(3).setPreferredWidth(200);  // Họ tên

        // Các nút chức năng
        refreshButton = new JButton("🔄 Làm mới");
        exportButton = new JButton("📊 Xuất Excel");
        
        stylePrimaryButton(refreshButton);
        stylePrimaryButton(exportButton);
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
        loadSampleData();
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
    
    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ
        
        // Dữ liệu mẫu - chỉ 4 cột
        model.addRow(new Object[]{"1", "2024-01-02 14:00:00", "user4", "Phạm Thị D"});
        model.addRow(new Object[]{"2", "2024-01-02 11:00:00", "user3", "Lê Văn C"});
        model.addRow(new Object[]{"3", "2024-01-02 09:15:00", "user1", "Nguyễn Văn A"});
        model.addRow(new Object[]{"4", "2024-01-02 08:30:00", "admin", "Quản trị viên"});
        model.addRow(new Object[]{"5", "2024-01-01 10:00:00", "user2", "Trần Thị B"});
        model.addRow(new Object[]{"6", "2024-01-01 09:00:00", "user1", "Nguyễn Văn A"});
        model.addRow(new Object[]{"7", "2024-01-01 08:00:00", "admin", "Quản trị viên"});
    }
}