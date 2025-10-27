package user.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test kết nối database
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  KIỂM TRA KẾT NỐI DATABASE - CHAT SYSTEM           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 1. Lấy instance DatabaseConnection
        System.out.println("📌 Bước 1: Khởi tạo DatabaseConnection...");
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        System.out.println("   ✅ DatabaseConnection instance created");
        System.out.println();
        
        // 2. Test kết nối
        System.out.println("📌 Bước 2: Kiểm tra kết nối MySQL...");
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✅ Kết nối database THÀNH CÔNG!");
                System.out.println();
                
                // 3. Lấy thông tin database
                System.out.println("📌 Bước 3: Thông tin database:");
                System.out.println("   Database Name: " + conn.getCatalog());
                System.out.println("   URL: " + conn.getMetaData().getURL());
                System.out.println("   MySQL Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println();
                
                // 4. Kiểm tra các bảng
                System.out.println("📌 Bước 4: Kiểm tra bảng 'users':");
                Statement stmt = conn.createStatement();
                
                // Đếm số user
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM users");
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.println("   Tổng số users: " + total);
                }
                rs.close();
                
                // Xem cấu trúc bảng
                rs = stmt.executeQuery("DESCRIBE users");
                System.out.println("   Cấu trúc bảng users:");
                System.out.println("   ┌─────────────────┬──────────────┬──────┐");
                System.out.println("   │ Field           │ Type         │ Key  │");
                System.out.println("   ├─────────────────┼──────────────┼──────┤");
                
                while (rs.next()) {
                    String field = rs.getString("Field");
                    String type = rs.getString("Type");
                    String key = rs.getString("Key");
                    System.out.printf("   │ %-15s │ %-12s │ %-4s │%n", field, type, key);
                }
                System.out.println("   └─────────────────┴──────────────┴──────┘");
                
                rs.close();
                stmt.close();
                
                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════╗");
                System.out.println("║  ✅ KẾT NỐI DATABASE HOÀN TOÀN THÀNH CÔNG!          ║");
                System.out.println("╚══════════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("🎉 Bạn có thể bắt đầu sử dụng các chức năng:");
                System.out.println("   1. Đăng ký tài khoản (UserService.registerUser)");
                System.out.println("   2. Đăng nhập (UserService.login)");
                System.out.println("   3. Cập nhật thông tin (UserService.updateProfile)");
                System.out.println("   4. Đổi mật khẩu (UserService.changePassword)");
                System.out.println("   5. Reset mật khẩu (UserService.resetPassword)");
                
            } else {
                System.out.println("   ❌ Kết nối database THẤT BẠI!");
                showTroubleshooting();
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ LỖI KHI KẾT NỐI DATABASE!");
            System.out.println();
            System.out.println("Chi tiết lỗi:");
            e.printStackTrace();
            System.out.println();
            showTroubleshooting();
            
        } finally {
            // Đóng connection
            DatabaseConnection.closeConnection(conn);
        }
    }
    
    private static void showTroubleshooting() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  🔧 HƯỚNG DẪN KHẮC PHỤC                             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("1️⃣  Kiểm tra MySQL Server đang chạy:");
        System.out.println("   • Windows: services.msc → Tìm MySQL80 → Start");
        System.out.println("   • Hoặc: net start MySQL80");
        System.out.println();
        System.out.println("2️⃣  Kiểm tra file: release/config.properties");
        System.out.println("   • db.username=root");
        System.out.println("   • db.password=YOUR_PASSWORD");
        System.out.println();
        System.out.println("3️⃣  Kiểm tra database đã tạo:");
        System.out.println("   • mysql -u root -p");
        System.out.println("   • SHOW DATABASES;");
        System.out.println("   • Phải thấy: chat_system");
        System.out.println();
        System.out.println("4️⃣  Kiểm tra MySQL Connector JAR:");
        System.out.println("   • File: lib/mysql-connector-j-9.5.0/mysql-connector-j-9.5.0.jar");
        System.out.println("   • Compile với: -cp \"lib/mysql-connector-j-9.5.0/mysql-connector-j-9.5.0.jar\"");
    }
}
