// package admin.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện quản lý người dùng - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class UserManagementFrame extends JInternalFrame {
    private JTable userTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JButton addButton, editButton, deleteButton, lockButton, unlockButton, refreshButton;
    
    public UserManagementFrame() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Quản lý người dùng");
        setSize(1000, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Bảng hiển thị danh sách người dùng
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Email", "Địa chỉ", "Ngày sinh", "Giới tính", "Trạng thái", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(model);
        userTable.setRowHeight(25);
        
        // Các trường tìm kiếm và lọc
        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Bị khóa"});
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo ngày tạo"});
        
        // Các nút chức năng
        addButton = new JButton("Thêm người dùng");
        editButton = new JButton("Sửa thông tin");
        deleteButton = new JButton("Xóa người dùng");
        lockButton = new JButton("Khóa tài khoản");
        unlockButton = new JButton("Mở khóa tài khoản");
        refreshButton = new JButton("Làm mới");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel tìm kiếm và lọc
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm theo tên/tên đăng nhập:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(statusFilter);
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        searchPanel.add(new JButton("Lọc"));
        searchPanel.add(new JButton("Tìm kiếm"));
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(lockButton);
        buttonPanel.add(unlockButton);
        buttonPanel.add(refreshButton);
        
        // Bảng dữ liệu
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "admin@chat.com", "Hà Nội", "1990-01-01", "Nam", "Hoạt động", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "user1@email.com", "TP.HCM", "1995-05-15", "Nam", "Hoạt động", "2024-01-02"});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "user2@email.com", "Đà Nẵng", "1998-03-20", "Nữ", "Hoạt động", "2024-01-03"});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "user3@email.com", "Hải Phòng", "1992-12-10", "Nam", "Hoạt động", "2024-01-04"});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", "user4@email.com", "Cần Thơ", "1996-08-25", "Nữ", "Bị khóa", "2024-01-05"});
    }
}
