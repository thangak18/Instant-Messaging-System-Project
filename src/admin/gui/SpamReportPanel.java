package admin.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Giao diện quản lý Báo Cáo Spam - PHIÊN BẢN 1
 * (Đã gộp ô lọc thời gian)
 */
public class SpamReportPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color DESTRUCTIVE_RED = new Color(220, 53, 69);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JTable spamTable;
    private JTextField searchUserField; // Ô tìm theo tên
    
    // --- THAY ĐỔI 1: GỘP 2 Ô THÀNH 1 Ô ---
    private JTextField dateFilterField; // Ô lọc thời gian (Gộp)
    
    private JComboBox<String> sortCombo;
    private JButton searchButton, lockUserButton, markAsDoneButton, refreshButton;

    public SpamReportPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // --- Bảng hiển thị báo cáo ---
        String[] columns = {"ID", "Thời Gian", "Người Báo Cáo", "Người Bị Tố Cáo", "Nội dung", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        spamTable = new JTable(model);
        // ... (code thiết lập bảng giữ nguyên) ...
        Color lightBlue = new Color(135, 206, 250);
        spamTable.setRowHeight(25);
        spamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        spamTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        spamTable.getTableHeader().setBackground(lightBlue);
        spamTable.getTableHeader().setForeground(Color.WHITE);
        TableColumnModel columnModel = spamTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(130);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(250);
        columnModel.getColumn(5).setPreferredWidth(80);

        // --- Các component Lọc và Sắp xếp ---
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo thời gian (mới nhất)", "Sắp xếp theo tên đăng nhập"});
        
        searchUserField = new JTextField(15); // Ô lọc tên
        
        // --- THAY ĐỔI 2: KHỞI TẠO 1 Ô THỜI GIAN ---
        dateFilterField = new JTextField(15); // Đặt kích thước 20
        
        searchButton = new JButton("Lọc/Tìm");
        stylePrimaryButton(searchButton);

        // --- Các component Chức năng ---
        lockUserButton = new JButton("Khóa tài khoản bị tố cáo");
        stylePrimaryButton(lockUserButton); 
        markAsDoneButton = new JButton("Đánh dấu 'Đã xử lý'");
        stylePrimaryButton(markAsDoneButton);
        refreshButton = new JButton("Làm mới");
        stylePrimaryButton(refreshButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Lọc và Sắp xếp (NORTH) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(createTitledBorder("Bộ lọc và Sắp xếp"));
        
        // --- THAY ĐỔI 3: SỬA LẠI PANEL LỌC ---
        
        // Lọc theo tên (giữ nguyên)
        searchPanel.add(new JLabel("Lọc theo tên:"));
        searchPanel.add(searchUserField);
        
        // Lọc theo thời gian (đã gộp)
        searchPanel.add(new JLabel("Lọc theo thời gian:"));
        searchPanel.add(dateFilterField); // Thêm ô đã gộp
        
        // Sắp xếp (giữ nguyên)
        searchPanel.add(new JLabel("Sắp xếp:"));
        searchPanel.add(sortCombo);
        
        // Nút bấm (giữ nguyên)
        searchPanel.add(searchButton);
        
        // --- HẾT THAY ĐỔI ---

        // --- Panel 2: Danh sách Báo cáo (CENTER) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(createTitledBorder("Danh sách báo cáo spam"));
        centerPanel.add(new JScrollPane(spamTable), BorderLayout.CENTER);

        // --- Panel 3: Chức năng (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBorder(createTitledBorder("Hành động"));
        buttonPanel.add(lockUserButton);
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(refreshButton);

        // Thêm vào layout chính
        add(searchPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // --- THÊM LABEL THÔNG TIN PHÁT TRIỂN ---
        JLabel devLabel = new JLabel("Báo cáo spam - Đang phát triển");
        devLabel.setFont(new Font("Arial", Font.BOLD, 18));
        devLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(devLabel, BorderLayout.CENTER);
    }

    private void loadSampleData() {
        // ... (Giữ nguyên không đổi) ...
        DefaultTableModel model = (DefaultTableModel) spamTable.getModel();
        model.addRow(new Object[]{"S1", "2024-05-10 09:15:00", "user1", "spam_user_A", "Gửi link quảng cáo", "Mới"});
        model.addRow(new Object[]{"S2", "2024-05-10 08:30:00", "user2", "spam_user_B", "Nội dung 18+", "Mới"});
        model.addRow(new Object[]{"S3", "2024-05-09 14:00:00", "user3", "spam_user_A", "Spam link liên tục", "Đã xử lý"});
        model.addRow(new Object[]{"S4", "2024-05-09 11:00:00", "user4", "spam_user_C", "Lừa đảo", "Mới"});
    }

    private void setupEventHandlers() {
        // ... (Giữ nguyên không đổi) ...
        lockUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLockUser();
            }
        });
    }

    private void handleLockUser() {
        // ... (GiV
        int selectedRow = spamTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một báo cáo trong bảng trước.", 
                "Chưa chọn báo cáo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userToLock = (String) spamTable.getValueAt(selectedRow, 3);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn KHÓA tài khoản \"" + userToLock + "\" không?\n" +
            "Hành động này không thể hoàn tác dễ dàng.",
            "Xác nhận Khóa tài khoản",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("Đã gửi yêu cầu khóa tài khoản: " + userToLock);
            JOptionPane.showMessageDialog(this, 
                "Đã khóa tài khoản \"" + userToLock + "\" thành công.",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            spamTable.setValueAt("Đã xử lý", selectedRow, 5);
        }
    }

    // --- Các hàm hỗ trợ tạo kiểu (Giữ nguyên không đổi) ---

    private Border createTitledBorder(String title) { //Tạo viền có tiêu đề
        Border emptyInside = new EmptyBorder(5, 5, 5, 5);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleColor(ZALO_BLUE);
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        return BorderFactory.createCompoundBorder(titledBorder, emptyInside);
    }

    private void stylePrimaryButton(JButton button) { //màu xanh
        button.setBackground(ZALO_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
    }
    
    private void styleDestructiveButton(JButton button) { //màu đỏ
        button.setBackground(DESTRUCTIVE_RED);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
    }

    private void styleNeutralButton(JButton button) { //màu xám
        button.setBackground(NEUTRAL_GRAY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
    }
}