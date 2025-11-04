package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Giao diện quản lý nhóm chat - PHIÊN BẢN 1
 * Sử dụng JSplitPane để chia giao diện Master-Detail
 */
public class GroupManagementPanel extends JPanel {
    // Định nghĩa màu
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);

    // -- Các components chính --
    private JTable groupTable; // Bảng danh sách nhóm (Bên trái)
    private JTextField searchField;

    public GroupManagementPanel() {
        initComponents();
        setupLayout();
        loadSampleData();
    }

    private void initComponents() {
        String[] columns = {"ID", "Tên nhóm", "Admin", "Số thành viên", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        groupTable = new JTable(model);
        groupTable.setRowHeight(25);
        groupTable.setAutoCreateRowSorter(true);
        groupTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        groupTable.getTableHeader().setBackground(WARNING_ORANGE);
        groupTable.getTableHeader().setForeground(Color.WHITE);

        searchField = new JTextField(20);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("👥 Danh sách nhóm chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(WARNING_ORANGE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        tablePanel.add(titleLabel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(groupTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton viewMembersBtn = createStyledButton("Xem thành viên", ZALO_BLUE);
        JButton refreshBtn = createStyledButton("Làm mới", WARNING_ORANGE);

        buttonPanel.add(viewMembersBtn);
        buttonPanel.add(refreshBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("🔍 Tìm kiếm nhóm chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(WARNING_ORANGE);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Tìm kiếm:"));
        formPanel.add(searchField);
        formPanel.add(createStyledButton("Tìm", WARNING_ORANGE));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadSampleData() {
        DefaultTableModel model = (DefaultTableModel) groupTable.getModel();
        model.addRow(new Object[]{"1", "Nhóm Java Dev", "admin", 15, "2024-01-01"});
        model.addRow(new Object[]{"2", "Team Project", "user1", 8, "2024-01-05"});
        model.addRow(new Object[]{"3", "Lập trình viên", "user2", 25, "2024-01-10"});
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