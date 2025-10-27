package user.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện danh sách bạn bè - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class FriendsListFrame extends JInternalFrame {
    private JTable friendsTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton refreshButton, chatButton, unfriendButton, blockButton;
    
    public FriendsListFrame() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Danh sách bạn bè");
        setSize(800, 500);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Bảng hiển thị danh sách bạn bè
        String[] columns = {"Tên đăng nhập", "Họ tên", "Trạng thái", "Số bạn bè", "Ngày kết bạn"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        friendsTable = new JTable(model);
        friendsTable.setRowHeight(25);
        
        // Các trường tìm kiếm và lọc
        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Online", "Offline"});
        
        // Các nút chức năng
        refreshButton = new JButton("Làm mới");
        chatButton = new JButton("Chat");
        unfriendButton = new JButton("Hủy kết bạn");
        blockButton = new JButton("Block");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel tìm kiếm và lọc
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm theo tên:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(statusFilter);
        searchPanel.add(new JButton("Tìm kiếm"));
        searchPanel.add(new JButton("Lọc"));
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(chatButton);
        buttonPanel.add(unfriendButton);
        buttonPanel.add(blockButton);
        
        // Bảng dữ liệu
        JScrollPane scrollPane = new JScrollPane(friendsTable);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        DefaultTableModel model = (DefaultTableModel) friendsTable.getModel();
        model.addRow(new Object[]{"user1", "Nguyễn Văn A", "Online", "15", "2024-01-01"});
        model.addRow(new Object[]{"user2", "Trần Thị B", "Offline", "8", "2024-01-02"});
        model.addRow(new Object[]{"user3", "Lê Văn C", "Online", "12", "2024-01-03"});
        model.addRow(new Object[]{"user4", "Phạm Thị D", "Offline", "6", "2024-01-04"});
        model.addRow(new Object[]{"user5", "Hoàng Văn E", "Online", "20", "2024-01-05"});
    }
}
