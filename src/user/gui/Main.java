package user.gui;

import user.socket.ChatServer;

import javax.swing.*;

/**
 * Main Entry Point - Khởi động ChatServer với thông báo hiển thị khi double-click server.jar
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  INSTANT CHAT SERVER");
        System.out.println("=================================");

        try {
            ChatServer server = new ChatServer();

            Thread serverThread = new Thread(server::start, "ChatServer-Thread");
            serverThread.setDaemon(false);
            serverThread.start();

            String message = "ChatServer is running on port 8888.\n" +
                    "Keep this window open.\n" +
                    "Close this dialog to stop the server.";

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Server is running",
                    JOptionPane.INFORMATION_MESSAGE
            );

            System.out.println("🛑 Stopping server...");
            server.stop();
        } catch (Exception e) {
            System.err.println("❌ Could not start ChatServer: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Could not start server: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
