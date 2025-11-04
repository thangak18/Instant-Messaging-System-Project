package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class SpamReportPanel extends JPanel {
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    
    private JTable spamTable;
    private JComboBox<String> statusFilter;

    public SpamReportPanel() {
        initComponents();
        setupLayout();
        loadSampleData();
    }

    private void initComponents() {
        String[] columns = {"ID", "Người báo cáo", "Người bị báo cáo", "Lý do", "Trạng thái", "Ngày báo cáo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        spamTable = new JTable(model);
        spamTable.setRowHeight(25);
        spamTable.setAutoCreateRowSorter(true);
        spamTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        spamTable.getTableHeader().setBackground(DANGER_RED);
        spamTable.getTableHeader().setForeground(Color.WHITE);
        
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Chờ xử lý", "Đã xử lý", "Từ chối"});
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel("🔔 Danh sách báo cáo spam");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(DANGER_RED);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        tablePanel.add(titleLabel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(spamTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton processBtn = createStyledButton("Xử lý", ZALO_BLUE);
        JButton rejectBtn = createStyledButton("Từ chối", DANGER_RED);
        
        buttonPanel.add(processBtn);
        buttonPanel.add(rejectBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("🔍 Lọc báo cáo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(DANGER_RED);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Trạng thái:"));
        formPanel.add(statusFilter);
        formPanel.add(createStyledButton("Lọc", DANGER_RED));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) spamTable.getModel();
        model.addRow(new Object[]{"1", "user1", "user456", "Spam quảng cáo", "Chờ xử lý", "2024-01-15"});
        model.addRow(new Object[]{"2", "user2", "user789", "Ngôn từ thô tục", "Đã xử lý", "2024-01-14"});
        model.addRow(new Object[]{"3", "user3", "user123", "Lừa đảo", "Chờ xử lý", "2024-01-13"});
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