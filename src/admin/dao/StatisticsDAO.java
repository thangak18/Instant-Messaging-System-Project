package admin.dao;

import admin.model.FriendStats;
import admin.model.UserActivity;
import admin.service.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object cho Statistics
 * Xử lý các queries thống kê phức tạp
 */
public class StatisticsDAO {
    private DatabaseConnection dbConnection;
    
    public StatisticsDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Lấy thống kê số lượng bạn bè của mỗi người dùng
     */
    public List<FriendStats> getFriendStatistics() throws SQLException {
        List<FriendStats> stats = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, " +
                    "COUNT(DISTINCT f.id) as friend_count " +
                    "FROM users u " +
                    "LEFT JOIN friendships f ON (u.id = f.user1_id OR u.id = f.user2_id) " +
                    "AND f.status = 'accepted' " +
                    "GROUP BY u.id, u.username, u.full_name " +
                    "ORDER BY friend_count DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FriendStats stat = new FriendStats();
                stat.setUserId(rs.getInt("id"));
                stat.setUsername(rs.getString("username"));
                stat.setFullName(rs.getString("full_name"));
                stat.setFriendCount(rs.getInt("friend_count"));
                stats.add(stat);
            }
        }
        return stats;
    }
    
    /**
     * Lấy danh sách người dùng hoạt động (có tin nhắn trong 30 ngày)
     */
    public List<UserActivity> getActiveUsers(int days) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, " +
                    "COUNT(DISTINCT pm.id) + COUNT(DISTINCT gm.id) as activity_count, " +
                    "GREATEST(MAX(pm.sent_at), MAX(gm.sent_at)) as last_activity " +
                    "FROM users u " +
                    "LEFT JOIN private_messages pm ON (u.id = pm.sender_id) " +
                    "AND pm.sent_at >= NOW() - INTERVAL ? DAY " +
                    "LEFT JOIN group_messages gm ON (u.id = gm.sender_id) " +
                    "AND gm.sent_at >= NOW() - INTERVAL ? DAY " +
                    "WHERE u.status = 'active' " +
                    "GROUP BY u.id, u.username, u.full_name " +
                    "HAVING activity_count > 0 " +
                    "ORDER BY activity_count DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, days);
            pstmt.setInt(2, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserActivity activity = new UserActivity();
                    activity.setUserId(rs.getInt("id"));
                    activity.setUsername(rs.getString("username"));
                    activity.setFullName(rs.getString("full_name"));
                    activity.setActivityCount(rs.getInt("activity_count"));
                    
                    Timestamp lastActivity = rs.getTimestamp("last_activity");
                    if (lastActivity != null) {
                        activity.setLastActivity(lastActivity.toLocalDateTime());
                    }
                    
                    activities.add(activity);
                }
            }
        }
        return activities;
    }
    
    /**
     * Lấy người dùng mới (đăng ký trong X ngày)
     */
    public List<Map<String, Object>> getNewUsers(int days) throws SQLException {
        List<Map<String, Object>> newUsers = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, created_at " +
                    "FROM users " +
                    "WHERE created_at >= NOW() - INTERVAL ? DAY " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    user.put("fullName", rs.getString("full_name"));
                    user.put("email", rs.getString("email"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.put("createdAt", createdAt.toLocalDateTime());
                    }
                    
                    newUsers.add(user);
                }
            }
        }
        return newUsers;
    }
    
    /**
     * Đếm số người dùng mới theo tháng (12 tháng gần nhất)
     */
    public Map<String, Integer> getUserGrowthByMonth() throws SQLException {
        Map<String, Integer> growth = new HashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
                    "FROM users " +
                    "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                    "GROUP BY month " +
                    "ORDER BY month";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                growth.put(rs.getString("month"), rs.getInt("count"));
            }
        }
        return growth;
    }
    
    /**
     * Đếm số đăng nhập theo ngày (7 ngày gần nhất)
     */
    public Map<String, Integer> getLoginCountByDay(int days) throws SQLException {
        Map<String, Integer> loginCounts = new HashMap<>();
        String sql = "SELECT DATE(login_time) as login_date, COUNT(*) as count " +
                    "FROM login_history " +
                    "WHERE login_time >= NOW() - INTERVAL ? DAY " +
                    "GROUP BY login_date " +
                    "ORDER BY login_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    loginCounts.put(rs.getString("login_date"), rs.getInt("count"));
                }
            }
        }
        return loginCounts;
    }
    
    /**
     * Thống kê tổng quan hệ thống
     */
    public Map<String, Integer> getSystemOverview() throws SQLException {
        Map<String, Integer> overview = new HashMap<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tổng số user
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) overview.put("totalUsers", rs.getInt(1));
            
            // User active
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE status = 'active'");
            if (rs.next()) overview.put("activeUsers", rs.getInt(1));
            
            // Tổng nhóm
            rs = stmt.executeQuery("SELECT COUNT(*) FROM chat_groups");
            if (rs.next()) overview.put("totalGroups", rs.getInt(1));
            
            // Tổng tin nhắn
            rs = stmt.executeQuery("SELECT COUNT(*) FROM private_messages");
            if (rs.next()) overview.put("totalMessages", rs.getInt(1));
            
            // Báo cáo spam pending
            rs = stmt.executeQuery("SELECT COUNT(*) FROM spam_reports WHERE status = 'pending'");
            if (rs.next()) overview.put("pendingReports", rs.getInt(1));
        }
        
        return overview;
    }
}
