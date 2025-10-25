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
 * Giao diện Báo cáo Người dùng mới - PHIÊN BẢN 1
 * (Bao gồm chọn khoảng thời gian, lọc theo tên, sắp xếp)
 */
public class NewUserReportPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JTable reportTable;
    private JTextField dateFromField;   // Ô "Từ ngày"
    private JTextField dateToField;     // Ô "Đến ngày"
    private JTextField searchNameField; // Yêu cầu (b) Lọc theo tên
    private JComboBox<String> sortCombo; // Yêu cầu (a) Sắp xếp
    private JButton filterButton, refreshButton, exportButton;

    public NewUserReportPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // --- Bảng hiển thị báo cáo ---
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Email", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reportTable = new JTable(model);
        reportTable.setRowHeight(25);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setAutoCreateRowSorter(true); // Cho phép sắp xếp khi nhấn vào tiêu đề

        // Áp dụng màu sắc cho tiêu đề bảng
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(ZALO_BLUE);
        reportTable.getTableHeader().setForeground(Color.WHITE);

        // Chỉnh độ rộng cột
        TableColumnModel columnModel = reportTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);  // ID
        columnModel.getColumn(1).setPreferredWidth(100); // Tên đăng nhập
        columnModel.getColumn(2).setPreferredWidth(120); // Họ tên
        columnModel.getColumn(3).setPreferredWidth(150); // Email
        columnModel.getColumn(4).setPreferredWidth(130); // Ngày tạo

        // --- Các component Lọc và Sắp xếp ---
        
        // Yêu cầu (chính): Chọn khoảng thời gian
        dateFromField = new JTextField(10); // Kích thước cho "yyyy-mm-dd"
        dateToField = new JTextField(10);
        
        // Yêu cầu (b): Lọc theo tên
        searchNameField = new JTextField(20);
        
        // Yêu cầu (a): Sắp xếp
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo thời gian tạo", "Sắp xếp theo tên"});
        
        filterButton = new JButton("Hiển thị báo cáo");
        stylePrimaryButton(filterButton);

        // --- Các component Chức năng ---
        refreshButton = new JButton("Làm mới");
        exportButton = new JButton("Xuất Excel");
        styleNeutralButton(refreshButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Lọc và Sắp xếp (NORTH) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(createTitledBorder("Tùy chọn báo cáo"));
        
        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(dateFromField);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(dateToField);
        filterPanel.add(new JLabel("Lọc theo tên:"));
        filterPanel.add(searchNameField);
        filterPanel.add(new JLabel("Sắp xếp:"));
        filterPanel.add(sortCombo);
        filterPanel.add(filterButton);

        // --- Panel 2: Danh sách Báo cáo (CENTER) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(createTitledBorder("Danh sách người dùng đăng ký mới"));
        centerPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // --- Panel 3: Chức năng (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBorder(createTitledBorder("Chức năng"));
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);

        // Thêm vào layout chính
        add(filterPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSampleData() {
        // Dữ liệu mẫu (chỉ hiển thị khi nhấn nút)
        // loadSampleDataByDateRange("2024-01-01", "2024-01-31");
        
        // Tạm thời hiển thị tất cả
        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "admin@chat.com", "2024-01-01"});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "user1@email.com", "2024-01-02"});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "user2@email.com", "2024-01-03"});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "user3@email.com", "2024-01-04"});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", "user4@email.com", "2024-01-05"});
    }

    /**
     * Đăng ký sự kiện cho các nút bấm
     */
    private void setupEventHandlers() {
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFilterReport();
            }
        });
    }

    /**
     * Xử lý logic khi nhấn nút "Hiển thị báo cáo"
     */
    private void handleFilterReport() {
        String fromDate = dateFromField.getText();
        String toDate = dateToField.getText();
        String nameFilter = searchNameField.getText();
        String sortOption = (String) sortCombo.getSelectedItem();
        
        // --- TRONG THỰC TẾ: BẠN SẼ GỌI CSDL VỚI CÁC THAM SỐ NÀY ---
        // ví dụ: List<User> users = ReportController.getNewUsers(fromDate, toDate, nameFilter, sortOption);
        // Sau đó xóa bảng cũ và load dữ liệu mới vào
        
        // Demo: Hiển thị thông báo
        JOptionPane.showMessageDialog(this, 
            "Đang tạo báo cáo với các tùy chọn:\n" +
            "Từ ngày: " + (fromDate.isEmpty() ? "N/A" : fromDate) + "\n" +
            "Đến ngày: " + (toDate.isEmpty() ? "N/A" : toDate) + "\n" +
            "Lọc tên: " + (nameFilter.isEmpty() ? "N/A" : nameFilter) + "\n" +
            "Sắp xếp: " + sortOption,
            "Đang xử lý",
            JOptionPane.INFORMATION_MESSAGE);
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
    
    private void styleNeutralButton(JButton button) {
        button.setBackground(NEUTRAL_GRAY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
    }
}