package admin.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Giao diện chính của phân hệ quản trị - Tích hợp đầy đủ các chức năng
 */
public class AdminMainFrame extends JFrame {
    private static final Color ZALO_BLUE = new Color(0, 102, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color INFO_CYAN = new Color(23, 162, 184);
    
    private JMenuBar menuBar;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private JPanel homePanel;
    
    // Thêm biến database (comment lại khi chưa có database)
    // private DatabaseConnection dbConnection;
    // private UserDAO userDAO;

    public AdminMainFrame() {
        // Khởi tạo database connection (comment lại khi chưa có database)
        // initDatabase();
        
        initializeComponents();
        setupLayout();
        setupMenu();
        showHomePage();
    }
    
    // private void initDatabase() {
    //     try {
    //         dbConnection = DatabaseConnection.getInstance();
    //         userDAO = new UserDAO();
    //     } catch (Exception e) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Không thể kết nối database: " + e.getMessage(),
    //             "Lỗi Database", JOptionPane.ERROR_MESSAGE);
    //     }
    // }

    private void showHomePage() {
        contentPanel.removeAll();

        homePanel = new JPanel(new BorderLayout(15, 15));
        homePanel.setBackground(LIGHT_GRAY);
        homePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header với tiêu đề và thời gian
        JPanel headerPanel = createHeaderPanel();
        homePanel.add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setOpaque(false);

        // Statistics cards
        JPanel statsPanel = createStatisticsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Quick actions grid
        JPanel actionsPanel = createQuickActionsPanel();
        mainPanel.add(actionsPanel, BorderLayout.CENTER);

        homePanel.add(mainPanel, BorderLayout.CENTER);

        contentPanel.add(homePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, ZALO_BLUE),
            new EmptyBorder(15, 20, 15, 20)
        ));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🏠 Trang chủ quản trị");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ZALO_BLUE);
        titlePanel.add(titleLabel);

        // Time panel
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        timePanel.setOpaque(false);
        
        JLabel timeLabel = new JLabel("🕐 " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")
        ));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        timeLabel.setForeground(Color.GRAY);
        timePanel.add(timeLabel);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(timePanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1160, 140)); // Fix height

        panel.add(createStatCard("Người dùng", "1,234", ZALO_BLUE, "👥"));
        panel.add(createStatCard("Đang online", "87", SUCCESS_GREEN, "🟢"));
        panel.add(createStatCard("Nhóm chat", "45", WARNING_ORANGE, "💬"));
        panel.add(createStatCard("Tin nhắn", "2,156", DANGER_RED, "📨"));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(12, 12, 12, 12)
        ));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        try {
            iconLabel.setFont(new Font("Apple Color Emoji", Font.PLAIN, 32));
        } catch (Exception e) {
            iconLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 30));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(">> Thao tác nhanh");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(ZALO_BLUE);
        titleLabel.setBorder(new EmptyBorder(8, 0, 10, 0));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        gridPanel.setOpaque(false);

        gridPanel.add(createActionCard("👤 Quản lý người dùng", 
            "Quản lý thông tin người dùng", ZALO_BLUE, e -> openUserManagement()));
        gridPanel.add(createActionCard("📜 Lịch sử đăng nhập", 
            "Xem lịch sử truy cập", SUCCESS_GREEN, e -> openLoginHistory()));
        gridPanel.add(createActionCard("👥 Danh sách nhóm", 
            "Quản lý nhóm chat", WARNING_ORANGE, e -> openGroupManagement()));
        gridPanel.add(createActionCard("🔔 Báo cáo spam", 
            "Xem các báo cáo spam", DANGER_RED, e -> openSpamReport()));
        gridPanel.add(createActionCard("🆕 Người dùng mới", 
            "Danh sách người đăng ký", INFO_CYAN, e -> openNewUserReport()));
        gridPanel.add(createActionCard("📊 Thống kê", 
            "Thống kê hệ thống", new Color(111, 66, 193), e -> openStatistics()));
        gridPanel.add(createActionCard("💝 Bạn bè", 
            "Thống kê mối quan hệ", new Color(255, 99, 132), e -> openFriendStats()));
        gridPanel.add(createActionCard("📈 Người hoạt động", 
            "Báo cáo hoạt động", new Color(54, 162, 235), e -> openActiveUserReport()));
        gridPanel.add(createActionCard("📉 Biểu đồ", 
            "Xem biểu đồ chi tiết", new Color(75, 192, 192), e -> openActiveUserChart()));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionCard(String title, String description, Color color, 
                                    java.awt.event.ActionListener action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(18, 15, 18, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(descLabel);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            private final Color hoverColor = new Color(245, 245, 245);
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(hoverColor);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 3),
                    new EmptyBorder(18, 15, 18, 15)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 2),
                    new EmptyBorder(18, 15, 18, 15)
                ));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.actionPerformed(null);
            }
        });

        return card;
    }

    private void initializeComponents() {
        setTitle("Hệ thống quản trị - Chat System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820); // Tăng height một chút
        setLocationRelativeTo(null);

        menuBar = new JMenuBar();
        contentPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Trạng thái: Sẵn sàng"); // Thêm space
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    private void setupLayout() {
        setJMenuBar(menuBar);
        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupMenu() {
        JMenu userMenu = new JMenu("Lựa chọn chức năng");
        
        addMenuItem(userMenu, "Quản lý người dùng", e -> openUserManagement());
        addMenuItem(userMenu, "Lịch sử đăng nhập", e -> openLoginHistory());
        addMenuItem(userMenu, "Xem danh sách nhóm chat", e -> openGroupManagement());
        addMenuItem(userMenu, "Xem danh sách báo cáo spam", e -> openSpamReport());
        addMenuItem(userMenu, "Xem danh sách người dùng mới", e -> openNewUserReport());
        addMenuItem(userMenu, "Thống kê người dùng", e -> openStatistics());
        addMenuItem(userMenu, "Danh sách người dùng và số lượng bạn bè", e -> openFriendStats());
        addMenuItem(userMenu, "Xem danh sách người dùng hoạt động", e -> openActiveUserReport());
        addMenuItem(userMenu, "Xem biểu đồ người dùng hoạt động", e -> openActiveUserChart());
        
        menuBar.add(userMenu);
    }

    private void addMenuItem(JMenu menu, String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(action);
        menu.add(item);
    }

    // ==================== MỞ CÁC PANEL CHỨC NĂNG ====================

    private void openUserManagement() {
        openPanel("Quản lý người dùng", loadPanelByClassName("admin.gui.UserManagementPanel"));
    }

    private void openLoginHistory() {
        openPanel("Lịch sử đăng nhập", loadPanelByClassName("admin.gui.LoginHistoryPanel"));
    }

    private void openGroupManagement() {
        openPanel("Quản lý nhóm chat", loadPanelByClassName("admin.gui.GroupManagementPanel"));
    }

    private void openSpamReport() {
        openPanel("Xem danh sách báo cáo spam", loadPanelByClassName("admin.gui.SpamReportPanel"));
    }

    private void openNewUserReport() {
        openPanel("Xem danh sách người dùng mới", loadPanelByClassName("admin.gui.NewUserReportPanel"));
    }

    private void openStatistics() {
        openPanel("Thống kê người dùng", loadPanelByClassName("admin.gui.StatisticsPanel"));
    }

    private void openFriendStats() {
        openPanel("Thống kê bạn bè", loadPanelByClassName("admin.gui.FriendStatsPanel"));
    }

    private void openActiveUserReport() {
        openPanel("Báo cáo người dùng hoạt động", loadPanelByClassName("admin.gui.ActiveUserReportPanel"));
    }

    private void openActiveUserChart() {
        openPanel("Biểu đồ người dùng hoạt động", loadPanelByClassName("admin.gui.ActiveUserChartPanel"));
    }

    // ==================== HELPER METHODS ====================

    private JPanel loadPanelByClassName(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof JPanel) {
                return (JPanel) instance;
            } else {
                return createErrorPanel("Class " + className + " không phải là JPanel");
            }
        } catch (ClassNotFoundException e) {
            return createErrorPanel("Chức năng đang phát triển - Class chưa tồn tại: " + className);
        } catch (Exception e) {
            return createErrorPanel("Lỗi khi tạo panel: " + e.getMessage());
        }
    }

    private void openPanel(String title, JPanel panel) {
        contentPanel.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());

        // Tiêu đề với emoji
        String emojiTitle = getEmojiForTitle(title) + " " + title;
        JLabel titleLabel = new JLabel(emojiTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        // Nội dung
        wrapper.add(panel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backBtn = new JButton("🏠 Quay lại trang chủ");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showHomePage());
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getEmojiForTitle(String title) {
        if (title.contains("người dùng")) return "👤";
        if (title.contains("lịch sử")) return "📜";
        if (title.contains("nhóm")) return "👥";
        if (title.contains("spam")) return "🔔";
        if (title.contains("mới")) return "🆕";
        if (title.contains("Thống kê")) return "📊";
        if (title.contains("bạn bè")) return "💝";
        if (title.contains("hoạt động")) return "📈";
        if (title.contains("Biểu đồ")) return "📉";
        return "📋";
    }

    private JPanel createErrorPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><center>" + message + "<br><br>Vui lòng tạo file class tương ứng</center></html>");
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.RED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdminMainFrame().setVisible(true);
        });
    }
}
