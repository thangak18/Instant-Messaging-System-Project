// package admin.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính của phân hệ quản trị - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class AdminMainFrame extends JFrame {
    private JMenuBar menuBar;
    private JDesktopPane desktopPane;
    private JLabel statusLabel;
    
    public AdminMainFrame() {
        initializeComponents();
        setupLayout();
        setupMenu();
    }
    
    private void initializeComponents() {
        setTitle("Hệ thống quản trị - Chat System (Phiên bản 1)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        menuBar = new JMenuBar();
        desktopPane = new JDesktopPane();
        statusLabel = new JLabel("Trạng thái: Sẵn sàng");
    }
    
    private void setupLayout() {
        setJMenuBar(menuBar);
        add(desktopPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupMenu() {
        // Menu Quản lý người dùng
        JMenu userMenu = new JMenu("Quản lý người dùng");
        JMenuItem userListMenuItem = new JMenuItem("Danh sách người dùng");
        JMenuItem loginHistoryMenuItem = new JMenuItem("Lịch sử đăng nhập");
        userMenu.add(userListMenuItem);
        userMenu.add(new JMenuItem("Thêm người dùng"));
        userMenu.add(new JMenuItem("Cập nhật người dùng"));
        userMenu.add(new JMenuItem("Xóa người dùng"));
        userMenu.addSeparator();
        userMenu.add(new JMenuItem("Khóa/Mở khóa tài khoản"));
        userMenu.add(new JMenuItem("Cập nhật mật khẩu"));
        userMenu.add(loginHistoryMenuItem);
        userMenu.add(new JMenuItem("Danh sách bạn bè"));
        
        // Menu Quản lý nhóm
        JMenu groupMenu = new JMenu("Quản lý nhóm");
        JMenuItem groupListMenuItem = new JMenuItem("Danh sách nhóm chat");
        groupMenu.add(groupListMenuItem);
        groupMenu.add(new JMenuItem("Thành viên nhóm"));
        groupMenu.add(new JMenuItem("Admin nhóm"));
        
        // Menu Báo cáo
        JMenu reportMenu = new JMenu("Báo cáo");
        JMenuItem spamReportMenuItem = new JMenuItem("Báo cáo spam");
        reportMenu.add(spamReportMenuItem);
        reportMenu.add(new JMenuItem("Người dùng mới"));
        reportMenu.add(new JMenuItem("Người dùng hoạt động"));
        reportMenu.add(new JMenuItem("Thống kê bạn bè"));
        
        // Menu Thống kê
        JMenu statsMenu = new JMenu("Thống kê");
        JMenuItem statisticsMenuItem = new JMenuItem("Biểu đồ đăng ký theo năm");
        statsMenu.add(statisticsMenuItem);
        statsMenu.add(new JMenuItem("Biểu đồ hoạt động theo năm"));
        
        // Menu Hệ thống
        JMenu systemMenu = new JMenu("Hệ thống");
        systemMenu.add(new JMenuItem("Đăng xuất"));
        JMenuItem exitMenuItem = new JMenuItem("Thoát");
        systemMenu.add(exitMenuItem);
        
        // Thêm event handlers
        userListMenuItem.addActionListener(e -> openUserManagement());
        loginHistoryMenuItem.addActionListener(e -> openLoginHistory());
        groupListMenuItem.addActionListener(e -> openGroupManagement());
        spamReportMenuItem.addActionListener(e -> openSpamReport());
        statisticsMenuItem.addActionListener(e -> openStatistics());
        exitMenuItem.addActionListener(e -> System.exit(0));
        
        menuBar.add(userMenu);
        menuBar.add(groupMenu);
        menuBar.add(reportMenu);
        menuBar.add(statsMenu);
        menuBar.add(systemMenu);
    }
    
    private void openUserManagement() {
        UserManagementFrame frame = new UserManagementFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openLoginHistory() {
        LoginHistoryFrame frame = new LoginHistoryFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openGroupManagement() {
        GroupManagementFrame frame = new GroupManagementFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openSpamReport() {
        SpamReportFrame frame = new SpamReportFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openStatistics() {
        StatisticsFrame frame = new StatisticsFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
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
