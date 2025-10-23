// package admin.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện xem lịch sử đăng nhập - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class LoginHistoryFrame extends JInternalFrame {
    private JTable historyTable;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JButton refreshButton, exportButton;
    
    public LoginHistoryFrame() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Lịch sử đăng nhập");
        setSize(1000, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Bảng hiển thị lịch sử đăng nhập
        String[] columns = {"ID", "Thời gian", "Tên đăng nhập", "Họ tên", "IP Address", "User Agent"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(model);
        historyTable.setRowHeight(25);
        
        // Các trường tìm kiếm và sắp xếp
        searchField = new JTextField(20);
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo thời gian (mới nhất)", "Sắp xếp theo tên đăng nhập"});
        
        // Các nút chức năng
        refreshButton = new JButton("Làm mới");
        exportButton = new JButton("Xuất Excel");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel tìm kiếm và sắp xếp
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm theo tên đăng nhập:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        searchPanel.add(new JButton("Tìm kiếm"));
        searchPanel.add(new JButton("Lọc"));
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        
        // Bảng dữ liệu
        JScrollPane scrollPane = new JScrollPane(historyTable);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.addRow(new Object[]{"1", "2024-01-01 08:00:00", "admin", "Quản trị viên", "192.168.1.1", "Mozilla/5.0"});
        model.addRow(new Object[]{"2", "2024-01-01 09:00:00", "user1", "Nguyễn Văn A", "192.168.1.2", "Chrome/120.0"});
        model.addRow(new Object[]{"3", "2024-01-01 10:00:00", "user2", "Trần Thị B", "192.168.1.3", "Firefox/121.0"});
        model.addRow(new Object[]{"4", "2024-01-02 08:30:00", "admin", "Quản trị viên", "192.168.1.1", "Mozilla/5.0"});
        model.addRow(new Object[]{"5", "2024-01-02 09:15:00", "user1", "Nguyễn Văn A", "192.168.1.2", "Chrome/120.0"});
        model.addRow(new Object[]{"6", "2024-01-02 11:00:00", "user3", "Lê Văn C", "192.168.1.4", "Safari/17.0"});
        model.addRow(new Object[]{"7", "2024-01-02 14:00:00", "user4", "Phạm Thị D", "192.168.1.5", "Edge/120.0"});
    }
}
