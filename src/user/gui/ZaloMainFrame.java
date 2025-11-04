package user.gui;

import user.socket.SocketClient;
import user.socket.Message;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính theo phong cách Zalo
 * Full screen với sidebar, chat list, và chat content
 */
public class ZaloMainFrame extends JFrame {
    
    // Colors - Zalo theme
    private static final Color SIDEBAR_COLOR = new Color(0, 132, 255);
    private static final Color CHAT_LIST_BG = new Color(250, 250, 250);
    private static final Color CHAT_CONTENT_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(0, 132, 255);
    
    // Components
    private SidebarPanel sidebarPanel;
    private ChatListPanel chatListPanel;
    private ContactPanel contactPanel;
    private ChatContentPanel chatContentPanel;
    
    // Panel switching
    private JPanel leftPanel; // CardLayout container for chatList and contactPanel
    private CardLayout leftCardLayout;
    private JPanel rightPanel; // CardLayout container for chatContent and contact content
    private CardLayout rightCardLayout;
    
    // User info
    private String username;
    private SocketClient socketClient;
    
    public ZaloMainFrame(String username) {
        this.username = username;
        initializeComponents();
        setupLayout();
        initializeSocket();
    }
    
    private void initializeComponents() {
        setTitle("Zalo - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        
        // Create panels
        sidebarPanel = new SidebarPanel(this);
        chatListPanel = new ChatListPanel(this);
        contactPanel = new ContactPanel(this);
        chatContentPanel = new ChatContentPanel(this);
        
        // CardLayout for switching between chatList and contactPanel (LEFT side)
        leftCardLayout = new CardLayout();
        leftPanel = new JPanel(leftCardLayout);
        leftPanel.add(chatListPanel, "CHAT");
        leftPanel.add(contactPanel, "CONTACT");
        
        // CardLayout for switching content panels (RIGHT side)
        rightCardLayout = new CardLayout();
        rightPanel = new JPanel(rightCardLayout);
        rightPanel.add(chatContentPanel, "CHAT_CONTENT");
        rightPanel.add(new FriendListPanel(this), "FRIENDS");
        rightPanel.add(createPlaceholderPanel("Danh sách nhóm"), "GROUPS");
        rightPanel.add(new FriendRequestPanel(this), "FRIEND_REQUESTS");
        rightPanel.add(createPlaceholderPanel("Lời mời vào nhóm"), "GROUP_INVITES");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel với sidebar + content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Content panel với leftPanel (chatList or contact) + chat area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(leftPanel, BorderLayout.WEST); // CardLayout panel
        contentPanel.add(rightPanel, BorderLayout.CENTER); // CardLayout for content
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private void initializeSocket() {
        socketClient = new SocketClient(username, this::handleIncomingMessage);
        
        new Thread(() -> {
            boolean connected = socketClient.connect();
            if (!connected) {
                System.err.println("⚠️ Không thể kết nối đến chat server. Socket features sẽ bị tắt.");
                // Không show error dialog - vẫn có thể dùng được các features khác
            } else {
                System.out.println("✅ Connected to chat server!");
            }
        }).start();
    }
    
    private void handleIncomingMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatContentPanel.handleMessage(message);
            chatListPanel.updateChatList(message);
        });
    }
    
    public void sendMessage(String content, String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            System.out.println("📤 Gửi tin nhắn đến " + receiver + ": " + content);
            if (receiver == null) {
                socketClient.sendChatMessage(content);
            } else {
                socketClient.sendPrivateMessage(receiver, content);
            }
        } else {
            System.err.println("❌ Socket chưa kết nối! Không thể gửi tin nhắn.");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Không thể gửi tin nhắn!\nVui lòng kiểm tra kết nối server.",
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    public void openChat(String contactName) {
        chatContentPanel.openChat(contactName);
    }
    
    public String getUsername() {
        return username;
    }
    
    public SocketClient getSocketClient() {
        return socketClient;
    }
    
    /**
     * SWITCH ĐẾN CHAT LIST PANEL
     */
    public void showChatPanel() {
        leftCardLayout.show(leftPanel, "CHAT");
        rightCardLayout.show(rightPanel, "CHAT_CONTENT");
    }
    
    /**
     * SWITCH ĐẾN CONTACT PANEL
     */
    public void showContactPanel() {
        leftCardLayout.show(leftPanel, "CONTACT");
        // Default: show friend requests
        rightCardLayout.show(rightPanel, "FRIEND_REQUESTS");
    }
    
    /**
     * SWITCH NỘI DUNG BÊN PHẢI KHI CLICK MENU TRONG CONTACT PANEL
     */
    public void showContactContent(String contentKey) {
        rightCardLayout.show(rightPanel, contentKey);
    }
    
    /**
     * TẠO PLACEHOLDER PANEL
     */
    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(150, 150, 150));
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    @Override
    public void dispose() {
        if (socketClient != null) {
            socketClient.disconnect();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog(
                null,
                "Nhập tên của bạn:",
                "Đăng nhập Zalo",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (username != null && !username.trim().isEmpty()) {
                ZaloMainFrame frame = new ZaloMainFrame(username.trim());
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
