// package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chat nhóm - PHIÊN BẢN 1
 * Chỉ có giao diện, chưa có logic xử lý
 */
public class GroupChatFrame extends JInternalFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, searchButton, clearButton, addMemberButton, manageGroupButton;
    private JList<String> memberList;
    private String currentGroupName;
    
    public GroupChatFrame(String groupName) {
        this.currentGroupName = groupName;
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    private void initializeComponents() {
        setTitle("Nhóm: " + currentGroupName);
        setSize(800, 600);
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
        addMemberButton = new JButton("Thêm thành viên");
        manageGroupButton = new JButton("Quản lý nhóm");
        
        // Danh sách thành viên
        memberList = new JList<>();
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
        functionPanel.add(addMemberButton);
        functionPanel.add(manageGroupButton);
        
        // Panel thành viên (bên phải)
        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.setBorder(BorderFactory.createTitledBorder("Thành viên"));
        memberPanel.add(new JScrollPane(memberList), BorderLayout.CENTER);
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(memberPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(functionPanel, BorderLayout.NORTH);
    }
    
    private void loadSampleData() {
        // Dữ liệu mẫu cho phiên bản 1
        chatArea.append("user1: Chào mọi người!\n");
        chatArea.append("user2: Chào bạn!\n");
        chatArea.append("user3: Chào cả nhóm!\n");
        chatArea.append("user1: Hôm nay thế nào?\n");
        chatArea.append("user2: Tốt lắm, cảm ơn bạn!\n");
        chatArea.append("user3: Có gì mới không?\n");
        
        // Danh sách thành viên mẫu
        String[] members = {"user1 (Admin)", "user2", "user3", "user4"};
        memberList.setListData(members);
    }
}
