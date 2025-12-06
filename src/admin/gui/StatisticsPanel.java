package admin.gui;

import admin.service.StatisticsDAO;
import admin.socket.UserActivity;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Thống kê - Biểu đồ số lượng người đăng ký mới theo năm
 * Yêu cầu: Chọn năm, vẽ biểu đồ với trục hoành là tháng, trục tung là số lượng
 * người đăng ký mới
 */
public class StatisticsPanel extends JPanel {

    // Định nghĩa màu sắc
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color NEUTRAL_GRAY = new Color(108, 117, 125);

    private JComboBox<Integer> yearSelector;
    private JButton viewButton, refreshButton;
    private BarChartPanel chartPanel;
    private JLabel currentYearLabel;
    private JLabel totalUsersLabel;

    // Backend
    private StatisticsDAO statisticsDAO;

    public StatisticsPanel() {
        this.statisticsDAO = new StatisticsDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();

        // Tải dữ liệu từ database cho năm hiện tại
        loadDataForYear(2025);
    }

    private void initializeComponents() {
        // Bộ lọc - Chọn năm (bao gồm 2025)
        Integer[] years = { 2025, 2024, 2023, 2022, 2021, 2020 };
        yearSelector = new JComboBox<>(years);
        yearSelector.setPreferredSize(new Dimension(100, 30));

        viewButton = createButtonWithIcon("Xem biểu đồ", "chart");
        refreshButton = createButtonWithIcon("Làm mới", "refresh");
        stylePrimaryButton(viewButton);
        stylePrimaryButton(refreshButton);

        // Panel vẽ biểu đồ
        chartPanel = new BarChartPanel();

        // Labels hiển thị thông tin
        currentYearLabel = new JLabel("Năm: 2025");
        currentYearLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentYearLabel.setForeground(ZALO_BLUE);

        totalUsersLabel = new JLabel("Tổng số người đăng ký: 0");
        totalUsersLabel.setFont(new Font("Arial", Font.BOLD, 13));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Panel 1: Bộ lọc (NORTH)
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Panel 2: Biểu đồ (CENTER)
        JPanel chartDisplayPanel = createChartPanel();
        add(chartDisplayPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(15, 15, 15, 15)));

        ImageIcon chartIcon = loadIcon("chart", 20, 20);
        JLabel titleLabel = new JLabel("Tùy chọn biểu đồ");
        if (chartIcon != null) {
            titleLabel.setIcon(chartIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formPanel.setOpaque(false);

        formPanel.add(new JLabel("Chọn năm:"));
        formPanel.add(yearSelector);
        formPanel.add(viewButton);
        formPanel.add(refreshButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        ImageIcon statsIcon = loadIcon("stats", 20, 20);
        JLabel titleLabel = new JLabel("Biểu đồ số lượng người đăng ký mới");
        if (statsIcon != null) {
            titleLabel.setIcon(statsIcon);
            titleLabel.setHorizontalTextPosition(JLabel.RIGHT);
            titleLabel.setIconTextGap(8);
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ZALO_BLUE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(currentYearLabel);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(totalUsersLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Chart area
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        chartContainer.add(chartPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chartContainer, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        viewButton.addActionListener(e -> {
            Integer selectedYear = (Integer) yearSelector.getSelectedItem();
            loadDataForYear(selectedYear);
        });

        refreshButton.addActionListener(e -> {
            Integer selectedYear = (Integer) yearSelector.getSelectedItem();
            loadDataForYear(selectedYear);
            JOptionPane.showMessageDialog(this,
                    "Đã làm mới dữ liệu năm " + selectedYear + "!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Tải dữ liệu cho năm được chọn
     * Trục hoành: 12 tháng (T1 -> T12)
     * Trục tung: Số lượng người đăng ký mới
     */
    private void loadDataForYear(int year) {
        try {
            // Lấy dữ liệu tăng trưởng người dùng theo tháng của năm được chọn
            int[] data = statisticsDAO.getUserGrowthByMonth(year);

            // Tính tổng
            int totalUsers = 0;
            for (int count : data) {
                totalUsers += count;
            }

            // Cập nhật biểu đồ
            chartPanel.updateData(data);

            // Cập nhật labels
            currentYearLabel.setText("Năm: " + year);
            totalUsersLabel.setText("Tổng số người đăng ký: " + totalUsers);

        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            String detailedMsg = "Lỗi load dữ liệu thống kê: " + errorMsg;
            
            if (errorMsg != null && (errorMsg.contains("connection") || 
                                     errorMsg.contains("Connection"))) {
                detailedMsg += "\n\nVui lòng kiểm tra:\n" +
                              "- Kết nối database\n" +
                              "- Năm đã chọn\n" +
                              "- File config.properties\n" +
                              "Hoặc liên hệ admin để được hỗ trợ.";
            }
            
            showError(detailedMsg);
            e.printStackTrace();

            // Fallback: hiển thị dữ liệu rỗng
            chartPanel.updateData(new int[12]);
            currentYearLabel.setText("Năm: " + year);
            totalUsersLabel.setText("Tổng số người đăng ký: 0");
        }
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Lớp con để vẽ biểu đồ cột
     * Trục hoành (X): 12 tháng
     * Trục tung (Y): Số lượng người đăng ký mới
     */
    private class BarChartPanel extends JPanel {
        private int[] data = new int[12]; // 12 tháng
        private String[] months = { "T1", "T2", "T3", "T4", "T5", "T6",
                "T7", "T8", "T9", "T10", "T11", "T12" };

        public BarChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 400));
        }

        public void updateData(int[] newData) {
            if (newData != null && newData.length == 12) {
                this.data = newData;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Định nghĩa lề
            int padding = 40;
            int labelPadding = 25;

            // Gốc tọa độ biểu đồ
            int chartOriginX = padding + labelPadding;
            int chartOriginY = panelHeight - padding - labelPadding;

            // Kích thước khu vực vẽ
            int chartWidth = panelWidth - 2 * padding - labelPadding;
            int chartHeight = panelHeight - 2 * padding - labelPadding;

            // Vẽ 2 trục
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(chartOriginX, chartOriginY, chartOriginX, padding); // Trục Y
            g2.drawLine(chartOriginX, chartOriginY, chartOriginX + chartWidth, chartOriginY); // Trục X

            // Vẽ nhãn trục
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("Số lượng", 5, padding - 5); // Nhãn trục Y
            g2.drawString("Tháng", chartOriginX + chartWidth + 10, chartOriginY + 5); // Nhãn trục X

            // Tìm giá trị max để chia tỉ lệ
            int maxDataValue = 0;
            for (int value : data) {
                if (value > maxDataValue) {
                    maxDataValue = value;
                }
            }
            maxDataValue = (int) (Math.ceil(maxDataValue / 50.0) * 50);
            if (maxDataValue == 0)
                maxDataValue = 100;

            // Vẽ các vạch chia trục Y
            int yTickCount = 5;
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= yTickCount; i++) {
                int y = chartOriginY - (i * chartHeight) / yTickCount;

                // Vạch chia ngang
                g2.setColor(new Color(220, 220, 220));
                g2.drawLine(chartOriginX, y, chartOriginX + chartWidth, y);

                // Nhãn số
                g2.setColor(Color.BLACK);
                String yLabel = String.valueOf((i * maxDataValue) / yTickCount);
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(yLabel);
                g2.drawString(yLabel, chartOriginX - labelWidth - 8, y + (fm.getHeight() / 2) - 3);
            }

            // Vẽ các cột cho 12 tháng
            int barWidth = chartWidth / (data.length * 2);
            int barSpacing = barWidth;

            for (int i = 0; i < data.length; i++) {
                int barX = chartOriginX + (i * (barWidth + barSpacing)) + barSpacing / 2;

                // Tính chiều cao cột dựa trên dữ liệu
                int barHeight = (int) (((double) data[i] / maxDataValue) * chartHeight);
                int barY = chartOriginY - barHeight;

                // Vẽ cột với gradient
                GradientPaint gradient = new GradientPaint(
                        barX, barY, ZALO_BLUE,
                        barX, chartOriginY, new Color(100, 180, 255));
                g2.setPaint(gradient);
                g2.fillRect(barX, barY, barWidth, barHeight);

                // Vẽ viền cột
                g2.setColor(ZALO_BLUE.darker());
                g2.drawRect(barX, barY, barWidth, barHeight);

                // Vẽ giá trị trên đầu cột
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                String valueLabel = String.valueOf(data[i]);
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(valueLabel);
                g2.drawString(valueLabel, barX + (barWidth - labelWidth) / 2, barY - 5);

                // Vẽ nhãn tháng (Trục X)
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                String monthLabel = months[i];
                labelWidth = fm.stringWidth(monthLabel);
                g2.drawString(monthLabel, barX + (barWidth - labelWidth) / 2,
                        chartOriginY + fm.getHeight() + 2);
            }
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ZALO_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleNeutralButton(JButton button) {
        button.setBackground(NEUTRAL_GRAY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        try {
            String path = "icons/" + iconName + ".png";
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
        }
        return null;
    }

    private JButton createButtonWithIcon(String text, String iconName) {
        JButton button = new JButton(text);
        ImageIcon icon = loadIcon(iconName, 16, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setHorizontalTextPosition(JButton.RIGHT);
            button.setIconTextGap(8);
        }
        button.setPreferredSize(new java.awt.Dimension(200, 35));
        return button;
    }

}