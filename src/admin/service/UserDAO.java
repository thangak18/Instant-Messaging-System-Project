package admin.service;

import admin.socket.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho User
 * Xử lý tất cả truy vấn database liên quan đến người dùng
 */
public class UserDAO {
    private DatabaseConnection dbConnection;
    
    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public enum SearchType {
        ALL,
        USERNAME,
        FULL_NAME,
        EMAIL
    }
    
    /**
     * Lấy tất cả người dùng
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, full_name, email, address, " +
                    "dob, gender, status, created_at, last_login " +
                    "FROM users ORDER BY created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }
    
    /**
     * Tìm kiếm người dùng theo tên hoặc username
     */
    public List<User> searchUsers(String keyword) throws SQLException {
        return searchUsers(keyword, SearchType.ALL);
    }

    /**
     * Tìm kiếm người dùng theo trường cụ thể
     */
    public List<User> searchUsers(String keyword, SearchType searchType) throws SQLException {
        List<User> users = new ArrayList<>();
        if (keyword == null || keyword.isEmpty()) {
            return users;
        }

        String columns = "SELECT user_id, username, password, full_name, email, address, " +
                        "dob, gender, status, created_at, last_login FROM users ";
        String baseSql;
        switch (searchType) {
            case USERNAME:
                baseSql = columns + "WHERE username LIKE ? ORDER BY created_at DESC";
                break;
            case FULL_NAME:
                baseSql = columns + "WHERE full_name LIKE ? ORDER BY created_at DESC";
                break;
            case EMAIL:
                baseSql = columns + "WHERE email LIKE ? ORDER BY created_at DESC";
                break;
            default:
                baseSql = columns + "WHERE username LIKE ? OR full_name LIKE ? OR email LIKE ? " +
                          "ORDER BY created_at DESC";
                break;
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(baseSql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);

            if (searchType == SearchType.ALL) {
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        }

        return users;
    }
    
    /**
     * Lọc người dùng theo trạng thái
     */
    public List<User> getUsersByStatus(String status) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, full_name, email, address, " +
                    "dob, gender, status, created_at, last_login " +
                    "FROM users WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        }
        return users;
    }
    
    /**
     * Lấy người dùng theo ID
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT user_id, username, password, full_name, email, address, " +
                    "dob, gender, status, created_at, last_login " +
                    "FROM users WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Thêm người dùng mới
     */
    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, email, address, " +
                    "dob, gender, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getAddress());
            pstmt.setDate(6, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getStatus());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cập nhật thông tin người dùng
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, address = ?, " +
                    "dob = ?, gender = ?, status = ? WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getAddress());
            pstmt.setDate(4, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getStatus());
            pstmt.setInt(7, user.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Xóa người dùng
     */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Khóa/Mở khóa tài khoản
     */
    public boolean updateUserStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cập nhật mật khẩu
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Đếm tổng số người dùng
     */
    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Đếm số người dùng theo trạng thái
     */
    public int countUsersByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Kiểm tra username đã tồn tại chưa
     */
    public boolean usernameExists(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean emailExists(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra email đã tồn tại chưa (trừ user hiện tại - dùng khi update)
     */
    public boolean emailExists(String email, int excludeUserId) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email.trim());
            pstmt.setInt(2, excludeUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Helper method: Extract User object from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setAddress(rs.getString("address"));
        
        Date dob = rs.getDate("dob");
        if (dob != null) {
            user.setBirthDate(dob.toLocalDate());
        }
        
        user.setGender(rs.getString("gender"));
        user.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Note: Database có last_login thay vì updated_at
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setUpdatedAt(lastLogin.toLocalDateTime()); // Tạm map vào updatedAt
        }
        
        return user;
    }
}
