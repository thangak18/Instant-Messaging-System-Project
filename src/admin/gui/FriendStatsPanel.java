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
 * Giao diện Báo cáo Thống kê bạn bè - PHIÊN BẢN 1
 * (Bao gồm các yêu cầu a, b, c)
 */
public class FriendStatsPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JTable reportTable;
    private JTextField searchNameField; // Yêu cầu (b) Lọc theo tên
    private JComboBox<String> sortCombo; // Yêu cầu (a) Sắp xếp
    
    // Yêu cầu (c) Lọc theo số lượng bạn
    private JComboBox<String> friendFilterCombo;
    private JSpinner friendFilterSpinner; // Dùng JSpinner để nhập số
    
    private JButton filterButton, refreshButton, exportButton;

    public FriendStatsPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // --- Bảng hiển thị báo cáo ---
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Ngày tạo", "Số bạn (Trực tiếp)", "Số bạn (Bạn của bạn)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reportTable = new JTable(model);
        reportTable.setRowHeight(25);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setAutoCreateRowSorter(true);

        // Áp dụng màu sắc cho tiêu đề bảng
        Color lightBlue = new Color(135, 206, 250);
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(lightBlue);
        reportTable.getTableHeader().setForeground(Color.WHITE);

        // Chỉnh độ rộng cột
        TableColumnModel columnModel = reportTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(130);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(130);

        // --- Các component Lọc và Sắp xếp ---
        
        // Yêu cầu (b): Lọc theo tên
        searchNameField = new JTextField(20);
        
        // Yêu cầu (a): Sắp xếp
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo tên", "Sắp xếp theo thời gian tạo"});
        
        // Yêu cầu (c): Lọc theo số bạn
        friendFilterCombo = new JComboBox<>(new String[]{"Tất cả", "=", ">", "<"});
        friendFilterSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        
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
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(createTitledBorder("Lọc thống kê bạn bè"));
        
        // Ô nhập tên ngắn hơn
        JTextField nameField = new JTextField();
        nameField.setColumns(10); // Giảm chiều dài ô nhập tên

        //compareCombo.setPreferredSize(new Dimension(45, 25)); // Ngắn gọn

        JTextField friendCountField = new JTextField();
        friendCountField.setColumns(5); // Ô nhập số ngắn
        
        filterPanel.add(new JLabel("Tên đăng nhập:"));
        filterPanel.add(nameField);
        filterPanel.add(new JLabel("Số lượng bạn trực tiếp:"));
        filterPanel.add(friendFilterCombo);
        filterPanel.add(friendCountField);
        filterPanel.add(new JLabel("Sắp xếp:"));
        filterPanel.add(sortCombo);
        filterPanel.add(filterButton);
    
        

        // --- Panel 2: Danh sách Báo cáo (CENTER) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(createTitledBorder("Thống kê bạn bè"));
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
        model.addRow(new Object[]{"1", "admin", "Quản trị viên", "2024-01-01", 50, 1500});
        model.addRow(new Object[]{"2", "user1", "Nguyễn Văn A", "2024-01-02", 120, 3200});
        model.addRow(new Object[]{"3", "user2", "Trần Thị B", "2024-01-03", 5, 80});
        model.addRow(new Object[]{"4", "user3", "Lê Văn C", "2024-01-04", 200, 15000});
        model.addRow(new Object[]{"5", "user4", "Phạm Thị D", "2024-01-05", 0, 0});
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
        String nameFilter = searchNameField.getText();
        String sortOption = (String) sortCombo.getSelectedItem();
        String comparison = (String) friendFilterCombo.getSelectedItem();
        int friendCount = (Integer) friendFilterSpinner.getValue();
        
        // --- TRONG THỰC TẾ: BẠN SẼ GỌI CSDL VỚI CÁC THAM SỐ NÀY ---
        
        // Demo: Hiển thị thông báo
        String filterMessage;
        if (comparison.equals("Tất cả")) {
            filterMessage = "Tất cả";
        } else {
            filterMessage = "Số bạn " + comparison + " " + friendCount;
        }

        JOptionPane.showMessageDialog(this, 
            "Đang lọc báo cáo với các tùy chọn:\n" +
            "Lọc tên: " + (nameFilter.isEmpty() ? "N/A" : nameFilter) + "\n" +
            "Lọc số bạn: " + filterMessage + "\n" +
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