package user.gui;

import javax.swing.*;

/**
 * Main Entry Point của ứng dụng InstantChat
 * Đây là class duy nhất có main() - khởi động từ LoginFrame
 */
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel cho đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Chạy trên Event Dispatch Thread (chuẩn Swing)
        SwingUtilities.invokeLater(() -> {
            System.out.println("=================================");
            System.out.println("  INSTANT CHAT APPLICATION");
            System.out.println("  Starting from Login Screen...");
            System.out.println("=================================");
            
            // Khởi động app từ LoginFrame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
