package admin.service;

import admin.socket.ChatGroup;
import admin.socket.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Chat Group
 */
public class GroupDAO {
    private DatabaseConnection dbConnection;

    public GroupDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Lấy tất cả nhóm chat - TỐI ƯU với admin count trong 1 query
     */
    public List<ChatGroup> getAllGroups() throws SQLException {
        List<ChatGroup> groups = new ArrayList<>();

        // Query tối ưu: Lấy tất cả thông tin bao gồm admin_count trong 1 lần
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, g.encrypted, " +
                "COALESCE(u.full_name, 'User #' || g.admin_id) as creator_name, " +
                "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.group_id) as member_count, " +
                // Đếm admin: 1 (admin chính) + số co-admin (nếu có cột is_admin)
                "(1 + COALESCE((SELECT COUNT(*) FROM group_members gm2 " +
                "WHERE gm2.group_id = g.group_id AND gm2.is_admin = true " +
                "AND gm2.user_id != g.admin_id), 0)) as admin_count " +
                "FROM groups g " +
                "LEFT JOIN users u ON g.admin_id = u.user_id " +
                "ORDER BY g.created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ChatGroup group = extractGroupFromResultSet(rs);
                // Thêm admin_count vào object (cần thêm field trong ChatGroup)
                try {
                    group.setAdminCount(rs.getInt("admin_count"));
                } catch (Exception e) {
                    // Nếu ChatGroup chưa có setAdminCount, bỏ qua
                    group.setAdminCount(1); // Default
                }
                groups.add(group);
            }
        } catch (SQLException e) {
            // Nếu query fail (có thể do cột is_admin chưa tồn tại), fall back query cũ
            System.out.println("⚠️ Optimized query failed, using fallback: " + e.getMessage());
            return getAllGroupsFallback();
        }
        return groups;
    }

    /**
     * Fallback method nếu query tối ưu fail
     */
    private List<ChatGroup> getAllGroupsFallback() throws SQLException {
        List<ChatGroup> groups = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, g.encrypted, " +
                "COALESCE(u.full_name, 'User #' || g.admin_id) as creator_name, " +
                "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.group_id) as member_count " +
                "FROM groups g " +
                "LEFT JOIN users u ON g.admin_id = u.user_id " +
                "ORDER BY g.created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ChatGroup group = extractGroupFromResultSet(rs);
                group.setAdminCount(1); // Default fallback
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Tìm kiếm nhóm theo tên
     */
    public List<ChatGroup> searchGroups(String keyword) throws SQLException {
        List<ChatGroup> groups = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, g.encrypted, " +
                "COALESCE(u.full_name, 'User #' || g.admin_id) as creator_name, " +
                "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.group_id) as member_count " +
                "FROM groups g " +
                "LEFT JOIN users u ON g.admin_id = u.user_id " +
                "WHERE g.group_name LIKE ? " +
                "ORDER BY g.created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    groups.add(extractGroupFromResultSet(rs));
                }
            }
        }
        return groups;
    }

    /**
     * Tìm nhóm theo tên hoặc admin
     */
    public List<ChatGroup> searchGroups(String keyword, boolean searchByAdmin) throws SQLException {
        if (!searchByAdmin) {
            return searchGroups(keyword);
        }

        List<ChatGroup> groups = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, g.encrypted, " +
                "COALESCE(u.full_name, 'User #' || g.admin_id) as creator_name, " +
                "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.group_id) as member_count " +
                "FROM groups g " +
                "LEFT JOIN users u ON g.admin_id = u.user_id " +
                "WHERE u.full_name LIKE ? OR u.username LIKE ? " +
                "ORDER BY g.created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    groups.add(extractGroupFromResultSet(rs));
                }
            }
        }
        return groups;
    }

    /**
     * Lấy nhóm theo ID
     */
    public ChatGroup getGroupById(int groupId) throws SQLException {
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, g.encrypted, " +
                "COALESCE(u.full_name, 'User #' || g.admin_id) as creator_name, " +
                "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.group_id) as member_count " +
                "FROM groups g " +
                "LEFT JOIN users u ON g.admin_id = u.user_id " +
                "WHERE g.group_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractGroupFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Đếm tổng số nhóm
     */
    public int getTotalGroups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM groups";

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
     * Xóa nhóm
     */
    public boolean deleteGroup(int groupId) throws SQLException {
        String sql = "DELETE FROM groups WHERE group_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method: Extract Group from ResultSet
     */
    private ChatGroup extractGroupFromResultSet(ResultSet rs) throws SQLException {
        ChatGroup group = new ChatGroup();
        group.setId(rs.getInt("group_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setDescription(""); // Database không có description
        group.setCreatedBy(rs.getInt("admin_id"));
        group.setCreatorName(rs.getString("creator_name"));
        group.setMemberCount(rs.getInt("member_count"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            group.setCreatedAt(createdAt.toLocalDateTime());
        }

        return group;
    }

    /**
     * Lấy thành viên của nhóm
     */
    public List<User> getGroupMembers(int groupId) throws SQLException {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.* FROM group_members gm " +
                "JOIN users u ON gm.user_id = u.user_id " +
                "WHERE gm.group_id = ? ORDER BY u.full_name";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractUserSummary(rs));
                }
            }
        }
        return members;
    }

    /**
     * Lấy danh sách tất cả admin trong nhóm (admin chính + co-admin)
     */
    public List<User> getGroupAdmins(int groupId) throws SQLException {
        List<User> admins = new ArrayList<>();

        // Đầu tiên lấy admin chính từ bảng groups
        String mainAdminSql = "SELECT u.* FROM groups g " +
                "JOIN users u ON g.admin_id = u.user_id " +
                "WHERE g.group_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(mainAdminSql)) {

            pstmt.setInt(1, groupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    admins.add(extractUserSummary(rs));
                }
            }
        }

        // Sau đó lấy thêm co-admin từ group_members.is_admin (nếu column tồn tại)
        try {
            String coAdminSql = "SELECT u.* FROM group_members gm " +
                    "JOIN users u ON gm.user_id = u.user_id " +
                    "JOIN groups g ON gm.group_id = g.group_id " +
                    "WHERE gm.group_id = ? AND gm.is_admin = true AND gm.user_id != g.admin_id";

            try (Connection conn = dbConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(coAdminSql)) {

                pstmt.setInt(1, groupId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        admins.add(extractUserSummary(rs));
                    }
                }
            }
        } catch (SQLException e) {
            // Column is_admin có thể chưa tồn tại, bỏ qua
            System.out.println("Cột is_admin chưa tồn tại hoặc lỗi: " + e.getMessage());
        }

        return admins;
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
     * Đếm số admin của nhóm (admin chính + co-admin)
     */
    public int countGroupAdmins(int groupId) throws SQLException {
        int count = 1; // Admin chính luôn có

        // Đếm thêm co-admin từ group_members.is_admin (nếu column tồn tại)
        try {
            String coAdminSql = "SELECT COUNT(*) as count FROM group_members gm " +
                    "JOIN groups g ON gm.group_id = g.group_id " +
                    "WHERE gm.group_id = ? AND gm.is_admin = true AND gm.user_id != g.admin_id";

            try (Connection conn = dbConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(coAdminSql)) {

                pstmt.setInt(1, groupId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        count += rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            // Column is_admin có thể chưa tồn tại, bỏ qua
            System.out.println("Cột is_admin chưa tồn tại hoặc lỗi: " + e.getMessage());
        }

        return count;
    }
}
