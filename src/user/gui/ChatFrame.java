// package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chat giữa 2 người dùng - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class ChatFrame extends JInternalFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, searchButton, clearButton, deleteButton;
    private JList<String> messageList;
    private String currentChatUser;
    
    public ChatFrame(String chatUser) {
        this.currentChatUser = chatUser;
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Chat với " + currentChatUser);
        setSize(600, 500);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        
        // Vùng hiển thị chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        chatArea.setBackground(Color.WHITE);
        
        // Vùng nhập tin nhắn
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Các nút chức năng
        sendButton = new JButton("Gửi");
        searchButton = new JButton("Tìm kiếm");
        clearButton = new JButton("Xóa lịch sử");
        deleteButton = new JButton("Xóa tin nhắn");
        
        // Danh sách tin nhắn (để tìm kiếm)
        messageList = new JList<>();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel chat chính
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        // Panel nhập tin nhắn
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Panel chức năng
        JPanel functionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        functionPanel.add(searchButton);
        functionPanel.add(clearButton);
        functionPanel.add(deleteButton);
        
        add(chatPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(functionPanel, BorderLayout.NORTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        chatArea.append("Bạn: Chào bạn!\n");
        chatArea.append(currentChatUser + ": Chào bạn! Bạn khỏe không?\n");
        chatArea.append("Bạn: Mình khỏe, cảm ơn bạn!\n");
        chatArea.append(currentChatUser + ": Hôm nay thế nào?\n");
        chatArea.append("Bạn: Tốt lắm, cảm ơn bạn!\n");
        chatArea.append(currentChatUser + ": Có gì mới không?\n");
        chatArea.append("Bạn: Không có gì đặc biệt\n");
    }
}
