package admin.dao;

import admin.model.LoginHistory;
import admin.service.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Login History
 */
public class LoginHistoryDAO {
    private DatabaseConnection dbConnection;
    
    public LoginHistoryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Lấy tất cả lịch sử đăng nhập
     */
    public List<LoginHistory> getAllLoginHistory() throws SQLException {
        List<LoginHistory> history = new ArrayList<>();
        String sql = "SELECT lh.*, u.username, u.full_name " +
                    "FROM login_history lh " +
                    "JOIN users u ON lh.user_id = u.id " +
                    "ORDER BY lh.login_time DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                history.add(extractLoginHistoryFromResultSet(rs));
            }
        }
        return history;
    }
    
    /**
     * Lấy lịch sử đăng nhập theo user ID
     */
    public List<LoginHistory> getLoginHistoryByUserId(int userId) throws SQLException {
        List<LoginHistory> history = new ArrayList<>();
        String sql = "SELECT lh.*, u.username, u.full_name " +
                    "FROM login_history lh " +
                    "JOIN users u ON lh.user_id = u.id " +
                    "WHERE lh.user_id = ? " +
                    "ORDER BY lh.login_time DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(extractLoginHistoryFromResultSet(rs));
                }
            }
        }
        return history;
    }
    
    /**
     * Lấy lịch sử đăng nhập trong khoảng thời gian
     */
    public List<LoginHistory> getLoginHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) 
            throws SQLException {
        List<LoginHistory> history = new ArrayList<>();
        String sql = "SELECT lh.*, u.username, u.full_name " +
                    "FROM login_history lh " +
                    "JOIN users u ON lh.user_id = u.id " +
                    "WHERE lh.login_time BETWEEN ? AND ? " +
                    "ORDER BY lh.login_time DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(extractLoginHistoryFromResultSet(rs));
                }
            }
        }
        return history;
    }
    
    /**
     * Tìm kiếm lịch sử đăng nhập theo username
     */
    public List<LoginHistory> searchLoginHistory(String keyword) throws SQLException {
        List<LoginHistory> history = new ArrayList<>();
        String sql = "SELECT lh.*, u.username, u.full_name " +
                    "FROM login_history lh " +
                    "JOIN users u ON lh.user_id = u.id " +
                    "WHERE u.username LIKE ? OR u.full_name LIKE ? " +
                    "ORDER BY lh.login_time DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(extractLoginHistoryFromResultSet(rs));
                }
            }
        }
        return history;
    }
    
    /**
     * Thêm lịch sử đăng nhập mới
     */
    public boolean addLoginHistory(LoginHistory loginHistory) throws SQLException {
        String sql = "INSERT INTO login_history (user_id, login_time, ip_address, user_agent) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, loginHistory.getUserId());
            pstmt.setTimestamp(2, Timestamp.valueOf(loginHistory.getLoginTime()));
            pstmt.setString(3, loginHistory.getIpAddress());
            pstmt.setString(4, loginHistory.getUserAgent());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Đếm tổng số lần đăng nhập
     */
    public int getTotalLoginCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM login_history";
        
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
     * Đếm số lần đăng nhập của user
     */
    public int getLoginCountByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM login_history WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Helper method: Extract LoginHistory from ResultSet
     */
    private LoginHistory extractLoginHistoryFromResultSet(ResultSet rs) throws SQLException {
        LoginHistory history = new LoginHistory();
        history.setId(rs.getInt("id"));
        history.setUserId(rs.getInt("user_id"));
        history.setUsername(rs.getString("username"));
        history.setFullName(rs.getString("full_name"));
        
        Timestamp loginTime = rs.getTimestamp("login_time");
        if (loginTime != null) {
            history.setLoginTime(loginTime.toLocalDateTime());
        }
        
        history.setIpAddress(rs.getString("ip_address"));
        history.setUserAgent(rs.getString("user_agent"));
        
        return history;
    }
}
