package user.service;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.HashMap;

/**
 * Service xử lý các nghiệp vụ liên quan đến User
 * - Đăng ký tài khoản
 * - Đăng nhập
 * - Cập nhật thông tin
 * - Đổi mật khẩu
 * - Reset mật khẩu
 */
public class UserService {
    
    private DatabaseConnection dbConnection;
    
    public UserService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * 1. ĐĂNG KÝ TÀI KHOẢN
     * Tạo user mới trong database
     * 
     * @return true nếu đăng ký thành công
     */
    public boolean registerUser(String username, String password, String fullName, 
                                 String email, String address, Date birthDate, String gender) {
        
        String sql = "INSERT INTO users (username, password, full_name, email, address, birth_date, gender, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'active')";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối database");
                return false;
            }
            
            // Kiểm tra username đã tồn tại chưa
            if (isUsernameExists(username)) {
                System.err.println("❌ Username đã tồn tại: " + username);
                return false;
            }
            
            // Kiểm tra email đã tồn tại chưa
            if (isEmailExistsInDB(email)) {
                System.err.println("❌ Email đã được đăng ký: " + email);
                return false;
            }
            
            // Hash password trước khi lưu
            String hashedPassword = hashPassword(password);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);
            pstmt.setString(5, address);
            pstmt.setDate(6, birthDate);
            pstmt.setString(7, gender);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Đăng ký thành công: " + username);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đăng ký user: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            closeResources(conn, pstmt, null);
        }
        
        return false;
    }
    
    /**
     * 2. ĐĂNG NHẬP
     * Xác thực username/email và password
     * 
     * @param usernameOrEmail Tên đăng nhập hoặc email
     * @param password Mật khẩu
     * @return Map chứa kết quả: success, message, username, full_name, email, user_id
     */
    public Map<String, Object> login(String usernameOrEmail, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        String sql = "SELECT id, username, password, full_name, email, status FROM users " +
                     "WHERE username = ? OR email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                result.put("message", "Không thể kết nối đến database!");
                return result;
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail); // Cho phép đăng nhập bằng email
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String status = rs.getString("status");
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                
                // Kiểm tra tài khoản có bị khóa không
                if ("locked".equals(status)) {
                    System.err.println("❌ Tài khoản đã bị khóa: " + usernameOrEmail);
                    result.put("message", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin!");
                    return result;
                }
                
                // Verify password
                if (verifyPassword(password, storedPassword)) {
                    System.out.println("✅ Đăng nhập thành công: " + username);
                    
                    // Ghi lại lịch sử đăng nhập
                    logLoginHistory(userId);
                    
                    // Trả về thông tin thành công
                    result.put("success", true);
                    result.put("message", "Đăng nhập thành công!");
                    result.put("user_id", userId);
                    result.put("username", username);
                    result.put("full_name", fullName);
                    result.put("email", email);
                    return result;
                    
                } else {
                    System.err.println("❌ Sai mật khẩu");
                    result.put("message", "Sai mật khẩu! Vui lòng thử lại.");
                    return result;
                }
                
            } else {
                System.err.println("❌ Không tìm thấy tài khoản: " + usernameOrEmail);
                result.put("message", "Tài khoản không tồn tại! Vui lòng kiểm tra lại.");
                return result;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đăng nhập: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "Lỗi database: " + e.getMessage());
            
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return result;
    }
    
    /**
     * 3. CẬP NHẬT THÔNG TIN TÀI KHOẢN
     * Cho phép user cập nhật: full_name, email, address, birth_date, gender
     * 
     * @return true nếu cập nhật thành công
     */
    public boolean updateProfile(String username, String fullName, String email, 
                                  String address, Date birthDate, String gender) {
        
        String sql = "UPDATE users SET full_name = ?, email = ?, address = ?, " +
                     "birth_date = ?, gender = ? WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            // Kiểm tra email mới có trùng với user khác không
            if (isEmailExistsForOtherUser(email, username)) {
                System.err.println("❌ Email đã được sử dụng bởi tài khoản khác");
                return false;
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, address);
            pstmt.setDate(4, birthDate);
            pstmt.setString(5, gender);
            pstmt.setString(6, username);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật thông tin thành công: " + username);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật profile: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            closeResources(conn, pstmt, null);
        }
        
        return false;
    }
    
    /**
     * 4. ĐỔI MẬT KHẨU
     * User nhập mật khẩu cũ và mật khẩu mới
     * 
     * @return true nếu đổi mật khẩu thành công
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Bước 1: Verify mật khẩu cũ
        Map<String, Object> loginResult = login(username, oldPassword);
        if (!(boolean) loginResult.get("success")) {
            System.err.println("❌ Mật khẩu cũ không đúng");
            return false;
        }
        
        // Bước 2: Update mật khẩu mới
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            String hashedPassword = hashPassword(newPassword);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, username);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Đổi mật khẩu thành công: " + username);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            closeResources(conn, pstmt, null);
        }
        
        return false;
    }
    
    /**
     * 5. RESET MẬT KHẨU (Quên mật khẩu)
     * Bước 1: Kiểm tra email và gửi mật khẩu random
     * 
     * @param email Email của user
     * @return Map với success, message, temporary_password, username
     */
    public Map<String, Object> sendResetPasswordEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        // Kiểm tra email có tồn tại không
        String checkSql = "SELECT id, username, full_name FROM users WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                result.put("message", "Không thể kết nối đến database!");
                return result;
            }
            
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                System.err.println("❌ Không tìm thấy email: " + email);
                result.put("message", "Email không tồn tại trong hệ thống!");
                return result;
            }
            
            int userId = rs.getInt("id");
            String username = rs.getString("username");
            String fullName = rs.getString("full_name");
            
            // Tạo mật khẩu random
            String temporaryPassword = generateRandomPassword();
            
            // Gửi email (demo mode - chỉ log ra console)
            EmailService emailService = new EmailService();
            boolean emailSent = emailService.sendResetPasswordEmail(email, fullName, temporaryPassword);
            
            if (emailSent) {
                System.out.println("✅ Đã gửi mật khẩu tạm thời qua email: " + email);
                result.put("success", true);
                result.put("message", "Mật khẩu tạm thời đã được gửi đến email của bạn!");
                result.put("temporary_password", temporaryPassword);
                result.put("username", username);
                result.put("user_id", userId);
                return result;
            } else {
                result.put("message", "Không thể gửi email. Vui lòng thử lại!");
                return result;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi reset password: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "Lỗi database: " + e.getMessage());
            
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return result;
    }
    
    /**
     * Bước 2: Cập nhật mật khẩu mới sau khi verify temporary password
     * 
     * @param email Email của user
     * @param temporaryPassword Mật khẩu tạm từ email
     * @param newPassword Mật khẩu mới do user nhập
     * @return Map với success và message
     */
    public Map<String, Object> resetPasswordWithTemporary(String email, String temporaryPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        // Cập nhật mật khẩu mới vào database
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                result.put("message", "Không thể kết nối đến database!");
                return result;
            }
            
            String hashedPassword = hashPassword(newPassword);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật mật khẩu mới thành công cho email: " + email);
                result.put("success", true);
                result.put("message", "Đổi mật khẩu thành công!");
                return result;
            } else {
                result.put("message", "Không tìm thấy email trong hệ thống!");
                return result;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật mật khẩu: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "Lỗi database: " + e.getMessage());
            
        } finally {
            closeResources(conn, pstmt, null);
        }
        
        return result;
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Kiểm tra username đã tồn tại chưa
     */
    private boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return false;
    }
    
    /**
     * Kiểm tra email đã tồn tại trong database chưa (private - dùng nội bộ)
     */
    private boolean isEmailExistsInDB(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return false;
    }
    
    /**
     * Kiểm tra email có bị trùng với user khác không (dùng khi update)
     */
    private boolean isEmailExistsForOtherUser(String email, String currentUsername) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND username != ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, currentUsername);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return false;
    }
    
    /**
     * Ghi lại lịch sử đăng nhập
     */
    private void logLoginHistory(int userId) {
        String sql = "INSERT INTO login_history (user_id, ip_address) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, "127.0.0.1"); // TODO: Lấy IP thật
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // Không quan trọng lắm, chỉ log
            System.err.println("⚠️  Không ghi được login history: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    /**
     * Hash password bằng SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            // Convert byte array to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Lỗi hash password: " + e.getMessage());
            return password; // Fallback (không an toàn)
        }
    }
    
    /**
     * Verify password với hash
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hash = hashPassword(plainPassword);
        return hash.equals(hashedPassword);
    }
    
    /**
     * Tạo mật khẩu random 12 ký tự
     * Bao gồm: chữ hoa, chữ thường, số, ký tự đặc biệt
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * Đóng resources (Connection, Statement, ResultSet)
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { }
        }
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException e) { }
        }
        if (conn != null) {
            DatabaseConnection.closeConnection(conn);
        }
    }
}
