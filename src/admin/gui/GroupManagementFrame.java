// package admin.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện quản lý nhóm chat - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class GroupManagementFrame extends JInternalFrame {
    private JTable groupTable;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JButton refreshButton, viewMembersButton, viewAdminsButton;
    
    public GroupManagementFrame() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Quản lý nhóm chat");
        setSize(1000, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Bảng hiển thị danh sách nhóm
        String[] columns = {"ID", "Tên nhóm", "Mô tả", "Người tạo", "Số thành viên", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        groupTable = new JTable(model);
        groupTable.setRowHeight(25);
        
        // Các trường tìm kiếm và sắp xếp
        searchField = new JTextField(20);
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo thời gian tạo"});
        
        // Các nút chức năng
        refreshButton = new JButton("Làm mới");
        viewMembersButton = new JButton("Xem thành viên");
        viewAdminsButton = new JButton("Xem admin");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel tìm kiếm và sắp xếp
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm theo tên nhóm:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        searchPanel.add(new JButton("Tìm kiếm"));
        searchPanel.add(new JButton("Lọc"));
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewMembersButton);
        buttonPanel.add(viewAdminsButton);
        
        // Bảng dữ liệu
        JScrollPane scrollPane = new JScrollPane(groupTable);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        DefaultTableModel model = (DefaultTableModel) groupTable.getModel();
        model.addRow(new Object[]{"1", "Nhóm bạn thân", "Nhóm chat của những người bạn thân", "user1", "3", "2024-01-01"});
        model.addRow(new Object[]{"2", "Công việc", "Nhóm chat về công việc", "user2", "3", "2024-01-02"});
        model.addRow(new Object[]{"3", "Học tập", "Nhóm chat về học tập", "user3", "3", "2024-01-03"});
        model.addRow(new Object[]{"4", "Gia đình", "Nhóm chat gia đình", "user4", "2", "2024-01-04"});
        model.addRow(new Object[]{"5", "Bạn bè cũ", "Nhóm chat bạn bè cũ", "user1", "4", "2024-01-05"});
    }
}
