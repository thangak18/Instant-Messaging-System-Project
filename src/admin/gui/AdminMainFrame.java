package admin.gui;

import admin.service.StatisticsDAO;

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
    private StatisticsDAO statisticsDAO;

    public AdminMainFrame() {
        statisticsDAO = new StatisticsDAO();

        initializeComponents();
        setupLayout();
        setupMenu();
        showHomePage();
    }

    // private void initDatabase() {
    // try {
    // dbConnection = DatabaseConnection.getInstance();
    // userDAO = new UserDAO();
    // } catch (Exception e) {
    // JOptionPane.showMessageDialog(this,
    // "Không thể kết nối database: " + e.getMessage(),
    // "Lỗi Database", JOptionPane.ERROR_MESSAGE);
    // }
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
                new EmptyBorder(15, 20, 15, 20)));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        // Title with icon
        ImageIcon homeIcon = loadIcon("home", 24, 24);
        JLabel titleLabel;
        if (homeIcon != null) {
            titleLabel = new JLabel("Trang chủ quản trị", homeIcon, JLabel.LEFT);
            titleLabel.setIconTextGap(8);
        } else {
            titleLabel = new JLabel("Trang chủ quản trị");
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ZALO_BLUE);
        titlePanel.add(titleLabel);

        // Time panel
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        timePanel.setOpaque(false);

        JLabel timeLabel = new JLabel(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")));
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
        panel.setPreferredSize(new Dimension(1160, 180));

        // Create cards with loading state
        JPanel userCard = createStatCard("Người dùng", "...", ZALO_BLUE, "user");
        JPanel spamCard = createStatCard("Báo cáo spam", "...", ZALO_BLUE, "notification");
        JPanel groupCard = createStatCard("Nhóm chat", "...", ZALO_BLUE, "chat");
        JPanel messageCard = createStatCard("Tin nhắn", "...", ZALO_BLUE, "message");

        panel.add(userCard);
        panel.add(spamCard);
        panel.add(groupCard);
        panel.add(messageCard);

        // Load statistics in background thread to avoid UI freeze
        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                // Load all stats in one background thread
                int users = getDashboardStat(() -> statisticsDAO.getTotalUsers());
                int spamReports = getDashboardStat(() -> statisticsDAO.getTotalSpamReports());
                int groups = getDashboardStat(() -> statisticsDAO.getTotalGroups());
                int messages = getDashboardStat(() -> statisticsDAO.getTotalMessages());
                return new int[] { users, spamReports, groups, messages };
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    updateStatCard(userCard, formatNumber(stats[0]));
                    updateStatCard(spamCard, formatNumber(stats[1]));
                    updateStatCard(groupCard, formatNumber(stats[2]));
                    updateStatCard(messageCard, formatNumber(stats[3]));
                } catch (Exception e) {
                    System.err.println("Error loading dashboard stats: " + e.getMessage());
                    updateStatCard(userCard, "N/A");
                    updateStatCard(spamCard, "N/A");
                    updateStatCard(groupCard, "N/A");
                    updateStatCard(messageCard, "N/A");
                }
            }
        };
        worker.execute();

        return panel;
    }

    // Update value in stat card
    private void updateStatCard(JPanel card, String newValue) {
        Component[] components = card.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                try {
                    // Find the value label (largest font)
                    if (label.getFont().getSize() == 30) {
                        label.setText(newValue);
                        break;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    // Helper để lấy dữ liệu từ database
    @FunctionalInterface
    interface SQLSupplier {
        int get() throws java.sql.SQLException;
    }

    private int getDashboardStat(SQLSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            System.err.println("Error getting dashboard stat: " + e.getMessage());
            return 0;
        }
    }

    // Format number với dấu phẩy
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

    // Icon loader method
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

    private JPanel createStatCard(String title, String value, Color color, String iconName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(12, 12, 12, 12)));

        // Icon - load from PNG file
        ImageIcon icon = loadIcon(iconName, 32, 32);
        JLabel iconLabel;
        if (icon != null) {
            iconLabel = new JLabel(icon);
        } else {
            // Fallback if icon not found
            iconLabel = new JLabel(iconName.toUpperCase().substring(0, 1));
            iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
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

    //
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(">> Thao tác nhanh");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(ZALO_BLUE);
        titleLabel.setBorder(new EmptyBorder(8, 0, 10, 0));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        gridPanel.setOpaque(false);

        gridPanel.add(createActionCard("Quản lý người dùng",
                "Quản lý danh sách người dùng", ZALO_BLUE, "user", e -> openUserManagement()));
        gridPanel.add(createActionCard("Lịch sử đăng nhập",
                "Xem danh sách đăng nhập theo thứ tự thời gian", ZALO_BLUE, "history", e -> openLoginHistory()));
        gridPanel.add(createActionCard("Danh sách nhóm",
                "Xem danh sách các nhóm chat", ZALO_BLUE, "chat", e -> openGroupManagement()));
        gridPanel.add(createActionCard("Báo cáo spam",
                "Xem danh sách báo cáo spam", ZALO_BLUE, "notification", e -> openSpamReport()));
        gridPanel.add(createActionCard("Người dùng mới",
                "Xem danh sách người dùng đăng ký mới", ZALO_BLUE, "add", e -> openNewUserReport()));
        gridPanel.add(createActionCard("Thống kê",
                "Biểu đồ số lượng người đăng ký mới theo năm", ZALO_BLUE, "chart", e -> openStatistics()));
        gridPanel.add(createActionCard("Bạn bè",
                "Xem danh sách người dùng và số lượng bạn bè", ZALO_BLUE, "friends", e -> openFriendStats()));
        gridPanel.add(createActionCard("Người hoạt động",
                "Xem danh sách người dùng hoạt động", ZALO_BLUE, "dashboard", e -> openActiveUserReport()));
        gridPanel.add(createActionCard("Biểu đồ",
                "Biểu đồ số lượng người hoạt động theo năm", ZALO_BLUE, "chart", e -> openActiveUserChart()));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionCard(String title, String description, Color color,
            String iconName, java.awt.event.ActionListener action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(18, 15, 18, 15)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon and title in horizontal layout - đảm bảo text và icon hiển thị đầy đủ
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Load icon
        ImageIcon icon = loadIcon(iconName, 20, 20);
        int iconWidth = 0;
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconWidth = icon.getIconWidth();
            headerPanel.add(iconLabel);
        }

        // Title label với size constraints để đảm bảo text không bị cắt
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(color);
        // Tính toán kích thước text để đảm bảo không bị cắt
        java.awt.FontMetrics fm = titleLabel.getFontMetrics(titleLabel.getFont());
        int textWidth = fm.stringWidth(title);
        // Thêm padding để đảm bảo text không bị cắt do font rendering
        int padding = 30;
        int totalWidth = textWidth + padding;
        // Set minimum và preferred size để đảm bảo text hiển thị đầy đủ
        titleLabel.setMinimumSize(new Dimension(totalWidth, 25));
        titleLabel.setPreferredSize(new Dimension(totalWidth, 25));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        // Đảm bảo text alignment
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        // Đảm bảo headerPanel có đủ không gian để hiển thị text và icon đầy đủ
        int headerMinWidth = iconWidth + 8 + totalWidth; // icon + gap + text
        headerPanel.setMinimumSize(new Dimension(headerMinWidth, 30));
        headerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(headerPanel);
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
                        new EmptyBorder(18, 15, 18, 15)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        new EmptyBorder(18, 15, 18, 15)));
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
        ImageIcon menuIcon = loadIcon("settings", 16, 16);
        JMenu userMenu = new JMenu("Lựa chọn chức năng");
        if (menuIcon != null) {
            userMenu.setIcon(menuIcon);
        }

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

        // 1. HEADER PANEL TỔNG
        JPanel titleHeaderPanel = new JPanel(new BorderLayout());
        titleHeaderPanel.setBackground(Color.WHITE);
        titleHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, ZALO_BLUE),
                new EmptyBorder(15, 20, 15, 20)));

        // 2. CONTAINER CHỨA TITLE (Dùng BoxLayout để xếp Icon và Text nằm ngang)
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.X_AXIS));
        titleContainer.setOpaque(false);

        // --- XỬ LÝ ICON (LABEL RIÊNG) ---
        String iconName = getIconNameForTitle(title);
        ImageIcon titleIcon = loadIcon(iconName, 24, 24);
        int iconWidth = (titleIcon != null) ? titleIcon.getIconWidth() : 0;
        int gap = (titleIcon != null) ? 15 : 10;

        if (titleIcon != null) {
            JLabel iconLabel = new JLabel(titleIcon);
            titleContainer.add(iconLabel);
            // Tạo khoảng cách giữa Icon và Text
            titleContainer.add(Box.createRigidArea(new Dimension(gap, 0)));
        } else {
            // Fallback emoji nếu không có icon
            JLabel emojiLabel = new JLabel(getEmojiForTitle(title));
            emojiLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleContainer.add(emojiLabel);
            titleContainer.add(Box.createRigidArea(new Dimension(gap, 0)));
        }

        // --- XỬ LÝ TEXT (LABEL RIÊNG) ---
        JLabel textLabel = new JLabel(title);
        textLabel.setFont(new Font("Arial", Font.BOLD, 24));
        textLabel.setForeground(ZALO_BLUE);
        // Đảm bảo text không bị cắt - tính toán kích thước dựa trên text
        java.awt.FontMetrics fm = textLabel.getFontMetrics(textLabel.getFont());
        int textWidth = fm.stringWidth(title);
        // Thêm padding để đảm bảo text không bị cắt do font rendering
        int padding = 50;
        int totalWidth = textWidth + padding;
        // Set minimum và preferred size để đảm bảo text hiển thị đầy đủ
        textLabel.setMinimumSize(new Dimension(totalWidth, 35));
        textLabel.setPreferredSize(new Dimension(totalWidth, 35));
        textLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        // Đảm bảo text alignment
        textLabel.setHorizontalAlignment(SwingConstants.LEFT);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);

        titleContainer.add(textLabel);

        // Đẩy toàn bộ nội dung sang trái, phần thừa bên phải lấp đầy bằng Glue
        titleContainer.add(Box.createHorizontalGlue());

        // Đảm bảo titleContainer có đủ không gian để hiển thị text đầy đủ
        // Tính toán tổng width cần thiết: icon + gap + text + padding
        int containerMinWidth = iconWidth + gap + totalWidth;
        titleContainer.setMinimumSize(new Dimension(containerMinWidth, 45));
        titleContainer.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));

        // Add container vào Header
        titleHeaderPanel.add(titleContainer, BorderLayout.CENTER);
        wrapper.add(titleHeaderPanel, BorderLayout.NORTH);

        // 3. NỘI DUNG CHÍNH (GIỮ NGUYÊN)
        wrapper.add(panel, BorderLayout.CENTER);

        // 4. NÚT QUAY LẠI (GIỮ NGUYÊN)
        ImageIcon homeIcon = loadIcon("home", 16, 16);
        JButton backBtn;
        if (homeIcon != null) {
            backBtn = new JButton("Quay lại trang chủ", homeIcon);
            backBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
            backBtn.setIconTextGap(8);
        } else {
            backBtn = new JButton("Quay lại trang chủ");
        }
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.setMargin(new Insets(8, 15, 8, 15));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showHomePage());

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getIconNameForTitle(String title) {
        if (title.contains("người dùng"))
            return "user";
        if (title.contains("lịch sử"))
            return "history";
        if (title.contains("nhóm"))
            return "chat";
        if (title.contains("spam"))
            return "notification";
        if (title.contains("mới"))
            return "new";
        if (title.contains("Thống kê"))
            return "statistics";
        if (title.contains("bạn bè"))
            return "friend";
        if (title.contains("hoạt động"))
            return "dashboard";
        if (title.contains("Biểu đồ"))
            return "chart";
        return "default";
    }

    private String getEmojiForTitle(String title) {
        if (title.contains("người dùng"))
            return "👤";
        if (title.contains("lịch sử"))
            return "📜";
        if (title.contains("nhóm"))
            return "👥";
        if (title.contains("spam"))
            return "🔔";
        if (title.contains("mới"))
            return "🆕";
        if (title.contains("Thống kê"))
            return "📊";
        if (title.contains("bạn bè"))
            return "💝";
        if (title.contains("hoạt động"))
            return "📈";
        if (title.contains("Biểu đồ"))
            return "📉";
        return "📋";
    }

    private JPanel createErrorPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(
                "<html><center>" + message + "<br><br>Vui lòng tạo file class tương ứng</center></html>");
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.RED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdminMainFrame().setVisible(true);
        });
    }
}
