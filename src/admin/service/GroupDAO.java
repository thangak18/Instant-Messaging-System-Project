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
     * Lấy tất cả nhóm chat
     */
    public List<ChatGroup> getAllGroups() throws SQLException {
        List<ChatGroup> groups = new ArrayList<>();
        String sql = "SELECT cg.*, u.full_name as creator_name, " +
                    "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = cg.id) as member_count " +
                    "FROM chat_groups cg " +
                    "JOIN users u ON cg.created_by = u.id " +
                    "ORDER BY cg.created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                groups.add(extractGroupFromResultSet(rs));
            }
        }
        return groups;
    }
    
    /**
     * Tìm kiếm nhóm theo tên
     */
    public List<ChatGroup> searchGroups(String keyword) throws SQLException {
        List<ChatGroup> groups = new ArrayList<>();
        String sql = "SELECT cg.*, u.full_name as creator_name, " +
                    "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = cg.id) as member_count " +
                    "FROM chat_groups cg " +
                    "JOIN users u ON cg.created_by = u.id " +
                    "WHERE cg.group_name LIKE ? " +
                    "ORDER BY cg.created_at DESC";
        
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
        String sql = "SELECT cg.*, u.full_name as creator_name, " +
                    "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = cg.id) as member_count " +
                    "FROM chat_groups cg " +
                    "JOIN users u ON cg.created_by = u.id " +
                    "WHERE u.full_name LIKE ? OR u.username LIKE ? " +
                    "ORDER BY cg.created_at DESC";

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
        String sql = "SELECT cg.*, u.full_name as creator_name, " +
                    "(SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = cg.id) as member_count " +
                    "FROM chat_groups cg " +
                    "JOIN users u ON cg.created_by = u.id " +
                    "WHERE cg.id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM chat_groups";
        
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
        String sql = "DELETE FROM chat_groups WHERE id = ?";
        
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
        group.setId(rs.getInt("id"));
        group.setGroupName(rs.getString("group_name"));
        group.setDescription(rs.getString("description"));
        group.setCreatedBy(rs.getInt("created_by"));
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
                    "JOIN users u ON gm.user_id = u.id " +
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
     * Lấy danh sách admin trong nhóm (bao gồm owner/admin)
     */
    public List<User> getGroupAdmins(int groupId) throws SQLException {
        List<User> admins = new ArrayList<>();
        String sql = "SELECT u.* FROM group_members gm " +
                    "JOIN users u ON gm.user_id = u.id " +
                    "WHERE gm.group_id = ? AND (gm.role = 'owner' OR gm.role = 'admin') " +
                    "ORDER BY u.full_name";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    admins.add(extractUserSummary(rs));
                }
            }
        }
        return admins;
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
}
