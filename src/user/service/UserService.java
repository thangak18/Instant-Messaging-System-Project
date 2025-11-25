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
        
        String sql = "INSERT INTO users (username, password, full_name, email, address, dob, gender, status) " +
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
        
        String sql = "SELECT user_id, username, password, full_name, email, status FROM users " +
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
                int userId = rs.getInt("user_id");
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
                     "dob = ?, gender = ? WHERE username = ?";
        
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
        String checkSql = "SELECT user_id, username, full_name FROM users WHERE email = ?";
        
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
            
            int userId = rs.getInt("user_id");
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
    
    /**
     * TÌM KIẾM USERS - For Add Friend feature
     * Tìm users theo username hoặc email (không bao gồm chính mình)
     */
    public java.util.List<Map<String, Object>> searchUsers(String query, String currentUsername) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        // PostgreSQL case-insensitive search với LOWER()
        String sql = "SELECT user_id, username, full_name, email " +
                     "FROM users " +
                     "WHERE (LOWER(username) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR LOWER(full_name) LIKE LOWER(?)) " +
                     "AND username != ? " +
                     "AND status = 'active' " +
                     "ORDER BY username " +
                     "LIMIT 20";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối database");
                return results;
            }
            
            System.out.println("🔍 Tìm kiếm users với query: '" + query + "', exclude: '" + currentUsername + "'");
            
            pstmt = conn.prepareStatement(sql);
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, currentUsername);
            
            System.out.println("📝 SQL: " + sql);
            System.out.println("📝 Pattern: " + searchPattern);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("user_id", rs.getInt("user_id"));
                user.put("username", rs.getString("username"));
                user.put("full_name", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                results.add(user);
                
                System.out.println("  ✅ Found: " + rs.getString("username") + " - " + rs.getString("full_name"));
            }
            
            System.out.println("✅ Tìm thấy tổng cộng: " + results.size() + " users");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm users: " + e.getMessage());
            e.printStackTrace();
        } finally {
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
        
        return results;
    }
    
    /**
     * LẤY DANH SÁCH LỜI MỜI KẾT BẠN ĐÃ NHẬN
     */
    public java.util.List<Map<String, Object>> getReceivedFriendRequests(String username) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        String sql = "SELECT f.friendship_id, f.user_id, f.friend_id, f.created_at, " +
                     "u.username as sender_username, u.full_name as sender_name " +
                     "FROM friends f " +
                     "JOIN users u ON f.user_id = u.user_id " +
                     "WHERE f.friend_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "AND f.status = 'pending' " +
                     "ORDER BY f.created_at DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return results;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("friendship_id", rs.getInt("friendship_id"));
                request.put("sender_id", rs.getInt("user_id"));
                request.put("sender_username", rs.getString("sender_username"));
                request.put("sender_name", rs.getString("sender_name"));
                request.put("created_at", rs.getTimestamp("created_at"));
                results.add(request);
            }
            
            System.out.println("✅ Tìm thấy " + results.size() + " lời mời đã nhận");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy received friend requests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * LẤY DANH SÁCH LỜI MỜI KẾT BẠN ĐÃ GỬI
     */
    public java.util.List<Map<String, Object>> getSentFriendRequests(String username) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        String sql = "SELECT f.friendship_id, f.user_id, f.friend_id, f.created_at, " +
                     "u.username as receiver_username, u.full_name as receiver_name " +
                     "FROM friends f " +
                     "JOIN users u ON f.friend_id = u.user_id " +
                     "WHERE f.user_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "AND f.status = 'pending' " +
                     "ORDER BY f.created_at DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return results;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("friendship_id", rs.getInt("friendship_id"));
                request.put("receiver_id", rs.getInt("friend_id"));
                request.put("receiver_username", rs.getString("receiver_username"));
                request.put("receiver_name", rs.getString("receiver_name"));
                request.put("created_at", rs.getTimestamp("created_at"));
                results.add(request);
            }
            
            System.out.println("✅ Tìm thấy " + results.size() + " lời mời đã gửi");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy sent friend requests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * GỬI LỜI MỜI KẾT BẠN
     */
    public boolean sendFriendRequest(String senderUsername, String receiverUsername) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            // ✅ CHECK: Kiểm tra xem có bị block không (cả 2 chiều)
            String checkBlockSQL = "SELECT COUNT(*) FROM blocked_users " +
                                   "WHERE (blocker_id = (SELECT user_id FROM users WHERE username = ?) " +
                                   "       AND blocked_id = (SELECT user_id FROM users WHERE username = ?)) " +
                                   "   OR (blocker_id = (SELECT user_id FROM users WHERE username = ?) " +
                                   "       AND blocked_id = (SELECT user_id FROM users WHERE username = ?))";
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBlockSQL)) {
                checkStmt.setString(1, senderUsername);
                checkStmt.setString(2, receiverUsername);
                checkStmt.setString(3, receiverUsername);
                checkStmt.setString(4, senderUsername);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("🚫 Không thể gửi lời mời: Có người đã bị chặn");
                        return false;
                    }
                }
            }
            
            // Tiếp tục insert friend request
            String sql = "INSERT INTO friends (user_id, friend_id, status, created_at) " +
                         "VALUES ((SELECT user_id FROM users WHERE username = ?), " +
                         "        (SELECT user_id FROM users WHERE username = ?), " +
                         "        'pending', CURRENT_TIMESTAMP)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, senderUsername);
            pstmt.setString(2, receiverUsername);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã gửi lời mời kết bạn từ " + senderUsername + " → " + receiverUsername);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi gửi friend request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * CHẤP NHẬN LỜI MỜI KẾT BẠN
     */
    public boolean acceptFriendRequest(int friendshipId) {
        String sql = "UPDATE friends SET status = 'accepted', updated_at = CURRENT_TIMESTAMP " +
                     "WHERE friendship_id = ? AND status = 'pending'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, friendshipId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã chấp nhận lời mời kết bạn #" + friendshipId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi accept friend request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * TỪ CHỐI LỜI MỜI KẾT BẠN
     */
    public boolean rejectFriendRequest(int friendshipId) {
        String sql = "DELETE FROM friends WHERE friendship_id = ? AND status = 'pending'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, friendshipId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã từ chối lời mời kết bạn #" + friendshipId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi reject friend request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * THU HỒI LỜI MỜI KẾT BẠN
     */
    public boolean recallFriendRequest(int friendshipId) {
        String sql = "DELETE FROM friends WHERE friendship_id = ? AND status = 'pending'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, friendshipId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã thu hồi lời mời kết bạn #" + friendshipId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi recall friend request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * ĐẾM SỐ LỜI MỜI KẾT BẠN ĐÃ NHẬN
     */
    public int countReceivedFriendRequests(String username) {
        String sql = "SELECT COUNT(*) FROM friends f " +
                     "WHERE f.friend_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "AND f.status = 'pending'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return 0;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("🔔 Có " + count + " lời mời kết bạn mới");
                return count;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đếm friend requests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * LẤY DANH SÁCH BẠN BÈ
     */
    public java.util.List<Map<String, Object>> getFriendsList(String username) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        String sql = "SELECT DISTINCT " +
                     "CASE " +
                     "  WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.user_id " +
                     "  ELSE u1.user_id " +
                     "END as user_id, " +
                     "CASE " +
                     "  WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.username " +
                     "  ELSE u1.username " +
                     "END as username, " +
                     "CASE " +
                     "  WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.full_name " +
                     "  ELSE u1.full_name " +
                     "END as full_name " +
                     "FROM friends f " +
                     "JOIN users u1 ON f.user_id = u1.user_id " +
                     "JOIN users u2 ON f.friend_id = u2.user_id " +
                     "WHERE f.status = 'accepted' " +
                     "AND (u1.username = ? OR u2.username = ?) " +
                     "ORDER BY full_name ASC, username ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return results;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            pstmt.setString(4, username);
            pstmt.setString(5, username);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> friend = new HashMap<>();
                friend.put("user_id", rs.getInt("user_id"));
                friend.put("username", rs.getString("username"));
                friend.put("full_name", rs.getString("full_name"));
                results.add(friend);
            }
            
            System.out.println("✅ Tìm thấy " + results.size() + " bạn bè");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách bạn bè: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * KIỂM TRA FRIENDSHIP STATUS
     * @return "friends" | "pending_sent" | "pending_received" | "none"
     */
    public String getFriendshipStatus(String currentUsername, String targetUsername) {
        String sql = "SELECT f.status, f.user_id, u1.username as sender " +
                     "FROM friends f " +
                     "JOIN users u1 ON f.user_id = u1.user_id " +
                     "JOIN users u2 ON f.friend_id = u2.user_id " +
                     "WHERE (u1.username = ? AND u2.username = ?) " +
                     "   OR (u1.username = ? AND u2.username = ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return "none";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUsername);
            pstmt.setString(2, targetUsername);
            pstmt.setString(3, targetUsername);
            pstmt.setString(4, currentUsername);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                String sender = rs.getString("sender");
                
                if ("accepted".equals(status)) {
                    return "friends";
                } else if ("pending".equals(status)) {
                    // Check ai là người gửi
                    if (sender.equals(currentUsername)) {
                        return "pending_sent";
                    } else {
                        return "pending_received";
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi check friendship status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return "none";
    }
    
    /**
     * LẤY DANH SÁCH CHAT GẦN ĐÂY
     * Bao gồm: bạn bè + tin nhắn cuối cùng + số tin chưa đọc
     */
    public java.util.List<Map<String, Object>> getRecentChats(String username) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        System.out.println("🔍 Đang tìm recent chats cho user: " + username);
        
        // Query lấy bạn bè và tin nhắn cuối cùng
        String sql = "WITH user_friends AS ( " +
                     "  SELECT " +
                     "    CASE WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN f.friend_id ELSE f.user_id END as friend_user_id " +
                     "  FROM friends f " +
                     "  WHERE f.status = 'accepted' " +
                     "  AND (f.user_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "       OR f.friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     ") " +
                     "SELECT DISTINCT " +
                     "  uf.friend_user_id, " +
                     "  u.username as friend_username, " +
                     "  u.full_name as friend_name, " +
                     "  m.last_message, " +
                     "  m.sent_at " +
                     "FROM user_friends uf " +
                     "JOIN users u ON uf.friend_user_id = u.user_id " +
                     "LEFT JOIN LATERAL ( " +
                     "  SELECT content as last_message, created_at as sent_at " +
                     "  FROM messages " +
                     "  WHERE (sender_id = (SELECT user_id FROM users WHERE username = ?) AND receiver_id = uf.friend_user_id) " +
                     "     OR (sender_id = uf.friend_user_id AND receiver_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     "  ORDER BY created_at DESC " +
                     "  LIMIT 1 " +
                     ") m ON true " +
                     "ORDER BY m.sent_at DESC NULLS LAST, u.full_name";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối database");
                return results;
            }
            
            System.out.println("✅ Đã kết nối database");
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            pstmt.setString(4, username);
            pstmt.setString(5, username);
            
            System.out.println("🔄 Đang execute query...");
            rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                Map<String, Object> chat = new HashMap<>();
                String friendUsername = rs.getString("friend_username");
                String friendName = rs.getString("friend_name");
                String lastMessage = rs.getString("last_message");
                java.sql.Timestamp sentAt = rs.getTimestamp("sent_at");
                
                System.out.println("  📌 Tìm thấy bạn: " + friendUsername + " (" + friendName + ")");
                
                chat.put("friend_user_id", rs.getInt("friend_user_id"));
                chat.put("friend_username", friendUsername);
                chat.put("friend_name", friendName);
                chat.put("last_message", lastMessage != null ? lastMessage : "Bắt đầu trò chuyện");
                chat.put("sent_at", sentAt);
                chat.put("unread_count", 0);
                
                results.add(chat);
            }
            
            System.out.println("✅ Tìm thấy " + count + " bạn bè / " + results.size() + " cuộc trò chuyện");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi lấy recent chats: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * LẤY LỊCH SỬ CHAT GIỮA 2 USERS
     */
    public java.util.List<Map<String, Object>> getChatHistory(String username1, String username2) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        String sql = "SELECT " +
                     "  m.message_id, " +
                     "  m.sender_id, " +
                     "  m.receiver_id, " +
                     "  u1.username as sender_username, " +
                     "  m.content, " +
                     "  m.created_at " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.user_id " +
                     "WHERE ( " +
                     "  (m.sender_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "   AND m.receiver_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     "  OR " +
                     "  (m.sender_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "   AND m.receiver_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     ") " +
                     // Loại bỏ tin nhắn đã xóa bởi user hiện tại (username1)
                     "AND NOT EXISTS ( " +
                     "  SELECT 1 FROM deleted_messages dm " +
                     "  WHERE dm.message_id = m.message_id " +
                     "  AND dm.user_id = (SELECT user_id FROM users WHERE username = ?) " +
                     ") " +
                     "ORDER BY m.created_at ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return results;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username1);
            pstmt.setString(2, username2);
            pstmt.setString(3, username2);
            pstmt.setString(4, username1);
            pstmt.setString(5, username1); // Loại bỏ tin nhắn đã xóa
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("message_id", rs.getInt("message_id"));
                msg.put("sender_id", rs.getInt("sender_id"));
                msg.put("receiver_id", rs.getInt("receiver_id"));
                msg.put("sender_username", rs.getString("sender_username"));
                msg.put("content", rs.getString("content"));
                msg.put("sent_at", rs.getTimestamp("created_at"));
                results.add(msg);
            }
            
            System.out.println("✅ Tìm thấy " + results.size() + " tin nhắn");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy chat history: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * LƯU TIN NHẮN VÀO DATABASE
     */
    public int saveMessage(String senderUsername, String receiverUsername, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, created_at) " +
                     "VALUES ( " +
                     "  (SELECT user_id FROM users WHERE username = ?), " +
                     "  (SELECT user_id FROM users WHERE username = ?), " +
                     "  ?, " +
                     "  CURRENT_TIMESTAMP " +
                     ") RETURNING message_id";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return -1;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, senderUsername);
            pstmt.setString(2, receiverUsername);
            pstmt.setString(3, content);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int messageId = rs.getInt("message_id");
                System.out.println("✅ Đã lưu tin nhắn #" + messageId + ": " + senderUsername + " → " + receiverUsername);
                return messageId;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lưu message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return -1;
    }
    
    /**
     * TÌM KIẾM BẠN BÈ THEO USERNAME HOẶC HỌ TÊN
     */
    public java.util.List<Map<String, Object>> searchFriends(String currentUsername, String searchQuery) {
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        String sql = "SELECT DISTINCT " +
                     "  CASE " +
                     "    WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.user_id " +
                     "    ELSE u1.user_id " +
                     "  END as user_id, " +
                     "  CASE " +
                     "    WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.username " +
                     "    ELSE u1.username " +
                     "  END as username, " +
                     "  CASE " +
                     "    WHEN f.user_id = (SELECT user_id FROM users WHERE username = ?) THEN u2.full_name " +
                     "    ELSE u1.full_name " +
                     "  END as full_name " +
                     "FROM friends f " +
                     "JOIN users u1 ON f.user_id = u1.user_id " +
                     "JOIN users u2 ON f.friend_id = u2.user_id " +
                     "WHERE f.status = 'accepted' " +
                     "AND (u1.username = ? OR u2.username = ?) " +
                     "AND ( " +
                     "  (f.user_id = (SELECT user_id FROM users WHERE username = ?) AND (LOWER(u2.username) LIKE LOWER(?) OR LOWER(u2.full_name) LIKE LOWER(?))) " +
                     "  OR " +
                     "  (f.friend_id = (SELECT user_id FROM users WHERE username = ?) AND (LOWER(u1.username) LIKE LOWER(?) OR LOWER(u1.full_name) LIKE LOWER(?))) " +
                     ") " +
                     "ORDER BY full_name ASC, username ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return results;
            
            pstmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchQuery + "%";
            
            pstmt.setString(1, currentUsername);
            pstmt.setString(2, currentUsername);
            pstmt.setString(3, currentUsername);
            pstmt.setString(4, currentUsername);
            pstmt.setString(5, currentUsername);
            pstmt.setString(6, currentUsername);
            pstmt.setString(7, searchPattern);
            pstmt.setString(8, searchPattern);
            pstmt.setString(9, currentUsername);
            pstmt.setString(10, searchPattern);
            pstmt.setString(11, searchPattern);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> friend = new HashMap<>();
                friend.put("user_id", rs.getInt("user_id"));
                friend.put("username", rs.getString("username"));
                friend.put("full_name", rs.getString("full_name"));
                results.add(friend);
            }
            
            System.out.println("✅ Tìm thấy " + results.size() + " bạn bè khớp với: " + searchQuery);
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm bạn bè: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * Huỷ kết bạn
     */
    public boolean unfriend(String username1, String username2) {
        String sql = "DELETE FROM friends " +
                     "WHERE status = 'accepted' " +
                     "AND ( " +
                     "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     "  OR " +
                     "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                     ")";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username1);
            pstmt.setString(2, username2);
            pstmt.setString(3, username2);
            pstmt.setString(4, username1);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã huỷ kết bạn: " + username1 + " <-> " + username2);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi huỷ kết bạn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Block user và huỷ kết bạn
     */
    public boolean blockUser(String blocker, String blocked) {
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Huỷ kết bạn (nếu có)
            String deleteFriendSQL = "DELETE FROM friends " +
                                     "WHERE status = 'accepted' " +
                                     "AND ( " +
                                     "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                                     "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                                     "  OR " +
                                     "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                                     "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                                     ")";
            
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFriendSQL)) {
                pstmt.setString(1, blocker);
                pstmt.setString(2, blocked);
                pstmt.setString(3, blocked);
                pstmt.setString(4, blocker);
                pstmt.executeUpdate();
            }
            
            // 2. Xoá các lời mời kết bạn pending (dùng bảng friends, không phải friend_requests)
            String deletePendingSQL = "DELETE FROM friends " +
                                      "WHERE status = 'pending' " +
                                      "AND ( " +
                                      "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                                      "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                                      "  OR " +
                                      "  (user_id = (SELECT user_id FROM users WHERE username = ?) " +
                                      "   AND friend_id = (SELECT user_id FROM users WHERE username = ?)) " +
                                      ")";
            
            try (PreparedStatement pstmt = conn.prepareStatement(deletePendingSQL)) {
                pstmt.setString(1, blocker);
                pstmt.setString(2, blocked);
                pstmt.setString(3, blocked);
                pstmt.setString(4, blocker);
                pstmt.executeUpdate();
            }
            
            // 3. Thêm vào bảng blocked_users
            String blockSQL = "INSERT INTO blocked_users (blocker_id, blocked_id, blocked_at) " +
                             "VALUES ( " +
                             "  (SELECT user_id FROM users WHERE username = ?), " +
                             "  (SELECT user_id FROM users WHERE username = ?), " +
                             "  CURRENT_TIMESTAMP " +
                             ") " +
                             "ON CONFLICT (blocker_id, blocked_id) DO NOTHING";
            
            try (PreparedStatement pstmt = conn.prepareStatement(blockSQL)) {
                pstmt.setString(1, blocker);
                pstmt.setString(2, blocked);
                int rows = pstmt.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    System.out.println("✅ Đã block user: " + blocker + " -> " + blocked);
                    return true;
                } else {
                    // Already blocked
                    conn.commit();
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi block user: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return false;
    }
    
    /**
     * Lấy thông tin chi tiết user
     */
    public Map<String, Object> getUserInfo(String username) {
        String sql = "SELECT username, full_name, email, address, dob, gender, created_at " +
                    "FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> userInfo = new java.util.HashMap<>();
                userInfo.put("username", rs.getString("username"));
                userInfo.put("full_name", rs.getString("full_name"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("address", rs.getString("address"));
                userInfo.put("dob", rs.getDate("dob"));
                userInfo.put("gender", rs.getString("gender"));
                userInfo.put("created_at", rs.getTimestamp("created_at"));
                return userInfo;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy thông tin user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Cập nhật thông tin user (full profile)
     */
    public boolean updateUserProfile(String username, String fullName, String email, 
                                     String address, java.sql.Date dob, String gender) {
        String sql = "UPDATE users SET full_name = ?, email = ?, address = ?, dob = ?, gender = ? " +
                    "WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, address);
            pstmt.setDate(4, dob);
            pstmt.setString(5, gender);
            pstmt.setString(6, username);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật profile: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Bỏ chặn user
     */
    public boolean unblockUser(String blocker, String blocked) {
        String sql = "DELETE FROM blocked_users " +
                    "WHERE blocker_id = (SELECT user_id FROM users WHERE username = ?) " +
                    "AND blocked_id = (SELECT user_id FROM users WHERE username = ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, blocker);
            pstmt.setString(2, blocked);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi unblock user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Lấy danh sách người đã chặn
     */
    public java.util.List<Map<String, Object>> getBlockedUsers(String username) {
        java.util.List<Map<String, Object>> blockedUsers = new java.util.ArrayList<>();
        
        String sql = "SELECT u.username, u.full_name, bu.blocked_at " +
                    "FROM blocked_users bu " +
                    "JOIN users u ON bu.blocked_id = u.user_id " +
                    "WHERE bu.blocker_id = (SELECT user_id FROM users WHERE username = ?) " +
                    "ORDER BY bu.blocked_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> user = new java.util.HashMap<>();
                user.put("username", rs.getString("username"));
                user.put("full_name", rs.getString("full_name"));
                user.put("blocked_at", rs.getTimestamp("blocked_at"));
                blockedUsers.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách blocked users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return blockedUsers;
    }
    
    /**
     * Cập nhật tên hiển thị
     */
    public boolean updateFullName(String username, String fullName) {
        String sql = "UPDATE users SET full_name = ? WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật full name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * BÁO CÁO SPAM
     * Thêm báo cáo spam vào database
     * 
     * @param reporterUsername username của người báo cáo
     * @param reportedUsername username của người bị báo cáo
     * @param reason lý do báo cáo
     * @return true nếu báo cáo thành công
     */
    public boolean reportSpam(String reporterUsername, String reportedUsername, String reason) {
        String sql = "INSERT INTO spam_reports (reporter_id, reported_user_id, reason, status) " +
                     "SELECT u1.id, u2.id, ?, 'pending' " +
                     "FROM users u1, users u2 " +
                     "WHERE u1.username = ? AND u2.username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối database");
                return false;
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, reason);
            pstmt.setString(2, reporterUsername);
            pstmt.setString(3, reportedUsername);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Báo cáo spam thành công: " + reporterUsername + " -> " + reportedUsername);
                return true;
            } else {
                System.err.println("❌ Không thể tạo báo cáo spam");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi báo cáo spam: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cập nhật mật khẩu
     */
    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật password: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ==================== CHAT HISTORY MANAGEMENT ====================
    
    /**
     * LẤY LỊCH SỬ CHAT VỚI 1 NGƯỜI (CÓ ID ĐỂ XÓA)
     */
    public java.util.List<Map<String, Object>> getChatHistoryWithUser(String username, String friendUsername) {
        String sql = "SELECT id, sender, receiver, content, sent_at " +
                     "FROM messages " +
                     "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                     "ORDER BY sent_at ASC";
        
        java.util.List<Map<String, Object>> messages = new java.util.ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, friendUsername);
            pstmt.setString(3, friendUsername);
            pstmt.setString(4, username);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("id"));
                message.put("sender", rs.getString("sender"));
                message.put("receiver", rs.getString("receiver"));
                message.put("content", rs.getString("content"));
                message.put("sent_at", rs.getTimestamp("sent_at"));
                messages.add(message);
            }
            
            System.out.println("📜 Lấy " + messages.size() + " tin nhắn với " + friendUsername);
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy lịch sử: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * XÓA NHIỀU TIN NHẮN THEO ID
     */
    public boolean deleteMessages(java.util.List<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return false;
        }
        
        StringBuilder sql = new StringBuilder("DELETE FROM messages WHERE id IN (");
        for (int i = 0; i < messageIds.size(); i++) {
            sql.append("?");
            if (i < messageIds.size() - 1) sql.append(",");
        }
        sql.append(")");
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < messageIds.size(); i++) {
                pstmt.setInt(i + 1, messageIds.get(i));
            }
            
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Đã xóa " + rows + " tin nhắn");
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi xóa tin nhắn: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * XÓA TOÀN BỘ LỊCH SỬ CHAT VỚI 1 NGƯỜI
     */
    public boolean deleteChatHistory(String username, String friendUsername) {
        String sql = "DELETE FROM messages WHERE " +
                     "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, friendUsername);
            pstmt.setString(3, friendUsername);
            pstmt.setString(4, username);
            
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Đã xóa " + rows + " tin nhắn với " + friendUsername);
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi xóa lịch sử: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * TÌM KIẾM TRONG LỊCH SỬ CHAT VỚI 1 NGƯỜI
     */
    public java.util.List<Map<String, Object>> searchInChatHistory(String username, String friendUsername, String keyword) {
        String sql = "SELECT sender, receiver, content, sent_at " +
                     "FROM messages " +
                     "WHERE ((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)) " +
                     "AND LOWER(content) LIKE LOWER(?) " +
                     "ORDER BY sent_at DESC " +
                     "LIMIT 100";
        
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, friendUsername);
            pstmt.setString(3, friendUsername);
            pstmt.setString(4, username);
            pstmt.setString(5, "%" + keyword + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("sender", rs.getString("sender"));
                message.put("receiver", rs.getString("receiver"));
                message.put("content", rs.getString("content"));
                message.put("sent_at", rs.getTimestamp("sent_at"));
                results.add(message);
            }
            
            System.out.println("🔍 Tìm thấy " + results.size() + " kết quả");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    // ==================== XÓA TIN NHẮN RIÊNG LẺ ====================
    
    /**
     * XÓA TIN NHẮN CHỈ MÌNH TÔI (Soft Delete)
     * Thêm user_id vào bảng deleted_messages
     */
    public boolean deleteMessageForMe(int messageId, String username) {
        // Kiểm tra bảng deleted_messages có tồn tại chưa, nếu chưa thì tạo
        String createTableSql = "CREATE TABLE IF NOT EXISTS deleted_messages (" +
                                "message_id INTEGER NOT NULL, " +
                                "user_id INTEGER NOT NULL, " +
                                "deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "PRIMARY KEY (message_id, user_id))";
        
        String insertSql = "INSERT INTO deleted_messages (message_id, user_id) " +
                          "SELECT ?, user_id FROM users WHERE username = ? " +
                          "ON CONFLICT (message_id, user_id) DO NOTHING";
        
        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) return false;
            
            // Tạo bảng nếu chưa có
            try (PreparedStatement createStmt = conn.prepareStatement(createTableSql)) {
                createStmt.execute();
            }
            
            // Insert vào deleted_messages
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, messageId);
                pstmt.setString(2, username);
                
                int rows = pstmt.executeUpdate();
                System.out.println("✅ Đã ẩn tin nhắn " + messageId + " cho " + username);
                return rows > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi xóa tin nhắn cho mình: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * THU HỒI TIN NHẮN (Hard Delete)
     * Xóa hoàn toàn khỏi database
     */
    public boolean recallMessage(int messageId, String username) {
        // Kiểm tra tin nhắn có phải của user này gửi không
        String checkSql = "SELECT sender_id FROM messages m " +
                         "JOIN users u ON m.sender_id = u.user_id " +
                         "WHERE m.message_id = ? AND u.username = ?";
        
        String deleteSql = "DELETE FROM messages WHERE message_id = ?";
        
        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) return false;
            
            // Kiểm tra quyền
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, messageId);
                checkStmt.setString(2, username);
                
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("❌ Không thể thu hồi tin nhắn của người khác!");
                    return false;
                }
            }
            
            // Xóa tin nhắn
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, messageId);
                
                int rows = deleteStmt.executeUpdate();
                System.out.println("✅ Đã thu hồi tin nhắn " + messageId);
                return rows > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thu hồi tin nhắn: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}

