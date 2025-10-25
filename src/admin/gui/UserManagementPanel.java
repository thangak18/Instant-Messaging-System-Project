import javax.swing.*;
import javax.swing.border.Border; 
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; 
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class UserManagementPanel extends JPanel {
    // --- CẢI TIẾN MÀU SẮC 1: Định nghĩa các màu chủ đạo ---
    private static final Color ZALO_BLUE = new Color(0, 102, 255); // Màu xanh dương
    private static final Color DESTRUCTIVE_RED = new Color(220, 53, 69); // Màu đỏ (Xóa/Khóa)
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125); // Màu xám (Làm mới)
    private JTable userTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortCombo;
    private JButton addButton, editButton, deleteButton, lockButton, unlockButton, refreshButton,
            filterButton, searchButton; 
    private JComboBox<String> activityCompareCombo;
    private JTextField activityCountField;
    public UserManagementPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    private void initializeComponents() {
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
        userTable.setAutoCreateRowSorter(true);
        userTable.setFillsViewportHeight(true);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- CẢI TIẾN MÀU SẮC 2: Thêm màu cho Tiêu đề Bảng (Header) ---
        Color lightBlue = new Color(135, 206, 250);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(lightBlue); // Nền trắng
        userTable.getTableHeader().setForeground(Color.WHITE); // Chữ đen

        // Thiết lập độ rộng cột
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(7).setPreferredWidth(80);

        // Các trường tìm kiếm và lọc
        searchField = new JTextField(10); // Giảm chiều dài ô nhập tên
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Bị khóa"});
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo ngày tạo"});

        // Lọc theo số lượng hoạt động
        activityCompareCombo = new JComboBox<>(new String[]{"=", "<", ">"});
        activityCompareCombo.setPreferredSize(new Dimension(45, 25));
        activityCountField = new JTextField();
        activityCountField.setColumns(5);

        // --- CẢI TIẾN MÀU SẮC 3: Tạo kiểu cho các nút bấm ---
        
        // Nút trong panel tìm kiếm
        filterButton = new JButton("Lọc");
        searchButton = new JButton("Tìm kiếm");
        stylePrimaryButton(filterButton);
        stylePrimaryButton(searchButton);

        // Nút trong panel chức năng
        addButton = new JButton("Thêm người dùng");
        editButton = new JButton("Sửa thông tin");
        deleteButton = new JButton("Xóa người dùng");
        lockButton = new JButton("Khóa tài khoản");
        unlockButton = new JButton("Mở khóa tài khoản");
        refreshButton = new JButton("Làm mới");

        stylePrimaryButton(addButton); // Xanh
        stylePrimaryButton(editButton);
        stylePrimaryButton(deleteButton);
        stylePrimaryButton(lockButton);
        stylePrimaryButton(unlockButton);
        stylePrimaryButton(refreshButton);
        // styleDestructiveButton(deleteButton); // Đỏ
        // styleDestructiveButton(lockButton); // Đỏ
        // styleNeutralButton(refreshButton); // Xám

        // Giữ mặc định cho "Sửa" và "Mở khóa" để tạo sự phân cấp
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel lọc và tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        Border searchBorder = createTitledBorder("Bộ lọc và Tìm kiếm");
        searchPanel.setBorder(searchBorder);

        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);

        searchPanel.add(new JLabel("Tên đăng nhập:"));
        searchPanel.add(searchField);

        searchPanel.add(new JLabel("Số lượng hoạt động:"));
        searchPanel.add(activityCompareCombo);
        searchPanel.add(activityCountField);

        searchPanel.add(filterButton);
        searchPanel.add(searchButton);

        // Panel Bảng
        JPanel centerPanel = new JPanel(new BorderLayout());
        Border centerBorder = createTitledBorder("Danh sách người dùng");
        centerPanel.setBorder(centerBorder);

        JScrollPane scrollPane = new JScrollPane(userTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        Border buttonBorder = createTitledBorder("Chức năng");
        buttonPanel.setBorder(buttonBorder);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(lockButton);
        buttonPanel.add(unlockButton);
        buttonPanel.add(refreshButton);

        // Thêm các panel con vào panel chính
        add(searchPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // --- CÁC PHƯƠNG THỨC HỖ TRỢ TẠO KIỂU (HELPER METHODS) ---

    /**
     * Tạo một TitledBorder (viền có tiêu đề) với màu xanh và in đậm
     */
    private Border createTitledBorder(String title) {
        // Viền rỗng bên trong để tạo padding
        Border emptyInside = new EmptyBorder(5, 5, 5, 5); 
        // Viền tiêu đề
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleColor(Color.BLACK);//chỉnh sửa màu chữ mục tiêu đề nhỏ
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        
        // Kết hợp viền tiêu đề bên ngoài và viền rỗng bên trong
        return BorderFactory.createCompoundBorder(titledBorder, emptyInside);
    }

    /**
     * Áp dụng kiểu "Primary" (xanh, chữ trắng) cho nút
     */
    private void stylePrimaryButton(JButton button) {
        button.setBackground(ZALO_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true); // Quan trọng để hiển thị màu nền trên macOS
        button.setBorderPainted(false); // Tắt viền mặc định
        button.setFocusPainted(false); // Tắt viền khi focus
        button.setMargin(new Insets(5, 12, 5, 12)); // Thêm padding cho nút
    }
    
    // (Phần loadSampleData không thay đổi)
    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "admin@chat.com", "Hà Nội", "1990-01-01", "Nam", "Hoạt động", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "user1@email.com", "TP.HCM", "1995-05-15", "Nam", "Hoạt động", "2024-01-02"});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "user2@email.com", "Đà Nẵng", "1998-03-20", "Nữ", "Hoạt động", "2024-01-03"});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "user3@email.com", "Hải Phòng", "1992-12-10", "Nam", "Hoạt động", "2024-01-04"});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", "user4@email.com", "Cần Thơ", "1996-08-25", "Nữ", "Bị khóa", "2024-01-05"});
    }
}