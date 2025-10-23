// package admin.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện thống kê - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class StatisticsFrame extends JInternalFrame {
    private JComboBox<String> yearCombo;
    private JComboBox<String> chartTypeCombo;
    private JButton generateButton, exportButton;
    private JPanel chartPanel;
    
    public StatisticsFrame() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setTitle("Thống kê và biểu đồ");
        setSize(1000, 600);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Combo box chọn năm
        yearCombo = new JComboBox<>(new String[]{"2024", "2023", "2022", "2021", "2020"});
        
        // Combo box chọn loại biểu đồ
        chartTypeCombo = new JComboBox<>(new String[]{
            "Biểu đồ số lượng người đăng ký mới theo năm",
            "Biểu đồ số lượng người hoạt động theo năm",
            "Thống kê người dùng mới theo khoảng thời gian",
            "Thống kê người dùng hoạt động theo khoảng thời gian"
        });
        
        // Các nút chức năng
        generateButton = new JButton("Tạo biểu đồ");
        exportButton = new JButton("Xuất báo cáo");
        
        // Panel hiển thị biểu đồ
        chartPanel = new JPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ thống kê"));
        chartPanel.setLayout(new BorderLayout());
        
        // Thêm label mẫu cho biểu đồ
        JLabel sampleChart = new JLabel("Biểu đồ sẽ được hiển thị ở đây", JLabel.CENTER);
        sampleChart.setFont(new Font("Arial", Font.BOLD, 16));
        chartPanel.add(sampleChart, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Chọn năm:"));
        controlPanel.add(yearCombo);
        controlPanel.add(new JLabel("Loại biểu đồ:"));
        controlPanel.add(chartTypeCombo);
        controlPanel.add(generateButton);
        controlPanel.add(exportButton);
        
        // Panel thông tin thống kê
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin thống kê"));
        
        // Thống kê mẫu
        infoPanel.add(new JLabel("Tổng số người dùng: 100"));
        infoPanel.add(new JLabel("Người dùng hoạt động: 85"));
        infoPanel.add(new JLabel("Tin nhắn hôm nay: 1,250"));
        infoPanel.add(new JLabel("Nhóm chat: 15"));
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }
}
