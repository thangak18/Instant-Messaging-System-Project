package user.service;

import java.sql.Date;

/**
 * Test đăng ký user
 */
public class TestRegister {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  TEST ĐĂNG KÝ USER MỚI                              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        
        UserService userService = new UserService();
        
        // Test 1: Đăng ký user mới
        System.out.println("📝 Test 1: Đăng ký user mới");
        System.out.println("   Username: alice123");
        System.out.println("   Password: password123");
        System.out.println("   Email: alice@example.com");
        System.out.println();
        
        boolean success1 = userService.registerUser(
            "alice123",
            "password123",
            "Alice Nguyen",
            "alice@example.com",
            "TP HCM",
            Date.valueOf("1995-05-15"),
            "Nữ"
        );
        
        if (success1) {
            System.out.println("✅ Test 1: PASS - Đăng ký thành công!");
        } else {
            System.out.println("❌ Test 1: FAIL - Đăng ký thất bại!");
        }
        
        System.out.println();
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Test 2: Đăng ký trùng username
        System.out.println("📝 Test 2: Đăng ký trùng username (alice123)");
        System.out.println("   Kết quả mong đợi: Thất bại");
        System.out.println();
        
        boolean success2 = userService.registerUser(
            "alice123",  // Trùng username
            "different_password",
            "Alice Duplicate",
            "alice2@example.com",
            "Ha Noi",
            Date.valueOf("2000-01-01"),
            "Nữ"
        );
        
        if (!success2) {
            System.out.println("✅ Test 2: PASS - Đúng là bị từ chối!");
        } else {
            System.out.println("❌ Test 2: FAIL - Lẽ ra phải bị từ chối!");
        }
        
        System.out.println();
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Test 3: Đăng ký trùng email
        System.out.println("📝 Test 3: Đăng ký trùng email (alice@example.com)");
        System.out.println("   Kết quả mong đợi: Thất bại");
        System.out.println();
        
        boolean success3 = userService.registerUser(
            "bob123",
            "password456",
            "Bob Tran",
            "alice@example.com",  // Trùng email
            "Da Nang",
            Date.valueOf("1998-08-20"),
            "Nam"
        );
        
        if (!success3) {
            System.out.println("✅ Test 3: PASS - Đúng là bị từ chối!");
        } else {
            System.out.println("❌ Test 3: FAIL - Lẽ ra phải bị từ chối!");
        }
        
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  ✅ HOÀN THÀNH TEST                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}
