// package admin.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính của phân hệ quản trị - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class AdminMainFrame extends JFrame {
    private JMenuBar menuBar;
    private JPanel contentPanel;
    private JLabel statusLabel;

    // Thêm biến trang chủ
    private JPanel homePanel;

    public AdminMainFrame() {
        initializeComponents();
        setupLayout();
        setupMenu();
        showHomePage(); // Hiển thị trang chủ khi khởi động
    }

    // Thêm hàm tạo giao diện trang chủ với các nút chức năng
    private void showHomePage() {
        contentPanel.removeAll();

        homePanel = new JPanel();
        homePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Tiêu đề trang chủ
        JLabel titleLabel = new JLabel("Trang chủ quản trị hệ thống chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 255));
        gbc.gridx = 0; gbc.gridy = 0;
        homePanel.add(titleLabel, gbc);

        // Nút lựa chọn chức năng
        JButton userBtn = new JButton("Quản lý người dùng");
        JButton historyBtn = new JButton("Xem lịch sử đăng nhập");
        JButton groupBtn = new JButton("Xem danh sách nhóm chat");
        JButton spamBtn = new JButton("Xem danh sách báo cáo spam");

        userBtn.setFont(new Font("Arial", Font.BOLD, 18));
        historyBtn.setFont(new Font("Arial", Font.BOLD, 18));
        groupBtn.setFont(new Font("Arial", Font.BOLD, 18));
        spamBtn.setFont(new Font("Arial", Font.BOLD, 18));

        userBtn.setBackground(new Color(0, 102, 255));
        userBtn.setForeground(Color.BLACK);
        historyBtn.setBackground(new Color(0, 102, 255));
        historyBtn.setForeground(Color.BLACK);
        groupBtn.setBackground(new Color(0, 102, 255));
        groupBtn.setForeground(Color.BLACK);
        spamBtn.setBackground(new Color(0, 102, 255));
        spamBtn.setForeground(Color.BLACK);

        userBtn.setFocusPainted(false);
        historyBtn.setFocusPainted(false);
        groupBtn.setFocusPainted(false);
        spamBtn.setFocusPainted(false);

        userBtn.addActionListener(e -> openUserManagement());
        historyBtn.addActionListener(e -> openLoginHistory());
        groupBtn.addActionListener(e -> openGroupManagement());
        spamBtn.addActionListener(e -> openSpamReport());

        gbc.gridx = 0; gbc.gridy = 1;
        homePanel.add(userBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        homePanel.add(historyBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        homePanel.add(groupBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 4;
        homePanel.add(spamBtn, gbc);

        contentPanel.add(homePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void initializeComponents() {
        setTitle("Hệ thống quản trị - Chat System (Phiên bản 1)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        menuBar = new JMenuBar();
        contentPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Trạng thái: Sẵn sàng");
    }

    private void setupLayout() {
        setJMenuBar(menuBar);
        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupMenu() {
        // Menu Quản lý người dùng
        JMenu userMenu = new JMenu("Lựa chọn chức năng");
        JMenuItem userListMenuItem = new JMenuItem("Quản lý người dùng");
        JMenuItem loginHistoryMenuItem = new JMenuItem("Lịch sử đăng nhập");
        JMenuItem groupListMenuItem = new JMenuItem("Xem danh sách nhóm chat");
        JMenuItem spamReportMenuItem = new JMenuItem("Xem danh sách báo cáo spam");
        userMenu.add(userListMenuItem);
        // userMenu.add(new JMenuItem("Thêm người dùng"));
        // userMenu.add(new JMenuItem("Cập nhật người dùng"));
        // userMenu.add(new JMenuItem("Xóa người dùng"));
        // userMenu.addSeparator();
        // userMenu.add(new JMenuItem("Khóa/Mở khóa tài khoản"));
        // userMenu.add(new JMenuItem("Cập nhật mật khẩu"));
        // userMenu.add(loginHistoryMenuItem);
        // userMenu.add(new JMenuItem("Danh sách bạn bè"));
        userMenu.add(loginHistoryMenuItem);
        userMenu.add(groupListMenuItem);
        userMenu.add(spamReportMenuItem);

        // Thêm event handler cho chức năng quản lý người dùng (quan trọng)
        userListMenuItem.addActionListener(e -> openUserManagement());

        loginHistoryMenuItem.addActionListener(e-> openLoginHistory());

        groupListMenuItem.addActionListener(e-> openGroupManagement());

        spamReportMenuItem.addActionListener(e-> openSpamReport());
        
        // // Menu Quản lý nhóm
        //JMenu groupMenu = new JMenu("Quản lý nhóm");
        //JMenuItem groupListMenuItem = new JMenuItem("Danh sách nhóm chat");
        //groupMenu.add(groupListMenuItem);
        // groupMenu.add(new JMenuItem("Thành viên nhóm"));
        // groupMenu.add(new JMenuItem("Admin nhóm"));
        
        // // Menu Báo cáo
        // JMenu reportMenu = new JMenu("Báo cáo");
        // JMenuItem spamReportMenuItem = new JMenuItem("Báo cáo spam");
        // reportMenu.add(spamReportMenuItem);
        // reportMenu.add(new JMenuItem("Người dùng mới"));
        // reportMenu.add(new JMenuItem("Người dùng hoạt động"));
        // reportMenu.add(new JMenuItem("Thống kê bạn bè"));
        
        // // Menu Thống kê
        // JMenu statsMenu = new JMenu("Thống kê");
        // JMenuItem statisticsMenuItem = new JMenuItem("Biểu đồ đăng ký theo năm");
        // statsMenu.add(statisticsMenuItem);
        // statsMenu.add(new JMenuItem("Biểu đồ hoạt động theo năm"));
        
        // // Menu Hệ thống
        // JMenu systemMenu = new JMenu("Hệ thống");
        // systemMenu.add(new JMenuItem("Đăng xuất"));
        // JMenuItem exitMenuItem = new JMenuItem("Thoát");
        // systemMenu.add(exitMenuItem);
        
        // // Thêm event handlers
        // userListMenuItem.addActionListener(e -> openUserManagement());
        // loginHistoryMenuItem.addActionListener(e -> openLoginHistory());
        // groupListMenuItem.addActionListener(e -> openGroupManagement());
        // spamReportMenuItem.addActionListener(e -> openSpamReport());
        // statisticsMenuItem.addActionListener(e -> openStatistics());
        // exitMenuItem.addActionListener(e -> System.exit(0));
        
         menuBar.add(userMenu);
        // menuBar.add(groupMenu);
        // menuBar.add(reportMenu);
        // menuBar.add(statsMenu);
        // menuBar.add(systemMenu);
    }
    

    //Hàm này giải quyết giao diện quản lý người dùng
    private void openUserManagement() {
        contentPanel.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        // Tiêu đề trang
        JLabel titleLabel = new JLabel("Quản lý người dùng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        UserManagementPanel userPanel = new UserManagementPanel();
        wrapper.add(userPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backBtn = new JButton("Quay lại trang chủ");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> showHomePage());
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    //Hàm này giải quyết giao diện lịch sử đăng nhập
    private void openLoginHistory() {
        contentPanel.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        // Tiêu đề trang
        JLabel titleLabel = new JLabel("Lịch sử đăng nhập");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        LoginHistoryPanel historyPanel = new LoginHistoryPanel();
        wrapper.add(historyPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backBtn = new JButton("Quay lại trang chủ");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> showHomePage());
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void openGroupManagement() {
        contentPanel.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        // Tiêu đề trang
        JLabel titleLabel = new JLabel("Quản lý nhóm chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        GroupManagementPanel groupPanel = new GroupManagementPanel();
        wrapper.add(groupPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backBtn = new JButton("Quay lại trang chủ");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> showHomePage());
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void openSpamReport() {
        contentPanel.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        // Tiêu đề trang
        JLabel titleLabel = new JLabel("Xem danh sách báo cáo spam");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        SpamReportPanel spamPanel = new SpamReportPanel();
        wrapper.add(spamPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backBtn = new JButton("Quay lại trang chủ");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> showHomePage());
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backBtn);
        wrapper.add(backPanel, BorderLayout.SOUTH);

        contentPanel.add(wrapper, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // private open newUserReport() {
    //     contentPanel.removeAll();
    //     JPanel wrapper = new JPanel(new BorderLayout());
    //     // Tiêu đề trang
    //     JLabel titleLabel = new JLabel("Xem danh sách người dùng mới");
    //     titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
    //     titleLabel.setForeground(new Color(0, 102, 255));
    //     titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    //     wrapper.add(titleLabel, BorderLayout.NORTH);

    //     NewUserReportPanel newUserPanel = new NewUserReportPanel();
    //     wrapper.add(newUserPanel, BorderLayout.CENTER);

    //     // Nút quay lại
    //     JButton backBtn = new JButton("Quay lại trang chủ");
    //     backBtn.setFont(new Font("Arial", Font.BOLD, 14));
    //     backBtn.setBackground(new Color(108, 117, 125));
    //     backBtn.setForeground(Color.BLACK);
    //     backBtn.setFocusPainted(false);
    //     backBtn.addActionListener(e -> showHomePage());
    //     JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    //     backPanel.add(backBtn);
    //     wrapper.add(backPanel, BorderLayout.SOUTH);

    //     contentPanel.add(wrapper, BorderLayout.CENTER);
    //     contentPanel.revalidate();
    //     contentPanel.repaint();
    // }
    
    // private void openStatistics() {
    //     StatisticsFrame frame = new StatisticsFrame();
    //     desktopPane.add(frame);
    //     frame.setVisible(true);
    // }
    
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
