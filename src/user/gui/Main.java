package user.gui;

import user.socket.ChatServer;

import javax.swing.*;

/**
 * Main Entry Point của ứng dụng InstantChat
 * Đây là class duy nhất có main() - khởi động từ LoginFrame
 */
public class Main {
    public static void main(String[] args) {
        // ========================================
        // KHỞI ĐỘNG CHAT SERVER TỰ ĐỘNG
        // ========================================
        System.out.println("=================================");
        System.out.println("  INSTANT CHAT APPLICATION");
        System.out.println("=================================");
        System.out.println("🚀 Starting ChatServer...");
        
        // Chạy ChatServer trong background thread
        Thread serverThread = new Thread(() -> {
            try {
                ChatServer server = new ChatServer();
                server.start(); // Sẽ block trong thread này
            } catch (Exception e) {
                System.err.println("❌ Could not start ChatServer: " + e.getMessage());
                e.printStackTrace();
            }
        }, "ChatServer-Thread");
        
        serverThread.setDaemon(true); // Daemon thread - không chặn JVM exit
        serverThread.start();
        
        // Đợi 1 giây để server khởi động
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("✅ ChatServer started successfully!");
        System.out.println("=================================");
        
        // ========================================
        // KHỞI ĐỘNG GUI
        // ========================================
        
        // Set Look and Feel cho đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Chạy trên Event Dispatch Thread (chuẩn Swing)
        SwingUtilities.invokeLater(() -> {
            System.out.println("  Starting Login Screen...");
            System.out.println("=================================");
            
            // Khởi động app từ LoginFrame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
