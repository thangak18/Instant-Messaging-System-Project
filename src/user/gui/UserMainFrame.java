// package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính của phân hệ người dùng - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class UserMainFrame extends JFrame {
    private JMenuBar menuBar;
    private JDesktopPane desktopPane;
    private JLabel statusLabel;
    
    public UserMainFrame() {
        initializeComponents();
        setupLayout();
        setupMenu();
    }
    
    private void initializeComponents() {
        setTitle("Chat System - Người dùng (Phiên bản 1)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        menuBar = new JMenuBar();
        desktopPane = new JDesktopPane();
        statusLabel = new JLabel("Trạng thái: Đang online");
    }
    
    private void setupLayout() {
        setJMenuBar(menuBar);
        add(desktopPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupMenu() {
        // Menu Tài khoản
        JMenu accountMenu = new JMenu("Tài khoản");
        accountMenu.add(new JMenuItem("Cập nhật thông tin"));
        accountMenu.add(new JMenuItem("Đổi mật khẩu"));
        accountMenu.add(new JMenuItem("Khởi tạo lại mật khẩu"));
        accountMenu.addSeparator();
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        accountMenu.add(logoutMenuItem);
        
        // Menu Bạn bè
        JMenu friendsMenu = new JMenu("Bạn bè");
        JMenuItem friendsListMenuItem = new JMenuItem("Danh sách bạn bè");
        friendsMenu.add(friendsListMenuItem);
        friendsMenu.add(new JMenuItem("Yêu cầu kết bạn"));
        friendsMenu.add(new JMenuItem("Tìm kiếm bạn bè"));
        friendsMenu.add(new JMenuItem("Bạn bè online"));
        friendsMenu.add(new JMenuItem("Hủy kết bạn"));
        friendsMenu.add(new JMenuItem("Block tài khoản"));
        
        // Menu Chat
        JMenu chatMenu = new JMenu("Chat");
        JMenuItem privateChatMenuItem = new JMenuItem("Chat riêng");
        JMenuItem groupChatMenuItem = new JMenuItem("Nhóm chat");
        chatMenu.add(privateChatMenuItem);
        chatMenu.add(groupChatMenuItem);
        chatMenu.add(new JMenuItem("Lịch sử chat"));
        chatMenu.add(new JMenuItem("Tìm kiếm tin nhắn"));
        chatMenu.add(new JMenuItem("Xóa lịch sử chat"));
        
        // Menu Nhóm
        JMenu groupMenu = new JMenu("Nhóm");
        groupMenu.add(new JMenuItem("Tạo nhóm"));
        groupMenu.add(new JMenuItem("Danh sách nhóm"));
        groupMenu.add(new JMenuItem("Quản lý nhóm"));
        groupMenu.add(new JMenuItem("Thêm thành viên"));
        groupMenu.add(new JMenuItem("Gán quyền admin"));
        groupMenu.add(new JMenuItem("Xóa thành viên"));
        
        // Menu Báo cáo
        JMenu reportMenu = new JMenu("Báo cáo");
        reportMenu.add(new JMenuItem("Báo cáo spam"));
        
        // Thêm event handlers
        friendsListMenuItem.addActionListener(e -> openFriendsList());
        privateChatMenuItem.addActionListener(e -> openPrivateChat());
        groupChatMenuItem.addActionListener(e -> openGroupChat());
        logoutMenuItem.addActionListener(e -> System.exit(0));
        
        menuBar.add(accountMenu);
        menuBar.add(friendsMenu);
        menuBar.add(chatMenu);
        menuBar.add(groupMenu);
        menuBar.add(reportMenu);
    }
    
    private void openFriendsList() {
        FriendsListFrame frame = new FriendsListFrame();
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openPrivateChat() {
        ChatFrame frame = new ChatFrame("user1");
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private void openGroupChat() {
        GroupChatFrame frame = new GroupChatFrame("Nhóm bạn thân");
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
            new UserMainFrame().setVisible(true);
        });
    }
}
