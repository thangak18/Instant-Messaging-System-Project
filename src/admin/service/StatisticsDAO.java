package admin.service;

import admin.socket.FriendStats;
import admin.socket.User;
import admin.socket.UserActivity;

import java.sql.*;
import java.time.LocalDate;
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
    String sql = "SELECT id, username, full_name, email, status, created_at " +
                    "FROM users " +
                    "WHERE created_at >= NOW() - INTERVAL ? DAY " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    
                    user.put("id", id);
                    user.put("user_id", id);
                    user.put("username", username);
                    user.put("fullName", fullName);
                    user.put("full_name", fullName);
                    user.put("email", rs.getString("email"));
                    user.put("status", rs.getString("status"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        LocalDateTime created = createdAt.toLocalDateTime();
                        user.put("createdAt", created);
                        user.put("created_at", created);
                    }
                    
                    newUsers.add(user);
                }
            }
        }
        return newUsers;
    }

    /**
     * Lấy danh sách người dùng mới theo khoảng thời gian và bộ lọc
     */
    public List<User> getNewUsers(LocalDate startDate, LocalDate endDate,
                                  String nameFilter, String sortOption) throws SQLException {
        List<User> users = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return users;
        }

        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        StringBuilder sql = new StringBuilder("SELECT id, username, full_name, email, status, created_at " +
                "FROM users WHERE DATE(created_at) BETWEEN ? AND ?");

        if (nameFilter != null && !nameFilter.isEmpty()) {
            sql.append(" AND (username LIKE ? OR full_name LIKE ?)");
        }

        sql.append(resolveSortClause(sortOption));

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setDate(paramIndex++, Date.valueOf(startDate));
            pstmt.setDate(paramIndex++, Date.valueOf(endDate));

            if (nameFilter != null && !nameFilter.isEmpty()) {
                String pattern = "%" + nameFilter + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserSummary(rs));
                }
            }
        }

        return users;
    }

    private String resolveSortClause(String sortOption) {
        if (sortOption == null) {
            return " ORDER BY created_at DESC";
        }

        switch (sortOption) {
            case "Sắp xếp theo thời gian (Cũ nhất)":
                return " ORDER BY created_at ASC";
            case "Sắp xếp theo tên (A-Z)":
                return " ORDER BY full_name ASC";
            case "Sắp xếp theo tên (Z-A)":
                return " ORDER BY full_name DESC";
            default:
                return " ORDER BY created_at DESC";
        }
    }

    private User extractUserSummary(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }

    /**
     * Lấy danh sách bạn bè của người dùng
     */
    public List<User> getFriendsOfUser(int userId) throws SQLException {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, u.email, u.status, u.created_at " +
                "FROM friendships f " +
                "JOIN users u ON u.id = CASE WHEN f.user1_id = ? THEN f.user2_id ELSE f.user1_id END " +
                "WHERE (f.user1_id = ? OR f.user2_id = ?) AND f.status = 'accepted' " +
                "ORDER BY u.full_name";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(extractUserSummary(rs));
                }
            }
        }

        return friends;
    }
    
    /**
     * Đếm số người dùng mới theo tháng (12 tháng gần nhất)
     */
    public Map<String, Integer> getUserGrowthByMonth() throws SQLException {
        int currentYear = LocalDate.now().getYear();
        Map<String, Integer> growth = new HashMap<>();
        int[] yearData = getUserGrowthByMonth(currentYear);
        for (int month = 1; month <= 12; month++) {
            growth.put(currentYear + "-" + String.format("%02d", month), yearData[month - 1]);
        }
        return growth;
    }

    /**
     * Lấy số người đăng ký mới theo từng tháng của một năm
     */
    public int[] getUserGrowthByMonth(int year) throws SQLException {
        int[] data = new int[12];
        String sql = "SELECT MONTH(created_at) as month, COUNT(*) as cnt " +
                "FROM users WHERE YEAR(created_at) = ? GROUP BY MONTH(created_at)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int monthIndex = rs.getInt("month") - 1;
                    if (monthIndex >= 0 && monthIndex < 12) {
                        data[monthIndex] = rs.getInt("cnt");
                    }
                }
            }
        }

        return data;
    }

    /**
     * Lấy thống kê người dùng hoạt động theo bộ lọc
     */
    public List<UserActivity> getUserActivities(LocalDate startDate, LocalDate endDate,
                                                String activityType, String nameFilter) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return activities;
        }

        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        switch (activityType) {
            case "Chat với người":
                return queryUserActivity("private_messages", "sent_at", "sender_id",
                        startDate, endDate, nameFilter, "Chat với người");
            case "Chat nhóm":
                return queryUserActivity("group_messages", "sent_at", "sender_id",
                        startDate, endDate, nameFilter, "Chat nhóm");
            default:
                return queryUserActivity("login_history", "login_time", "user_id",
                        startDate, endDate, nameFilter, "Mở ứng dụng");
        }
    }

    private List<UserActivity> queryUserActivity(String table, String dateColumn, String userColumn,
                                                 LocalDate startDate, LocalDate endDate,
                                                 String nameFilter, String activityLabel) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT u.id, u.username, u.full_name, COUNT(*) as activity_count, " +
                "MAX(t." + dateColumn + ") as last_activity " +
                "FROM " + table + " t " +
                "JOIN users u ON u.id = t." + userColumn + " " +
                "WHERE t." + dateColumn + " >= ? AND t." + dateColumn + " < ?");

        if (nameFilter != null && !nameFilter.isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.full_name LIKE ?)");
        }

        sql.append(" GROUP BY u.id, u.username, u.full_name ORDER BY activity_count DESC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());

            int idx = 1;
            pstmt.setTimestamp(idx++, startTs);
            pstmt.setTimestamp(idx++, endTs);

            if (nameFilter != null && !nameFilter.isEmpty()) {
                String pattern = "%" + nameFilter + "%";
                pstmt.setString(idx++, pattern);
                pstmt.setString(idx++, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserActivity activity = new UserActivity();
                    activity.setUserId(rs.getInt("id"));
                    activity.setUsername(rs.getString("username"));
                    activity.setFullName(rs.getString("full_name"));
                    activity.setActivityType(activityLabel);
                    activity.setActivityCount(rs.getInt("activity_count"));

                    Timestamp last = rs.getTimestamp("last_activity");
                    if (last != null) {
                        activity.setLastActivity(last.toLocalDateTime());
                    }

                    activities.add(activity);
                }
            }
        }

        return activities;
    }

    /**
     * Số người dùng hoạt động theo tháng (dựa trên đăng nhập) trong một năm
     */
    public int[] getMonthlyActiveUsers(int year) throws SQLException {
        int[] data = new int[12];
        String sql = "SELECT MONTH(login_time) as month, COUNT(DISTINCT user_id) as cnt " +
                "FROM login_history WHERE YEAR(login_time) = ? GROUP BY MONTH(login_time)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int monthIdx = rs.getInt("month") - 1;
                    if (monthIdx >= 0 && monthIdx < 12) {
                        data[monthIdx] = rs.getInt("cnt");
                    }
                }
            }
        }

        return data;
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
