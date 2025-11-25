package user.gui;

import user.socket.ChatServer;

/**
 * Main Entry Point - Chỉ khởi động ChatServer
 * Để login, chạy LoginFrame.java
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  INSTANT CHAT SERVER");
        System.out.println("=================================");
        System.out.println("🚀 Starting ChatServer...");
        
        try {
            ChatServer server = new ChatServer();
            server.start(); // Block ở đây để server chạy
        } catch (Exception e) {
            System.err.println("❌ Could not start ChatServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
