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
 * Giao diện Báo cáo Người dùng hoạt động - PHIÊN BẢN 1
 * (Bao gồm các yêu cầu a, b, c)
 */
public class ActiveUserReportPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JTable reportTable;
    private JTextField dateFromField;   // Ô "Từ ngày"
    private JTextField dateToField;     // Ô "Đến ngày"
    private JTextField searchNameField; // Yêu cầu (b) Lọc theo tên
    private JComboBox<String> sortCombo; // Yêu cầu (a) Sắp xếp
    
    // Yêu cầu (c) Lọc theo số lượng hoạt động
    private JComboBox<String> activityTypeCombo; // (ví dụ: Chat với người, chat nhóm...)
    private JComboBox<String> comparisonCombo;
    private JSpinner activityCountSpinner;
    
    private JButton filterButton, refreshButton, exportButton;

    public ActiveUserReportPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // --- Bảng hiển thị báo cáo ---
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Lần mở app", "Chat (Người)", "Chat (Nhóm)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reportTable = new JTable(model);
        reportTable.setRowHeight(25);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setAutoCreateRowSorter(true);

        // Áp dụng màu sắc cho tiêu đề bảng
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(ZALO_BLUE);
        reportTable.getTableHeader().setForeground(Color.WHITE);

        // Chỉnh độ rộng cột
        TableColumnModel columnModel = reportTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(80);
        columnModel.getColumn(5).setPreferredWidth(80);

        // --- Các component Lọc và Sắp xếp ---
        
        // Chọn khoảng thời gian
        dateFromField = new JTextField(10);
        dateToField = new JTextField(10);
        
        // Yêu cầu (b): Lọc theo tên
        searchNameField = new JTextField(15);
        
        // Yêu cầu (a): Sắp xếp
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo Lần mở app"});
        
        // Yêu cầu (c): Lọc theo số lượng hoạt động
        activityTypeCombo = new JComboBox<>(new String[]{"Lần mở app", "Chat (Người)", "Chat (Nhóm)"});
        comparisonCombo = new JComboBox<>(new String[]{"Tất cả", "=", ">", "<"});
        activityCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        
        filterButton = new JButton("Lọc báo cáo");
        stylePrimaryButton(filterButton);

        // --- Các component Chức năng ---
        refreshButton = new JButton("Làm mới");
        exportButton = new JButton("Xuất Excel");
        stylePrimaryButton(refreshButton);
        stylePrimaryButton(exportButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Lọc và Sắp xếp (NORTH) ---
        // Dùng 2 hàng cho đỡ chật
        JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
        filterPanel.setBorder(createTitledBorder("Tùy chọn báo cáo"));
        
        // Hàng 1: Thời gian và Tên
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Từ ngày:"));
        row1.add(dateFromField);
        row1.add(new JLabel("Đến ngày:"));
        row1.add(dateToField);
        row1.add(new JLabel("Lọc theo tên:"));
        row1.add(searchNameField);
        
        // Hàng 2: Lọc hoạt động và Sắp xếp
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(new JLabel("Lọc theo số lượng hoạt động:"));
        row2.add(activityTypeCombo);
        row2.add(comparisonCombo);
        row2.add(activityCountSpinner);
        row2.add(new JLabel("Sắp xếp:"));
        row2.add(sortCombo);
        row2.add(filterButton);

        filterPanel.add(row1, BorderLayout.NORTH);
        filterPanel.add(row2, BorderLayout.CENTER);

        // --- Panel 2: Danh sách Báo cáo (CENTER) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(createTitledBorder("Danh sách người dùng hoạt động"));
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
        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", 150, 20, 5});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", 300, 120, 10});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", 50, 10, 2});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", 10, 1, 1});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", 0, 0, 0});
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
     * Xử lý logic khi nhấn nút "Lọc báo cáo"
     */
    private void handleFilterReport() {
        String fromDate = dateFromField.getText();
        String toDate = dateToField.getText();
        String nameFilter = searchNameField.getText();
        String sortOption = (String) sortCombo.getSelectedItem();
        
        String activityType = (String) activityTypeCombo.getSelectedItem();
        String comparison = (String) comparisonCombo.getSelectedItem();
        int activityCount = (Integer) activityCountSpinner.getValue();
        
        // --- TRONG THỰC TẾ: BẠN SẼ GỌI CSDL VỚI CÁC THAM SỐ NÀY ---
        
        // Demo: Hiển thị thông báo
        String filterMessage;
        if (comparison.equals("Tất cả")) {
            filterMessage = "Tất cả";
        } else {
            filterMessage = activityType + " " + comparison + " " + activityCount;
        }

        JOptionPane.showMessageDialog(this, 
            "Đang lọc báo cáo với các tùy chọn:\n" +
            "Từ ngày: " + (fromDate.isEmpty() ? "N/A" : fromDate) + "\n" +
            "Đến ngày: " + (toDate.isEmpty() ? "N/A" : toDate) + "\n" +
            "Lọc tên: " + (nameFilter.isEmpty() ? "N/A" : nameFilter) + "\n" +
            "Lọc hoạt động: " + filterMessage + "\n" +
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