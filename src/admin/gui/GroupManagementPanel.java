package admin.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện quản lý nhóm chat - PHIÊN BẢN 1
 * Sử dụng JSplitPane để chia giao diện Master-Detail
 */
public class GroupManagementPanel extends JPanel {

    // Định nghĩa màu
    private static final Color ZALO_BLUE = new Color(0, 102, 255);

    // -- Các components chính --
    private JTable groupTable; // Bảng danh sách nhóm (Bên trái)
    private JList<String> memberList; // Danh sách thành viên (Bên phải)
    private JList<String> adminList;  // Danh sách admin (Bên phải)
    
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JButton searchButton;
    
    // Model cho JList để cập nhật động
    private DefaultListModel<String> memberListModel;
    private DefaultListModel<String> adminListModel;

    public GroupManagementPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers(); // Thêm hàm xử lý sự kiện
    }

    private void initializeComponents() {
        // --- Phần Lọc và Sắp xếp (ở trên cùng) ---
        searchField = new JTextField(20);
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo thời gian tạo"});
        searchButton = new JButton("Tìm kiếm");
        stylePrimaryButton(searchButton);
        
        // --- Bảng danh sách nhóm (Bên trái) ---
        String[] groupColumns = {"ID", "Tên nhóm", "Ngày tạo", "Số thành viên"};
        DefaultTableModel groupTableModel = new DefaultTableModel(groupColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        groupTable = new JTable(groupTableModel);
        groupTable.setRowHeight(25);
        Color lightBlue = new Color(135, 206, 250);
        groupTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        groupTable.getTableHeader().setBackground(lightBlue);
        groupTable.getTableHeader().setForeground(Color.WHITE);
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // --- Danh sách thành viên (Bên phải) ---
        memberListModel = new DefaultListModel<>();
        memberList = new JList<>(memberListModel);
        
        // --- Danh sách admin (Bên phải) ---
        adminListModel = new DefaultListModel<>();
        adminList = new JList<>(adminListModel);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Lọc và Sắp xếp (NORTH) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(createTitledBorder("Bộ lọc và Sắp xếp"));
        searchPanel.add(new JLabel("Tìm theo tên nhóm:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        searchPanel.add(searchButton);
        
        // --- Panel 2: Nội dung chính (CENTER) - Dùng JSplitPane ---
        
        // 2a. Panel BÊN TRÁI (Danh sách nhóm)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(createTitledBorder("Danh sách nhóm"));
        leftPanel.add(new JScrollPane(groupTable), BorderLayout.CENTER);

        // 2b. Panel BÊN PHẢI (Chi tiết thành viên và admin)
        JPanel rightPanel = new JPanel();
        // Chia bên phải làm 2 hàng (1 cột)
        rightPanel.setLayout(new GridLayout(2, 1, 0, 10)); 
        
        // Panel chi tiết thành viên
        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.setBorder(createTitledBorder("Danh sách thành viên (của nhóm)"));
        memberPanel.add(new JScrollPane(memberList), BorderLayout.CENTER);
        
        // Panel chi tiết admin
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(createTitledBorder("Danh sách Admin (của nhóm)"));
        adminPanel.add(new JScrollPane(adminList), BorderLayout.CENTER);
        
        rightPanel.add(memberPanel);
        rightPanel.add(adminPanel);
        
        // --- Tạo JSplitPane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        // Đặt vị trí thanh chia, 40% cho bên trái
        splitPane.setResizeWeight(0.4); 

        // Thêm các panel chính vào layout
        add(searchPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho bảng Nhóm
        DefaultTableModel model = (DefaultTableModel) groupTable.getModel();
        model.addRow(new Object[]{"G1", "Lớp Lập Trình Java", "2024-01-01", 30});
        model.addRow(new Object[]{"G2", "Team Báo Cáo", "2024-02-15", 5});
        model.addRow(new Object[]{"G3", "Gia đình", "2024-03-20", 3});
        
        // Dữ liệu ban đầu cho JList
        memberListModel.addElement("Vui lòng chọn một nhóm từ danh sách bên trái...");
        adminListModel.addElement("Vui lòng chọn một nhóm từ danh sách bên trái...");
    }
    
    /**
     * Hàm này đăng ký sự kiện: khi click vào 1 hàng trong bảng
     */
    private void setupEventHandlers() {
        // Lắng nghe sự kiện khi người dùng chọn 1 hàng trong bảng groupTable
        groupTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // e.getValueIsAdjusting() == false nghĩa là sự kiện đã chọn xong (không phải đang kéo chuột)
                if (!e.getValueIsAdjusting()) {
                    handleGroupSelection();
                }
            }
        });
    }
    
    /**
     * Xử lý khi 1 nhóm được chọn (Yêu cầu C và D)
     */
    private void handleGroupSelection() {
        int selectedRow = groupTable.getSelectedRow();
        
        // Xóa dữ liệu cũ
        memberListModel.clear();
        adminListModel.clear();

        if (selectedRow == -1) {
            // Nếu không chọn gì (ví dụ: sau khi lọc)
            memberListModel.addElement("Vui lòng chọn một nhóm từ danh sách bên trái...");
            adminListModel.addElement("Vui lòng chọn một nhóm từ danh sách bên trái...");
            return;
        }

        // Lấy ID của nhóm
        String groupID = (String) groupTable.getValueAt(selectedRow, 0);

        // *** TRONG THỰC TẾ: BẠN SẼ TRUY VẤN DATABASE DỰA TRÊN groupID ***
        
        // Dưới đây là dữ liệu giả lập (hard-coded) để demo
        if (groupID.equals("G1")) {
            // Thành viên nhóm G1
            memberListModel.addElement("admin (Admin)");
            memberListModel.addElement("user1 (Admin)");
            memberListModel.addElement("user2");
            memberListModel.addElement("user3");
            // Admin nhóm G1
            adminListModel.addElement("admin");
            adminListModel.addElement("user1");
        } else if (groupID.equals("G2")) {
            // Thành viên nhóm G2
            memberListModel.addElement("admin (Admin)");
            memberListModel.addElement("user4");
            // Admin nhóm G2
            adminListModel.addElement("admin");
        } else if (groupID.equals("G3")) {
            // Thành viên nhóm G3
            memberListModel.addElement("user1 (Admin)");
            memberListModel.addElement("user2");
            // Admin nhóm G3
            adminListModel.addElement("user1");
        }
    }
    
    // --- Các hàm hỗ trợ tạo kiểu (Copy từ các file trước) ---

    private Border createTitledBorder(String title) {
        Border emptyInside = new EmptyBorder(5, 5, 5, 5);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleColor(ZALO_BLUE);
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        return BorderFactory.createCompoundBorder(titledBorder, emptyInside);
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ZALO_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
    }
}