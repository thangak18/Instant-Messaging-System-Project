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
     * Đã tối ưu: tính cả bạn trực tiếp và bạn của bạn trong 1 query
     */
    public List<FriendStats> getFriendStatistics() throws SQLException {
        List<FriendStats> stats = new ArrayList<>();

        // Query tối ưu: lấy số bạn trực tiếp
        String sql = "SELECT u.user_id, u.username, u.full_name, u.created_at, " +
                "COUNT(DISTINCT CASE WHEN f.user_id = u.user_id THEN f.friend_id " +
                "                    WHEN f.friend_id = u.user_id THEN f.user_id END) as friend_count " +
                "FROM users u " +
                "LEFT JOIN friends f ON (u.user_id = f.user_id OR u.user_id = f.friend_id) " +
                "AND f.status = 'accepted' " +
                "GROUP BY u.user_id, u.username, u.full_name, u.created_at " +
                "ORDER BY friend_count DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FriendStats stat = new FriendStats();
                stat.setUserId(rs.getInt("user_id"));
                stat.setUsername(rs.getString("username"));
                stat.setFullName(rs.getString("full_name"));
                stat.setFriendCount(rs.getInt("friend_count"));
                stat.setFriendsOfFriendsCount(0); // Sẽ tính sau

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    stat.setCreatedAt(createdAt.toLocalDateTime());
                }

                stats.add(stat);
            }
        }

        // Tính số bạn của bạn trong 1 query duy nhất cho tất cả users
        Map<Integer, Integer> fofCounts = countAllFriendsOfFriends();
        for (FriendStats stat : stats) {
            stat.setFriendsOfFriendsCount(fofCounts.getOrDefault(stat.getUserId(), 0));
        }

        return stats;
    }

    /**
     * Đếm số bạn của bạn cho TẤT CẢ users trong 1 query duy nhất
     */
    private Map<Integer, Integer> countAllFriendsOfFriends() throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();

        // Query tối ưu: tính friends of friends cho tất cả users cùng lúc
        String sql = "WITH direct_friends AS (" +
                "  SELECT user_id as uid, friend_id as fid FROM friends WHERE status = 'accepted' " +
                "  UNION " +
                "  SELECT friend_id as uid, user_id as fid FROM friends WHERE status = 'accepted' " +
                "), " +
                "friends_of_friends AS (" +
                "  SELECT df1.uid as user_id, df2.fid as fof_id " +
                "  FROM direct_friends df1 " +
                "  JOIN direct_friends df2 ON df1.fid = df2.uid " +
                "  WHERE df2.fid != df1.uid " + // Không tính chính mình
                ") " +
                "SELECT user_id, COUNT(DISTINCT fof_id) as fof_count " +
                "FROM friends_of_friends " +
                "GROUP BY user_id";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.put(rs.getInt("user_id"), rs.getInt("fof_count"));
            }
        }

        return result;
    }

    /**
     * Lấy thống kê bạn bè với bộ lọc đầy đủ
     * Đã tối ưu: chỉ cần 2 queries thay vì N+1
     */
    public List<FriendStats> getFriendStatisticsWithFilters(String nameFilter,
            String comparison,
            Integer friendCount,
            String sortOption) throws SQLException {
        List<FriendStats> stats = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.username, u.full_name, u.created_at, " +
                        "COUNT(DISTINCT CASE WHEN f.user_id = u.user_id THEN f.friend_id " +
                        "                    WHEN f.friend_id = u.user_id THEN f.user_id END) as friend_count " +
                        "FROM users u " +
                        "LEFT JOIN friends f ON (u.user_id = f.user_id OR u.user_id = f.friend_id) " +
                        "AND f.status = 'accepted' " +
                        "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Lọc theo tên
        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + nameFilter.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" GROUP BY u.user_id, u.username, u.full_name, u.created_at");

        // Lọc theo số lượng bạn (HAVING clause)
        if (comparison != null && !"Tất cả".equals(comparison) && friendCount != null) {
            String havingClause = " HAVING COUNT(DISTINCT CASE WHEN f.user_id = u.user_id THEN f.friend_id " +
                    "WHEN f.friend_id = u.user_id THEN f.user_id END)";
            switch (comparison) {
                case "=":
                    sql.append(havingClause + " = ?");
                    params.add(friendCount);
                    break;
                case ">":
                    sql.append(havingClause + " > ?");
                    params.add(friendCount);
                    break;
                case "<":
                    sql.append(havingClause + " < ?");
                    params.add(friendCount);
                    break;
            }
        }

        // Sắp xếp
        if (sortOption != null) {
            switch (sortOption) {
                case "Sắp xếp theo tên (A-Z)":
                    sql.append(" ORDER BY u.full_name ASC");
                    break;
                case "Sắp xếp theo tên (Z-A)":
                    sql.append(" ORDER BY u.full_name DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Mới nhất)":
                    sql.append(" ORDER BY u.created_at DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Cũ nhất)":
                default:
                    sql.append(" ORDER BY u.created_at ASC");
            }
        } else {
            sql.append(" ORDER BY u.created_at DESC");
        }

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FriendStats stat = new FriendStats();
                    stat.setUserId(rs.getInt("user_id"));
                    stat.setUsername(rs.getString("username"));
                    stat.setFullName(rs.getString("full_name"));
                    stat.setFriendCount(rs.getInt("friend_count"));
                    stat.setFriendsOfFriendsCount(0);

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        stat.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    stats.add(stat);
                }
            }
        }

        // Tính số bạn của bạn - 1 query cho tất cả
        Map<Integer, Integer> fofCounts = countAllFriendsOfFriends();
        for (FriendStats stat : stats) {
            stat.setFriendsOfFriendsCount(fofCounts.getOrDefault(stat.getUserId(), 0));
        }

        return stats;
    }

    /**
     * Lấy danh sách người dùng hoạt động (có tin nhắn trong 30 ngày)
     */
    public List<UserActivity> getActiveUsers(int days) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.full_name, " +
                "COUNT(DISTINCT pm.message_id) + COUNT(DISTINCT gm.message_id) as activity_count, " +
                "GREATEST(MAX(pm.created_at), MAX(gm.sent_time)) as last_activity " +
                "FROM users u " +
                "LEFT JOIN messages pm ON (u.user_id = pm.sender_id) " +
                "AND pm.created_at >= NOW() - (? * INTERVAL '1 day') " +
                "LEFT JOIN group_messages gm ON (u.user_id = gm.sender_id) " +
                "AND gm.sent_time >= NOW() - (? * INTERVAL '1 day') " +
                "WHERE u.status = 'active' " +
                "GROUP BY u.user_id, u.username, u.full_name " +
                "HAVING COUNT(DISTINCT pm.message_id) + COUNT(DISTINCT gm.message_id) > 0 " +
                "ORDER BY activity_count DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            pstmt.setInt(2, days);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserActivity activity = new UserActivity();
                    activity.setUserId(rs.getInt("user_id"));
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
        String sql = "SELECT user_id, username, full_name, email, status, created_at " +
                "FROM users " +
                "WHERE created_at >= NOW() - (? * INTERVAL '1 day') " +
                "ORDER BY created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    int id = rs.getInt("user_id");
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
            String nameFilter, String emailFilter, String sortOption) throws SQLException {
        List<User> users = new ArrayList<>();
        
        // Nếu cả startDate và endDate đều null, load tất cả users
        boolean loadAll = (startDate == null && endDate == null);
        
        if (!loadAll && (startDate == null || endDate == null)) {
            return users;
        }

        if (!loadAll && endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        StringBuilder sql = new StringBuilder("SELECT user_id, username, full_name, email, status, created_at " +
                "FROM users");
        
        // Chỉ thêm WHERE nếu có date filter
        if (!loadAll) {
            sql.append(" WHERE DATE(created_at) BETWEEN ? AND ?");
        } else {
            sql.append(" WHERE 1=1"); // Load tất cả
        }

        if (nameFilter != null && !nameFilter.isEmpty()) {
            sql.append(" AND (username LIKE ? OR full_name LIKE ?)");
        }
        
        if (emailFilter != null && !emailFilter.isEmpty()) {
            sql.append(" AND email LIKE ?");
        }

        sql.append(resolveSortClause(sortOption));

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            // Chỉ set date parameters nếu không phải load all
            if (!loadAll) {
                pstmt.setDate(paramIndex++, Date.valueOf(startDate));
                pstmt.setDate(paramIndex++, Date.valueOf(endDate));
            }

            if (nameFilter != null && !nameFilter.isEmpty()) {
                String pattern = "%" + nameFilter + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }
            
            if (emailFilter != null && !emailFilter.isEmpty()) {
                String pattern = "%" + emailFilter + "%";
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
            case "Sắp xếp theo email (A-Z)":
                return " ORDER BY email ASC";
            case "Sắp xếp theo email (Z-A)":
                return " ORDER BY email DESC";
            default:
                return " ORDER BY created_at DESC";
        }
    }

    private User extractUserSummary(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
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
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.status, u.created_at " +
                "FROM friends f " +
                "JOIN users u ON u.user_id = CASE WHEN f.user_id = ? THEN f.friend_id ELSE f.user_id END " +
                "WHERE (f.user_id = ? OR f.friend_id = ?) AND f.status = 'accepted' " +
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
        String sql = "SELECT EXTRACT(MONTH FROM created_at) as month, COUNT(*) as cnt " +
                "FROM users WHERE EXTRACT(YEAR FROM created_at) = ? " +
                "GROUP BY EXTRACT(MONTH FROM created_at) ORDER BY month";

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
                return queryUserActivity("messages", "created_at", "sender_id",
                        startDate, endDate, nameFilter, "Chat với người");
            case "Chat nhóm":
                return queryUserActivity("group_messages", "sent_time", "sender_id",
                        startDate, endDate, nameFilter, "Chat nhóm");
            default:
                return queryUserActivity("login_history", "login_time", "user_id",
                        startDate, endDate, nameFilter, "Mở ứng dụng");
        }
    }

    private List<UserActivity> queryUserActivity(String table, String dateColumn, String userColumn,
            LocalDate startDate, LocalDate endDate,
            String nameFilter, String activityLabel) throws SQLException {
        return queryUserActivityWithFilters(table, dateColumn, userColumn, startDate, endDate,
                nameFilter, activityLabel, null, null, null);
    }

    /**
     * Query user activities với đầy đủ bộ lọc
     */
    private List<UserActivity> queryUserActivityWithFilters(String table, String dateColumn, String userColumn,
            LocalDate startDate, LocalDate endDate,
            String nameFilter, String activityLabel,
            String comparison, Integer activityCount,
            String sortOption) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT u.user_id, u.username, u.full_name, u.created_at, " +
                "COUNT(*) as activity_count, MAX(t." + dateColumn + ") as last_activity " +
                "FROM " + table + " t " +
                "JOIN users u ON u.user_id = t." + userColumn + " " +
                "WHERE t." + dateColumn + " >= ? AND t." + dateColumn + " < ?");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        params.add(Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

        if (nameFilter != null && !nameFilter.isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + nameFilter + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" GROUP BY u.user_id, u.username, u.full_name, u.created_at");

        // Lọc theo số lượng hoạt động (HAVING)
        if (comparison != null && !"Tất cả".equals(comparison) && activityCount != null) {
            switch (comparison) {
                case "=":
                    sql.append(" HAVING COUNT(*) = ?");
                    params.add(activityCount);
                    break;
                case ">":
                    sql.append(" HAVING COUNT(*) > ?");
                    params.add(activityCount);
                    break;
                case "<":
                    sql.append(" HAVING COUNT(*) < ?");
                    params.add(activityCount);
                    break;
            }
        }

        // Sắp xếp
        if (sortOption != null) {
            switch (sortOption) {
                case "Sắp xếp theo tên (A-Z)":
                    sql.append(" ORDER BY u.full_name ASC");
                    break;
                case "Sắp xếp theo tên (Z-A)":
                    sql.append(" ORDER BY u.full_name DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Mới nhất)":
                    sql.append(" ORDER BY u.created_at DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Cũ nhất)":
                    sql.append(" ORDER BY u.created_at ASC");
                    break;
                default:
                    sql.append(" ORDER BY activity_count DESC");
            }
        } else {
            sql.append(" ORDER BY activity_count DESC");
        }

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserActivity activity = new UserActivity();
                    activity.setUserId(rs.getInt("user_id"));
                    activity.setUsername(rs.getString("username"));
                    activity.setFullName(rs.getString("full_name"));
                    activity.setActivityType(activityLabel);
                    activity.setActivityCount(rs.getInt("activity_count"));

                    Timestamp last = rs.getTimestamp("last_activity");
                    if (last != null) {
                        activity.setLastActivity(last.toLocalDateTime());
                    }

                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        activity.setCreatedAt(created.toLocalDateTime());
                    }

                    activities.add(activity);
                }
            }
        }

        return activities;
    }

    /**
     * Lấy danh sách hoạt động người dùng với đầy đủ bộ lọc
     */
    public List<UserActivity> getUserActivitiesWithFilters(LocalDate startDate, LocalDate endDate,
            String activityType, String nameFilter,
            String comparison, Integer activityCount,
            String sortOption) throws SQLException {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        switch (activityType) {
            case "Chat với người":
                return queryUserActivityWithFilters("messages", "created_at", "sender_id",
                        startDate, endDate, nameFilter, "Chat với người", comparison, activityCount, sortOption);
            case "Chat nhóm":
                return queryUserActivityWithFilters("group_messages", "sent_time", "sender_id",
                        startDate, endDate, nameFilter, "Chat nhóm", comparison, activityCount, sortOption);
            default:
                return queryUserActivityWithFilters("login_history", "login_time", "user_id",
                        startDate, endDate, nameFilter, "Mở ứng dụng", comparison, activityCount, sortOption);
        }
    }

    /**
     * Số người dùng hoạt động theo tháng TẠO TÀI KHOẢN (và có login) trong một năm
     * Đếm user theo tháng created_at, nhưng chỉ user có ít nhất 1 lần login
     */
    public int[] getMonthlyActiveUsers(int year) throws SQLException {
        int[] data = new int[12];
        String sql = "SELECT EXTRACT(MONTH FROM u.created_at) as month, COUNT(DISTINCT u.user_id) as cnt " +
                "FROM users u " +
                "INNER JOIN login_history lh ON u.user_id = lh.user_id " +
                "WHERE EXTRACT(YEAR FROM u.created_at) = ? " +
                "GROUP BY EXTRACT(MONTH FROM u.created_at) " +
                "ORDER BY month";

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
     * Tổng số người dùng tạo tài khoản trong năm VÀ có hoạt động login
     */
    public int getTotalActiveUsersInYear(int year) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT u.user_id) as total " +
                "FROM users u " +
                "INNER JOIN login_history lh ON u.user_id = lh.user_id " +
                "WHERE EXTRACT(YEAR FROM u.created_at) = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    /**
     * Đếm số đăng nhập theo ngày (7 ngày gần nhất)
     */
    public Map<String, Integer> getLoginCountByDay(int days) throws SQLException {
        Map<String, Integer> loginCounts = new HashMap<>();
        String sql = "SELECT DATE(login_time) as login_date, COUNT(*) as count " +
                "FROM login_history " +
                "WHERE login_time >= NOW() - (? * INTERVAL '1 day') " +
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
            if (rs.next())
                overview.put("totalUsers", rs.getInt(1));

            // User active
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE status = 'active'");
            if (rs.next())
                overview.put("activeUsers", rs.getInt(1));

            // Tổng nhóm
            rs = stmt.executeQuery("SELECT COUNT(*) FROM groups");
            if (rs.next())
                overview.put("totalGroups", rs.getInt(1));

            // Tổng tin nhắn
            rs = stmt.executeQuery("SELECT COUNT(*) FROM messages");
            if (rs.next())
                overview.put("totalMessages", rs.getInt(1));

            // Báo cáo spam pending
            rs = stmt.executeQuery("SELECT COUNT(*) FROM spam_reports WHERE status = 'pending'");
            if (rs.next())
                overview.put("pendingReports", rs.getInt(1));
        }

        return overview;
    }

    /**
     * Lấy thống kê comprehensive user activities - tất cả 3 loại hoạt động trong 1
     * query
     * 
     * @return List of UserActivity với loginCount, privateChatCount, groupChatCount
     */
    public List<UserActivity> getUserActivitiesComprehensive(LocalDate startDate, LocalDate endDate,
            String nameFilter, String comparison,
            Integer totalActivityCount, String sortOption) throws SQLException {
        List<UserActivity> activities = new ArrayList<>();
        
        // Nếu cả startDate và endDate đều null, load tất cả users
        boolean loadAll = (startDate == null && endDate == null);
        
        if (!loadAll && (startDate == null || endDate == null)) {
            return activities;
        }

        if (!loadAll && endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        // Complex query to aggregate all 3 activity types
        // Filter by user creation date, not activity date
        StringBuilder sql = new StringBuilder(
                "WITH user_logins AS (" +
                        "  SELECT user_id, COUNT(*) as login_count, MAX(login_time) as last_login " +
                        "  FROM login_history " +
                        "  GROUP BY user_id" +
                        "), " +
                        "user_private_chats AS (" +
                        "  SELECT sender_id as user_id, COUNT(*) as chat_count " +
                        "  FROM messages " +
                        "  GROUP BY sender_id" +
                        "), " +
                        "user_group_chats AS (" +
                        "  SELECT sender_id as user_id, COUNT(*) as group_count " +
                        "  FROM group_messages " +
                        "  GROUP BY sender_id" +
                        ") " +
                        "SELECT u.user_id, u.username, u.full_name, u.created_at, " +
                        "  COALESCE(ul.login_count, 0) as login_count, " +
                        "  COALESCE(ul.last_login, u.created_at) as last_activity, " +
                        "  COALESCE(upc.chat_count, 0) as private_chat_count, " +
                        "  COALESCE(ugc.group_count, 0) as group_chat_count, " +
                        "  (COALESCE(ul.login_count, 0) + COALESCE(upc.chat_count, 0) + COALESCE(ugc.group_count, 0)) as total_count "
                        +
                        "FROM users u " +
                        "LEFT JOIN user_logins ul ON u.user_id = ul.user_id " +
                        "LEFT JOIN user_private_chats upc ON u.user_id = upc.user_id " +
                        "LEFT JOIN user_group_chats ugc ON u.user_id = ugc.user_id " +
                        "WHERE 1=1");
        
        // Chỉ thêm date filter nếu không phải load all
        if (!loadAll) {
            sql.append(" AND u.created_at >= ? AND u.created_at < ?");
        }
        
        sql.append(" AND (ul.login_count IS NOT NULL OR upc.chat_count IS NOT NULL OR ugc.group_count IS NOT NULL)");

        List<Object> params = new ArrayList<>();
        // Parameters for date filtering on user creation - chỉ thêm nếu không phải load all
        if (!loadAll) {
            Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
            params.add(startTs); // user created_at >= start
            params.add(endTs); // user created_at < end
        }

        // Name filter
        if (nameFilter != null && !nameFilter.isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + nameFilter + "%";
            params.add(pattern);
            params.add(pattern);
        }

        // Filter by login count (Mở ứng dụng) only
        if (comparison != null && !"Tất cả".equals(comparison) && totalActivityCount != null) {
            switch (comparison) {
                case "=":
                    sql.append(" AND COALESCE(ul.login_count, 0) = ?");
                    params.add(totalActivityCount);
                    break;
                case ">":
                    sql.append(" AND COALESCE(ul.login_count, 0) > ?");
                    params.add(totalActivityCount);
                    break;
                case "<":
                    sql.append(" AND COALESCE(ul.login_count, 0) < ?");
                    params.add(totalActivityCount);
                    break;
            }
        }

        // Sorting
        if (sortOption != null) {
            switch (sortOption) {
                case "Sắp xếp theo tên (A-Z)":
                    sql.append(" ORDER BY u.full_name ASC");
                    break;
                case "Sắp xếp theo tên (Z-A)":
                    sql.append(" ORDER BY u.full_name DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Mới nhất)":
                    sql.append(" ORDER BY u.created_at DESC");
                    break;
                case "Sắp xếp theo thời gian tạo (Cũ nhất)":
                    sql.append(" ORDER BY u.created_at ASC");
                    break;
                default:
                    sql.append(" ORDER BY total_count DESC");
            }
        } else {
            sql.append(" ORDER BY total_count DESC");
        }

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserActivity activity = new UserActivity();
                    activity.setUserId(rs.getInt("user_id"));
                    activity.setUsername(rs.getString("username"));
                    activity.setFullName(rs.getString("full_name"));

                    // Set all 3 activity counts
                    activity.setLoginCount(rs.getInt("login_count"));
                    activity.setPrivateChatCount(rs.getInt("private_chat_count"));
                    activity.setGroupChatCount(rs.getInt("group_chat_count"));
                    activity.setActivityCount(rs.getInt("total_count"));

                    Timestamp last = rs.getTimestamp("last_activity");
                    if (last != null) {
                        activity.setLastActivity(last.toLocalDateTime());
                    }

                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        activity.setCreatedAt(created.toLocalDateTime());
                    }

                    activities.add(activity);
                }
            }
        }

        return activities;
    }

    /**
     * Dashboard Statistics - Get total user count
     */
    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM users WHERE status IN ('active', 'locked')";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Dashboard Statistics - Get online users count
     * Xác định user online dựa trên last_login trong bảng users
     * User được coi là online nếu last_login trong vòng 5 phút gần nhất
     * và status = 'active'
     */
    public int getOnlineUsers() throws SQLException {
        // Sử dụng last_login từ bảng users để xác định user online
        // User online nếu: last_login trong 5 phút gần nhất VÀ status = 'active'
        String sql = "SELECT COUNT(*) as total FROM users " +
                "WHERE status = 'active' " +
                "AND last_login IS NOT NULL " +
                "AND last_login >= NOW() - INTERVAL '5 minutes'";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Dashboard Statistics - Get total group count
     */
    public int getTotalGroups() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM groups";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Dashboard Statistics - Get total message count (private + group)
     */
    public int getTotalMessages() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM messages) + " +
                "(SELECT COUNT(*) FROM group_messages) as total";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}
