package user.service;

/**
 * Test chức năng Quên Mật Khẩu
 */
public class TestForgotPassword {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("TEST: QUÊN MẬT KHẨU");
        System.out.println("=".repeat(70));
        
        UserService userService = new UserService();
        
        // Test 1: Email không tồn tại
        System.out.println("\n>>> Test 1: Email không tồn tại");
        java.util.Map<String, Object> result1 = userService.sendResetPasswordEmail("notexist@example.com");
        System.out.println("Success: " + result1.get("success"));
        System.out.println("Message: " + result1.get("message"));
        
        // Test 2: Email hợp lệ (email của user hung)
        System.out.println("\n>>> Test 2: Email hợp lệ");
        java.util.Map<String, Object> result2 = userService.sendResetPasswordEmail("tmhung23@clc.fitus.edu.vn");
        System.out.println("Success: " + result2.get("success"));
        System.out.println("Message: " + result2.get("message"));
        
        if ((boolean) result2.get("success")) {
            String tempPassword = (String) result2.get("temporary_password");
            String username = (String) result2.get("username");
            
            System.out.println("\n📧 Thông tin gửi email:");
            System.out.println("   Username: " + username);
            System.out.println("   Temporary Password: " + tempPassword);
            
            // Test 3: Cập nhật mật khẩu mới
            System.out.println("\n>>> Test 3: Cập nhật mật khẩu mới");
            String newPassword = "newpass123";
            java.util.Map<String, Object> result3 = userService.resetPasswordWithTemporary(
                "tmhung23@clc.fitus.edu.vn",
                tempPassword,
                newPassword
            );
            System.out.println("Success: " + result3.get("success"));
            System.out.println("Message: " + result3.get("message"));
            
            if ((boolean) result3.get("success")) {
                // Test 4: Đăng nhập với mật khẩu mới
                System.out.println("\n>>> Test 4: Đăng nhập với mật khẩu mới");
                java.util.Map<String, Object> result4 = userService.login(username, newPassword);
                System.out.println("Success: " + result4.get("success"));
                System.out.println("Message: " + result4.get("message"));
                
                if ((boolean) result4.get("success")) {
                    System.out.println("Full Name: " + result4.get("full_name"));
                }
                
                // Reset lại mật khẩu cũ (123456) để test tiếp
                System.out.println("\n>>> Reset lại mật khẩu cũ (123456)");
                java.util.Map<String, Object> result5 = userService.resetPasswordWithTemporary(
                    "tmhung23@clc.fitus.edu.vn",
                    tempPassword,
                    "123456"
                );
                System.out.println("Success: " + result5.get("success"));
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST HOÀN TẤT");
        System.out.println("=".repeat(70));
    }
}
