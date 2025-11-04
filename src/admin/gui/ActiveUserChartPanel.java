package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Giao diện Thống kê (Biểu đồ người dùng HOẠT ĐỘNG theo năm)
 */
public class ActiveUserChartPanel extends JPanel {
    // Định nghĩa màu sắc
    private static final Color TEAL = new Color(75, 192, 192);
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    
    private JComboBox<String> yearCombo;
    private JComboBox<String> chartTypeCombo;

    public ActiveUserChartPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        yearCombo = new JComboBox<>(new String[]{"2024", "2023", "2022", "2021"});
        chartTypeCombo = new JComboBox<>(new String[]{"Biểu đồ cột", "Biểu đồ đường", "Biểu đồ tròn"});
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Chart area (placeholder)
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL, 2),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        JPanel chartContent = new JPanel();
        chartContent.setLayout(new BoxLayout(chartContent, BoxLayout.Y_AXIS));
        chartContent.setOpaque(false);
        
        JLabel chartIcon = new JLabel("📊", SwingConstants.CENTER);
        chartIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        chartIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel chartLabel = new JLabel("Biểu đồ người dùng hoạt động theo năm");
        chartLabel.setFont(new Font("Arial", Font.BOLD, 20));
        chartLabel.setForeground(TEAL);
        chartLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Biểu đồ sẽ hiển thị số lượng người dùng hoạt động theo từng tháng");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        chartContent.add(chartIcon);
        chartContent.add(Box.createVerticalStrut(20));
        chartContent.add(chartLabel);
        chartContent.add(Box.createVerticalStrut(10));
        chartContent.add(infoLabel);
        
        chartPanel.add(chartContent, BorderLayout.CENTER);
        add(chartPanel, BorderLayout.CENTER);

        // Legend panel
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL, 2),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("📉 Biểu đồ người dùng hoạt động");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(TEAL);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        
        filterPanel.add(new JLabel("Năm:"));
        filterPanel.add(yearCombo);
        filterPanel.add(new JLabel("Loại biểu đồ:"));
        filterPanel.add(chartTypeCombo);
        
        JButton viewBtn = createStyledButton("Xem biểu đồ", TEAL);
        JButton exportBtn = createStyledButton("Xuất ảnh", ZALO_BLUE);
        
        filterPanel.add(viewBtn);
        filterPanel.add(exportBtn);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 15, 10, 15)
        ));

        panel.add(createLegendItem("🟦", "Người dùng hoạt động", ZALO_BLUE));
        panel.add(createLegendItem("🟩", "Người dùng mới", new Color(40, 167, 69)));
        panel.add(createLegendItem("🟨", "Tổng người dùng", new Color(255, 193, 7)));

        return panel;
    }

    private JPanel createLegendItem(String icon, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(color);
        
        item.add(iconLabel);
        item.add(textLabel);
        
        return item;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 12, 5, 12));
        return button;
    }
}