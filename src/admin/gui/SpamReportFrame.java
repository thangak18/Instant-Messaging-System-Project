// package admin.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện báo cáo spam - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class SpamReportFrame extends JInternalFrame {
    private JTable reportTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JButton refreshButton, lockUserButton, resolveButton;
    
    public SpamReportFrame() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Báo cáo spam");
        setSize(1000, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Bảng hiển thị báo cáo spam
        String[] columns = {"ID", "Thời gian", "Người báo cáo", "Người bị báo cáo", "Lý do", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(model);
        reportTable.setRowHeight(25);
        
        // Các trường tìm kiếm và lọc
        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Chờ xử lý", "Đã xử lý", "Đã bỏ qua"});
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo thời gian", "Sắp xếp theo tên đăng nhập"});
        
        // Các nút chức năng
        refreshButton = new JButton("Làm mới");
        lockUserButton = new JButton("Khóa tài khoản");
        resolveButton = new JButton("Xử lý báo cáo");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel tìm kiếm và lọc
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm theo tên đăng nhập:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(statusFilter);
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        searchPanel.add(new JButton("Tìm kiếm"));
        searchPanel.add(new JButton("Lọc"));
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(lockUserButton);
        buttonPanel.add(resolveButton);
        
        // Bảng dữ liệu
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        model.addRow(new Object[]{"1", "2024-01-01 10:00:00", "user1", "user5", "Gửi tin nhắn spam", "Chờ xử lý"});
        model.addRow(new Object[]{"2", "2024-01-01 11:00:00", "user2", "user5", "Quấy rối", "Đã xử lý"});
        model.addRow(new Object[]{"3", "2024-01-01 12:00:00", "user3", "user5", "Nội dung không phù hợp", "Đã bỏ qua"});
        model.addRow(new Object[]{"4", "2024-01-02 09:00:00", "user4", "user6", "Gửi tin nhắn spam", "Chờ xử lý"});
        model.addRow(new Object[]{"5", "2024-01-02 10:00:00", "user1", "user7", "Quấy rối", "Chờ xử lý"});
    }
}
