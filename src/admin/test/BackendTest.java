package admin.test;

import admin.dao.UserDAO;
import admin.dao.LoginHistoryDAO;
import admin.dao.GroupDAO;
import admin.dao.SpamReportDAO;
import admin.dao.StatisticsDAO;
import admin.model.User;
import admin.service.DatabaseConnection;

import java.sql.Connection;
import java.util.List;

/**
 * Test class để kiểm tra tất cả backend DAOs
 */
public class BackendTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("KIỂM TRA BACKEND ADMIN");
        System.out.println("========================================\n");
        
        // Test 1: Database Connection
        System.out.println("Test 1: Kiểm tra kết nối database");
        System.out.println("----------------------------------------");
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        
        try (Connection conn = dbConn.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Kết nối database THÀNH CÔNG!");
                System.out.println("   URL: " + dbConn.getUrl());
                System.out.println("   User: " + dbConn.getUsername());
            }
        } catch (Exception e) {
            System.out.println("❌ Kết nối database THẤT BẠI!");
            System.out.println("   Lỗi: " + e.getMessage());
            return;
        }
        System.out.println();
        
        // Test 2: UserDAO
        System.out.println("Test 2: Kiểm tra UserDAO");
        System.out.println("----------------------------------------");
        try {
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers();
            System.out.println("✅ UserDAO hoạt động!");
            System.out.println("   Số lượng users: " + users.size());
            
            if (users.size() > 0) {
                User firstUser = users.get(0);
                System.out.println("   User đầu tiên: " + firstUser.getUsername() 
                    + " - " + firstUser.getFullName());
            }
        } catch (Exception e) {
            System.out.println("❌ UserDAO lỗi: " + e.getMessage());
            if (e.getMessage().contains("relation") || e.getMessage().contains("does not exist")) {
                System.out.println("   → Table 'users' chưa được tạo trong database!");
            }
        }
        System.out.println();
        
        // Test 3: LoginHistoryDAO
        System.out.println("Test 3: Kiểm tra LoginHistoryDAO");
        System.out.println("----------------------------------------");
        try {
            LoginHistoryDAO loginDAO = new LoginHistoryDAO();
            int count = loginDAO.getAllLoginHistory().size();
            System.out.println("✅ LoginHistoryDAO hoạt động!");
            System.out.println("   Số lượng login records: " + count);
        } catch (Exception e) {
            System.out.println("❌ LoginHistoryDAO lỗi: " + e.getMessage());
            if (e.getMessage().contains("relation") || e.getMessage().contains("does not exist")) {
                System.out.println("   → Table 'login_history' chưa được tạo!");
            }
        }
        System.out.println();
        
        // Test 4: GroupDAO
        System.out.println("Test 4: Kiểm tra GroupDAO");
        System.out.println("----------------------------------------");
        try {
            GroupDAO groupDAO = new GroupDAO();
            int count = groupDAO.getAllGroups().size();
            System.out.println("✅ GroupDAO hoạt động!");
            System.out.println("   Số lượng groups: " + count);
        } catch (Exception e) {
            System.out.println("❌ GroupDAO lỗi: " + e.getMessage());
            if (e.getMessage().contains("relation") || e.getMessage().contains("does not exist")) {
                System.out.println("   → Table 'chat_groups' chưa được tạo!");
            }
        }
        System.out.println();
        
        // Test 5: SpamReportDAO
        System.out.println("Test 5: Kiểm tra SpamReportDAO");
        System.out.println("----------------------------------------");
        try {
            SpamReportDAO spamDAO = new SpamReportDAO();
            int count = spamDAO.getAllSpamReports().size();
            System.out.println("✅ SpamReportDAO hoạt động!");
            System.out.println("   Số lượng spam reports: " + count);
        } catch (Exception e) {
            System.out.println("❌ SpamReportDAO lỗi: " + e.getMessage());
            if (e.getMessage().contains("relation") || e.getMessage().contains("does not exist")) {
                System.out.println("   → Table 'spam_reports' chưa được tạo!");
            }
        }
        System.out.println();
        
        // Test 6: StatisticsDAO
        System.out.println("Test 6: Kiểm tra StatisticsDAO");
        System.out.println("----------------------------------------");
        try {
            StatisticsDAO statsDAO = new StatisticsDAO();
            int friendStatsCount = statsDAO.getFriendStatistics().size();
            System.out.println("✅ StatisticsDAO hoạt động!");
            System.out.println("   Friend statistics records: " + friendStatsCount);
        } catch (Exception e) {
            System.out.println("❌ StatisticsDAO lỗi: " + e.getMessage());
            if (e.getMessage().contains("relation") || e.getMessage().contains("does not exist")) {
                System.out.println("   → Các tables cần thiết chưa được tạo!");
            }
        }
        System.out.println();
        
        // Kết luận
        System.out.println("========================================");
        System.out.println("KẾT LUẬN");
        System.out.println("========================================");
        System.out.println("Nếu thấy lỗi 'relation does not exist':");
        System.out.println("  → Bạn cần tạo tables trong Supabase");
        System.out.println("  → Vào: Supabase Dashboard → SQL Editor");
        System.out.println("  → Run: script/database/create_database_supabase.sql");
        System.out.println();
        System.out.println("Nếu tất cả đều ✅:");
        System.out.println("  → Backend admin hoạt động hoàn hảo!");
        System.out.println("  → Có thể chạy ứng dụng: ./run_admin.sh");
        System.out.println("========================================");
    }
}
