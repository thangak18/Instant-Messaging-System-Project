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
    private FriendRequestPanel friendRequestPanel; // Lưu reference để refresh
    private FriendListPanel friendListPanel; // Lưu reference để refresh online status
    private GroupListPanel groupListPanel; // Lưu reference để refresh groups
    private GroupChatPanel currentGroupChatPanel; // Lưu reference group chat hiện tại để handle realtime messages
    private int currentGroupId = -1; // ID nhóm đang mở
    
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
        
        // Tạo và lưu reference FriendListPanel
        friendListPanel = new FriendListPanel(this);
        rightPanel.add(friendListPanel, "FRIENDS");
        
        // Tạo và lưu reference GroupListPanel
        groupListPanel = new GroupListPanel(this);
        rightPanel.add(groupListPanel, "GROUPS");
        
        // Tạo và lưu reference FriendRequestPanel
        friendRequestPanel = new FriendRequestPanel(this);
        rightPanel.add(friendRequestPanel, "FRIEND_REQUESTS");
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
            // Handle chat messages
            if (message.getType() == Message.MessageType.PRIVATE_MESSAGE ||
                message.getType() == Message.MessageType.BROADCAST) {
                chatContentPanel.handleMessage(message);
                chatListPanel.updateChatList(message);
            }
            
            // Handle user online/offline status
            else if (message.getType() == Message.MessageType.USER_JOINED ||
                     message.getType() == Message.MessageType.USER_LEFT ||
                     message.getType() == Message.MessageType.ONLINE_USERS) {
                // Refresh online status in ChatContentPanel
                chatContentPanel.refreshOnlineStatus();
                // Refresh FriendListPanel
                if (friendListPanel != null) {
                    friendListPanel.refreshOnlineStatus();
                }
                // ✅ CẬP NHẬT ONLINE STATUS TRONG CHAT LIST
                if (chatListPanel != null && socketClient != null) {
                    chatListPanel.updateOnlineUsers(socketClient.getOnlineUsers());
                }
            }
            
            // Handle friend request notifications
            else if (message.getType() == Message.MessageType.FRIEND_REQUEST_SENT) {
                System.out.println("🔔 Nhận thông báo lời mời kết bạn từ: " + message.getSender());
                // Reload friend requests panel
                if (friendRequestPanel != null) {
                    friendRequestPanel.refreshFriendRequests();
                }
            }
            else if (message.getType() == Message.MessageType.FRIEND_REQUEST_ACCEPTED) {
                System.out.println("✅ Lời mời kết bạn được chấp nhận: " + message.getContent());
                if (friendRequestPanel != null) {
                    friendRequestPanel.refreshFriendRequests();
                }
                // Refresh chat list và friend list ngay lập tức
                if (chatListPanel != null) {
                    chatListPanel.refreshChatList();
                }
                if (friendListPanel != null) {
                    friendListPanel.refreshFriendList();
                }
            }
            else if (message.getType() == Message.MessageType.FRIEND_REQUEST_REJECTED) {
                System.out.println("❌ Lời mời kết bạn bị từ chối: " + message.getContent());
                if (friendRequestPanel != null) {
                    friendRequestPanel.refreshFriendRequests();
                }
            }
            else if (message.getType() == Message.MessageType.FRIEND_REQUEST_RECALLED) {
                System.out.println("↩️ Lời mời kết bạn bị thu hồi: " + message.getContent());
                if (friendRequestPanel != null) {
                    friendRequestPanel.refreshFriendRequests();
                }
            }
            
            // Handle unfriend notification
            else if (message.getType() == Message.MessageType.UNFRIEND) {
                System.out.println("💔 Bị hủy kết bạn: " + message.getContent());
                // Refresh chat list và friend list
                if (chatListPanel != null) {
                    chatListPanel.refreshChatList();
                }
                if (friendListPanel != null) {
                    friendListPanel.refreshFriendList();
                }
                // Show notification
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        message.getSender() + " đã hủy kết bạn với bạn",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            // Handle block notification
            else if (message.getType() == Message.MessageType.BLOCK) {
                System.out.println("🚫 Bị chặn: " + message.getContent());
                // Refresh chat list và friend list
                if (chatListPanel != null) {
                    chatListPanel.refreshChatList();
                }
                if (friendListPanel != null) {
                    friendListPanel.refreshFriendList();
                }
                // Show notification
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        message.getSender() + " đã chặn bạn",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                });
            }
            
            // Handle group message realtime
            else if (message.getType() == Message.MessageType.GROUP_MESSAGE) {
                int groupId = (Integer) message.getData();
                System.out.println("📨 Nhận tin nhắn nhóm " + groupId + " từ: " + message.getSender());
                
                // Nếu đang mở đúng group chat này thì refresh
                if (currentGroupChatPanel != null && currentGroupId == groupId) {
                    currentGroupChatPanel.handleIncomingMessage(message);
                }
            }
            
            // Handle group created notification - refresh danh sách nhóm
            else if (message.getType() == Message.MessageType.GROUP_CREATED) {
                String groupName = message.getContent();
                String creator = message.getSender();
                System.out.println("📨 Nhận thông báo nhóm mới: " + groupName + " từ " + creator);
                
                // Refresh group list để hiển thị nhóm mới
                if (groupListPanel != null) {
                    groupListPanel.refreshGroupList();
                }
                
                // Hiển thị thông báo
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        creator + " đã thêm bạn vào nhóm \"" + groupName + "\"",
                        "Nhóm mới",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });
    }
    
    /**
     * Gửi notification lời mời kết bạn
     */
    public void sendFriendRequestNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.FRIEND_REQUEST_SENT, username, receiver, 
                username + " đã gửi lời mời kết bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification lời mời kết bạn đến: " + receiver);
        }
    }
    
    /**
     * Gửi notification chấp nhận lời mời
     */
    public void sendFriendRequestAcceptedNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.FRIEND_REQUEST_ACCEPTED, username, receiver,
                username + " đã chấp nhận lời mời kết bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification chấp nhận lời mời đến: " + receiver);
        }
    }
    
    /**
     * Gửi notification từ chối lời mời
     */
    public void sendFriendRequestRejectedNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.FRIEND_REQUEST_REJECTED, username, receiver,
                username + " đã từ chối lời mời kết bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification từ chối lời mời đến: " + receiver);
        }
    }
    
    /**
     * Gửi notification thu hồi lời mời
     */
    public void sendFriendRequestRecalledNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.FRIEND_REQUEST_RECALLED, username, receiver,
                username + " đã thu hồi lời mời kết bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification thu hồi lời mời đến: " + receiver);
        }
    }
    
    /**
     * Gửi notification hủy kết bạn
     */
    public void sendUnfriendNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.UNFRIEND, username, receiver,
                username + " đã hủy kết bạn với bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification hủy kết bạn đến: " + receiver);
        }
    }
    
    /**
     * Gửi notification chặn user
     */
    public void sendBlockNotification(String receiver) {
        if (socketClient != null && socketClient.isConnected()) {
            Message msg = new Message(Message.MessageType.BLOCK, username, receiver,
                username + " đã chặn bạn");
            socketClient.sendMessage(msg);
            System.out.println("📤 Gửi notification chặn user đến: " + receiver);
        }
    }
    
    /**
     * Refresh FriendRequestPanel
     */
    public void refreshFriendRequestPanel() {
        if (friendRequestPanel != null) {
            friendRequestPanel.refreshFriendRequests();
        }
    }
    
    /**
     * Refresh chat list và friend list (gọi khi User A chấp nhận lời mời)
     */
    public void refreshChatAndFriendList() {
        System.out.println("🔄 Refreshing chat list and friend list for current user...");
        if (chatListPanel != null) {
            chatListPanel.refreshChatList();
        }
        if (friendListPanel != null) {
            friendListPanel.refreshFriendList();
        }
    }
    
    /**
     * Refresh chat list (gọi khi gửi tin nhắn)
     */
    public void refreshChatList() {
        if (chatListPanel != null) {
            chatListPanel.refreshChatList();
        }
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
    
    /**
     * MỞ CHAT VÀ CUỘN ĐẾN TIN NHẮN CỤ THỂ
     * @param contactName Tên người chat
     * @param messageId ID tin nhắn cần cuộn đến
     */
    public void openChatAndScrollToMessage(String contactName, int messageId) {
        chatContentPanel.openChat(contactName);
        
        // Đợi UI load xong rồi mới cuộn đến tin nhắn
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(500); // Đợi tin nhắn load xong
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chatContentPanel.scrollToMessage(messageId);
        });
    }
    
    /**
     * CUỘN ĐẾN TIN NHẮN CỤ THỂ TRONG CHAT HIỆN TẠI
     * @param messageId ID tin nhắn cần cuộn đến
     */
    public void scrollToMessageInChat(int messageId) {
        chatContentPanel.scrollToMessage(messageId);
    }
    
    /**
     * MỞ GROUP CHAT (NHÓM THƯỜNG)
     */
    public void openGroupChat(int groupId, String groupName, boolean isAdmin) {
        openGroupChat(groupId, groupName, isAdmin, false);
    }
    
    /**
     * MỞ GROUP CHAT VỚI TÙY CHỌN MÃ HÓA
     * @param groupId ID nhóm
     * @param groupName Tên nhóm
     * @param isAdmin Có phải admin không
     * @param isEncrypted Nhóm có mã hóa E2E không
     */
    public void openGroupChat(int groupId, String groupName, boolean isAdmin, boolean isEncrypted) {
        // Lưu lại groupId hiện tại
        this.currentGroupId = groupId;
        
        // Remove old GROUP_CHAT panel nếu tồn tại
        if (currentGroupChatPanel != null) {
            rightPanel.remove(currentGroupChatPanel);
        }
        
        // Tạo GroupChatPanel mới
        currentGroupChatPanel = new GroupChatPanel(this, groupId, groupName, isAdmin, isEncrypted);
        
        // Add new group chat panel
        rightPanel.add(currentGroupChatPanel, "GROUP_CHAT");
        
        // Revalidate để cập nhật layout
        rightPanel.revalidate();
        rightPanel.repaint();
        
        // Switch đến GROUP_CHAT
        rightCardLayout.show(rightPanel, "GROUP_CHAT");
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
     * SWITCH ĐẾN DANH SÁCH NHÓM VÀ REFRESH
     */
    public void showGroupList() {
        leftCardLayout.show(leftPanel, "CONTACT");
        rightCardLayout.show(rightPanel, "GROUPS");
        // Refresh danh sách nhóm
        if (groupListPanel != null) {
            groupListPanel.refreshGroupList();
        }
    }
    
    /**
     * REFRESH DANH SÁCH NHÓM (không switch view)
     */
    public void refreshGroupList() {
        if (groupListPanel != null) {
            groupListPanel.refreshGroupList();
        }
    }
    
    /**
     * SWITCH NỘI DUNG BÊN PHẢI KHI CLICK MENU TRONG CONTACT PANEL
     */
    public void showContactContent(String contentKey) {
        rightCardLayout.show(rightPanel, contentKey);
    }
    
    /**
     * SWITCH ĐẾN TAB (chat hoặc contact)
     */
    public void switchToTab(String tab) {
        if ("chat".equalsIgnoreCase(tab)) {
            showChatPanel();
        } else if ("contact".equalsIgnoreCase(tab)) {
            showContactPanel();
        }
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
