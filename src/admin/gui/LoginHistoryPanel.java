// Đổi tên file thành LoginHistoryPanel.java
// package admin.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Giao diện xem lịch sử đăng nhập - ĐÃ SỬA THÀNH PANEL
 * Đã áp dụng màu sắc đồng bộ
 */
// THAY ĐỔI 1: Kế thừa từ JPanel
public class LoginHistoryPanel extends JPanel {

    // Định nghĩa các màu chủ đạo
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JTable historyTable;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JButton refreshButton, exportButton, filterButton, searchButton;

    // THAY ĐỔI 2: Đổi tên hàm khởi tạo
    public LoginHistoryPanel() {
        initializeComponents();
        setupLayout();
        loadSampleData();
    }

    private void initializeComponents() {
        // THAY ĐỔI 3: Xóa các dòng code của JInternalFrame
        // setTitle("Lịch sử đăng nhập");
        // setSize(1000, 600);
        // setClosable(true);
        // setMaximizable(true);
        // setResizable(true);

        // Bảng hiển thị lịch sử đăng nhập
        String[] columns = {"Thời gian", "Tên đăng nhập", "Họ tên"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(model);
        historyTable.setRowHeight(25);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setFillsViewportHeight(true);

        // --- ÁP DỤNG MÀU SẮC CHO BẢNG ---
        Color lightBlue = new Color(135, 206, 250);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(lightBlue);
        historyTable.getTableHeader().setForeground(Color.WHITE);

        // Thiết lập độ rộng cột
        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);

        // Các trường tìm kiếm và sắp xếp
        searchField = new JTextField(20);
        sortCombo = new JComboBox<>(new String[]{"Sắp xếp theo thời gian (mới nhất)", "Sắp xếp theo tên đăng nhập"});

        // --- ÁP DỤNG MÀU SẮC CHO NÚT BẤM ---
        filterButton = new JButton("Lọc");
        searchButton = new JButton("Tìm kiếm");
        stylePrimaryButton(filterButton);
        stylePrimaryButton(searchButton);

        refreshButton = new JButton("Làm mới");
        exportButton = new JButton("Xuất Excel");
        stylePrimaryButton(refreshButton);
        stylePrimaryButton(exportButton);
    }

    private void setupLayout() {
        // --- ÁP DỤNG BORDER VÀ PADDING ---
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel Bảng
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(createTitledBorder("Lịch sử đăng nhập"));
        JScrollPane scrollPane = new JScrollPane(historyTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBorder(createTitledBorder("Chức năng"));
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);

        // Chỉ thêm bảng và nút chức năng vào panel chính
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- CÁC HÀM HỖ TRỢ TẠO KIỂU (HELPER METHODS) ---

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
    
    // (Phần loadSampleData không thay đổi)
    private void loadSampleData() {
        // Dữ liệu mẫu (đã sắp xếp mới nhất lên đầu)
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.addRow(new Object[]{"2024-01-02 14:00:00", "user4", "Phạm Thị D"});
        model.addRow(new Object[]{"2024-01-02 11:00:00", "user3", "Lê Văn C"});
        model.addRow(new Object[]{"2024-01-02 09:15:00", "user1", "Nguyễn Văn A"});
        model.addRow(new Object[]{"2024-01-02 08:30:00", "admin", "Quản trị viên"});
        model.addRow(new Object[]{"2024-01-01 10:00:00", "user2", "Trần Thị B"});
        model.addRow(new Object[]{"2024-01-01 09:00:00", "user1", "Nguyễn Văn A"});
        model.addRow(new Object[]{"2024-01-01 08:00:00", "admin", "Quản trị viên"});
    }
}