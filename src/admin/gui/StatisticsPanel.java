import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Giao diện Thống kê (Biểu đồ đăng ký theo năm)
 * Bao gồm bộ lọc và một panel tùy chỉnh để vẽ biểu đồ
 */
public class StatisticsPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    
    private JComboBox<Integer> yearSelector;
    private JButton viewButton;
    private BarChartPanel chartPanel; // Panel tùy chỉnh để vẽ

    public StatisticsPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // Tải dữ liệu mẫu cho lần chạy đầu tiên
        loadDataForYear(2024); 
    }

    private void initializeComponents() {
        // --- Bộ lọc (Chọn năm) ---
        Integer[] years = {2024, 2023, 2022}; // Dữ liệu năm mẫu
        yearSelector = new JComboBox<>(years);
        
        viewButton = new JButton("Xem biểu đồ");
        stylePrimaryButton(viewButton);

        // --- Panel vẽ biểu đồ ---
        chartPanel = new BarChartPanel();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Bộ lọc (NORTH) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(createTitledBorder("Tùy chọn biểu đồ"));
        
        filterPanel.add(new JLabel("Chọn năm:"));
        filterPanel.add(yearSelector);
        filterPanel.add(viewButton);

        // --- Panel 2: Biểu đồ (CENTER) ---
        JPanel chartDisplayPanel = new JPanel(new BorderLayout());
        chartDisplayPanel.setBorder(createTitledBorder("Biểu đồ số lượng người đăng ký mới"));
        chartDisplayPanel.add(chartPanel, BorderLayout.CENTER);

        // Thêm vào layout chính
        add(filterPanel, BorderLayout.NORTH);
        add(chartDisplayPanel, BorderLayout.CENTER);
    }

    /**
     * Đăng ký sự kiện cho nút "Xem biểu đồ"
     */
    private void setupEventHandlers() {
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedYear = (Integer) yearSelector.getSelectedItem();
                loadDataForYear(selectedYear);
            }
        });
    }

    /**
     * Tải (hoặc giả lập) dữ liệu cho năm được chọn
     */
    private void loadDataForYear(int year) {
        // --- TRONG THỰC TẾ: BẠN SẼ GỌI CSDL TẠI ĐÂY ---
        // ví dụ: int[] data = ReportController.getRegistrationDataByYear(year);
        
        // --- DỮ LIỆU GIẢ LẬP (DEMO) ---
        int[] data = new int[12];
        Random rand = new Random();
        
        // Tạo dữ liệu ngẫu nhiên khác nhau cho mỗi năm để thấy sự thay đổi
        int baseValue = 50;
        if (year == 2023) {
            baseValue = 30;
        } else if (year == 2022) {
            baseValue = 10;
        }
        
        for (int i = 0; i < 12; i++) {
            data[i] = baseValue + rand.nextInt(50); // Giá trị ngẫu nhiên
        }
        
        // Cập nhật dữ liệu cho panel vẽ và yêu cầu nó vẽ lại
        chartPanel.updateData(data);
    }
    
    // --- LỚP CON (INNER CLASS) ĐỂ VẼ BIỂU ĐỒ ---
    /**
     * Đây là Panel tùy chỉnh, chịu trách nhiệm vẽ biểu đồ cột.
     */
    private class BarChartPanel extends JPanel {
        private int[] data = new int[12]; // 12 tháng
        private String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};

        public BarChartPanel() {
            setBackground(Color.WHITE); // Nền trắng cho biểu đồ
        }
        
        /**
         * Nhận dữ liệu mới và gọi repaint() để vẽ lại
         */
        public void updateData(int[] newData) {
            if (newData != null && newData.length == 12) {
                this.data = newData;
                repaint(); // Rất quan trọng: Yêu cầu Swing vẽ lại panel này
            }
        }

        /**
         * Đây là phương thức cốt lõi, nơi chúng ta "vẽ"
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Vẽ nền (màu trắng)
            
            Graphics2D g2 = (Graphics2D) g;
            
            // Bật chế độ khử răng cưa cho đẹp hơn
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            
            // Định nghĩa lề cho khu vực vẽ
            int padding = 30;
            int labelPadding = 20;
            
            // Tọa độ của gốc (0,0) cho biểu đồ
            int chartOriginX = padding + labelPadding;
            int chartOriginY = panelHeight - padding - labelPadding;
            
            // Chiều rộng và chiều cao của khu vực vẽ biểu đồ
            int chartWidth = panelWidth - 2 * padding - labelPadding;
            int chartHeight = panelHeight - 2 * padding - labelPadding;

            // --- 1. Vẽ 2 trục X và Y ---
            g2.setColor(Color.BLACK);
            g2.drawLine(chartOriginX, chartOriginY, chartOriginX, padding); // Trục Y
            g2.drawLine(chartOriginX, chartOriginY, chartOriginX + chartWidth, chartOriginY); // Trục X

            // --- 2. Tìm giá trị lớn nhất để chia tỉ lệ (Trục Y) ---
            int maxDataValue = 0;
            for (int value : data) {
                if (value > maxDataValue) {
                    maxDataValue = value;
                }
            }
            // Làm tròn max_value lên (ví dụ: 95 -> 100)
            maxDataValue = (int) (Math.ceil(maxDataValue / 50.0) * 50);
            if (maxDataValue == 0) maxDataValue = 50; // Tránh chia cho 0

            // Vẽ các vạch chia tỉ lệ trục Y
            int yTickCount = 5;
            for (int i = 0; i <= yTickCount; i++) {
                int y = chartOriginY - (i * chartHeight) / yTickCount;
                g2.setColor(Color.LIGHT_GRAY); // Vạch mờ
                g2.drawLine(chartOriginX, y, chartOriginX + chartWidth, y);
                
                g2.setColor(Color.BLACK); // Chữ
                String yLabel = String.valueOf((i * maxDataValue) / yTickCount);
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(yLabel);
                g2.drawString(yLabel, chartOriginX - labelWidth - 5, y + (fm.getHeight() / 2) - 3);
            }

            // --- 3. Vẽ các cột (Trục X) ---
            int barWidth = chartWidth / (data.length * 2); // 1 nửa là cột, 1 nửa là khoảng trắng
            int barSpacing = barWidth;

            for (int i = 0; i < data.length; i++) {
                int barX = chartOriginX + (i * (barWidth + barSpacing)) + barSpacing / 2;
                
                // Tính chiều cao cột
                // (double) để đảm bảo phép chia là số thực
                int barHeight = (int) (((double) data[i] / maxDataValue) * chartHeight);
                
                int barY = chartOriginY - barHeight;
                
                // Vẽ cột
                g2.setColor(ZALO_BLUE);
                g2.fillRect(barX, barY, barWidth, barHeight);
                
                // Vẽ giá trị trên đầu cột
                g2.setColor(Color.BLACK);
                String valueLabel = String.valueOf(data[i]);
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(valueLabel);
                g2.drawString(valueLabel, barX + (barWidth - labelWidth) / 2, barY - 5);
                
                // Vẽ nhãn tháng (Trục X)
                String monthLabel = months[i];
                labelWidth = fm.stringWidth(monthLabel);
                g2.drawString(monthLabel, barX + (barWidth - labelWidth) / 2, chartOriginY + fm.getHeight());
            }
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