package user.service;

import java.sql.*;
import java.util.*;

/**
 * Service xử lý các nghiệp vụ liên quan đến Group Chat
 */
public class GroupService {
    
    private DatabaseConnection dbConnection;
    
    public GroupService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * TẠO NHÓM CHAT MỚI
     * @param groupName Tên nhóm
     * @param description Mô tả nhóm (bỏ qua vì DB không hỗ trợ)
     * @param creatorUsername Username người tạo
     * @param memberUsernames Danh sách username thành viên (tối thiểu 1 người)
     * @return group_id nếu thành công, -1 nếu thất bại
     */
    public int createGroup(String groupName, String description, String creatorUsername, List<String> memberUsernames) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return -1;
            
            conn.setAutoCommit(false); // Start transaction
            
            // Lấy user_id của creator
            int creatorId = getUserId(conn, creatorUsername);
            if (creatorId == -1) {
                conn.rollback();
                return -1;
            }
            
            // 1. Tạo nhóm (admin_id là creator)
            String createGroupSQL = "INSERT INTO groups (group_name, admin_id, created_at) " +
                                   "VALUES (?, ?, CURRENT_TIMESTAMP) RETURNING group_id";
            
            pstmt = conn.prepareStatement(createGroupSQL);
            pstmt.setString(1, groupName);
            pstmt.setInt(2, creatorId);
            
            rs = pstmt.executeQuery();
            int groupId = -1;
            if (rs.next()) {
                groupId = rs.getInt(1);
            }
            
            if (groupId == -1) {
                conn.rollback();
                return -1;
            }
            
            // 2. Thêm creator vào group_members
            String addCreatorSQL = "INSERT INTO group_members (group_id, user_id, joined_at) " +
                                  "VALUES (?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement ps = conn.prepareStatement(addCreatorSQL)) {
                ps.setInt(1, groupId);
                ps.setInt(2, creatorId);
                ps.executeUpdate();
            }
            
            // 3. Thêm các thành viên khác
            if (memberUsernames != null && !memberUsernames.isEmpty()) {
                String addMemberSQL = "INSERT INTO group_members (group_id, user_id, joined_at) " +
                                     "VALUES (?, (SELECT user_id FROM users WHERE username = ?), CURRENT_TIMESTAMP)";
                
                try (PreparedStatement ps = conn.prepareStatement(addMemberSQL)) {
                    for (String username : memberUsernames) {
                        if (!username.equals(creatorUsername)) { // Tránh thêm creator 2 lần
                            ps.setInt(1, groupId);
                            ps.setString(2, username);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }
            
            conn.commit();
            System.out.println("✅ Đã tạo nhóm: " + groupName + " (ID: " + groupId + ")");
            return groupId;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tạo nhóm: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
            return -1;
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { }
                DatabaseConnection.closeConnection(conn);
            }
        }
    }
    
    /**
     * ĐỔI TÊN NHÓM CHAT
     */
    public boolean renameGroup(int groupId, String newName, String username) {
        // Kiểm tra quyền admin
        if (!isAdmin(groupId, username)) {
            System.err.println("❌ Chỉ admin mới được đổi tên nhóm");
            return false;
        }
        
        String sql = "UPDATE groups SET group_name = ? WHERE group_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newName);
            pstmt.setInt(2, groupId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã đổi tên nhóm: " + newName);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đổi tên nhóm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * THÊM THÀNH VIÊN VÀO NHÓM
     */
    public boolean addMember(int groupId, String newMemberUsername, String adderUsername) {
        // Kiểm tra người thêm có phải thành viên không
        if (!isMember(groupId, adderUsername)) {
            System.err.println("❌ Bạn không phải thành viên của nhóm này");
            return false;
        }
        
        String sql = "INSERT INTO group_members (group_id, user_id, joined_at) " +
                     "VALUES (?, (SELECT user_id FROM users WHERE username = ?), CURRENT_TIMESTAMP)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, newMemberUsername);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã thêm thành viên: " + newMemberUsername);
                return true;
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                System.err.println("❌ Thành viên đã có trong nhóm");
            } else {
                System.err.println("❌ Lỗi khi thêm thành viên: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * GÁN QUYỀN ADMIN CHO THÀNH VIÊN (thay đổi admin_id)
     */
    public boolean promoteToAdmin(int groupId, String targetUsername, String promoterUsername) {
        // Kiểm tra người gán có phải admin không
        if (!isAdmin(groupId, promoterUsername)) {
            System.err.println("❌ Chỉ admin mới có quyền gán admin");
            return false;
        }
        
        // Kiểm tra target có phải thành viên không
        if (!isMember(groupId, targetUsername)) {
            System.err.println("❌ Người dùng không phải thành viên của nhóm");
            return false;
        }
        
        String sql = "UPDATE groups SET admin_id = (SELECT user_id FROM users WHERE username = ?) " +
                     "WHERE group_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, targetUsername);
            pstmt.setInt(2, groupId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã gán quyền admin cho: " + targetUsername);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi gán quyền admin: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * XÓA THÀNH VIÊN KHỎI NHÓM (chỉ admin)
     */
    public boolean removeMember(int groupId, String targetUsername, String removerUsername) {
        // Kiểm tra người xóa có phải admin không
        if (!isAdmin(groupId, removerUsername)) {
            System.err.println("❌ Chỉ admin mới có quyền xóa thành viên");
            return false;
        }
        
        // Không cho phép xóa chính mình nếu là admin duy nhất
        if (targetUsername.equals(removerUsername)) {
            int adminCount = countAdmins(groupId);
            if (adminCount <= 1) {
                System.err.println("❌ Không thể rời nhóm khi là admin duy nhất. Hãy gán admin cho người khác trước.");
                return false;
            }
        }
        
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = (SELECT user_id FROM users WHERE username = ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, targetUsername);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã xóa thành viên: " + targetUsername);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi xóa thành viên: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * GỬI TIN NHẮN NHÓM
     */
    public boolean sendGroupMessage(int groupId, String senderUsername, String content) {
        // Kiểm tra có phải thành viên không
        if (!isMember(groupId, senderUsername)) {
            System.err.println("❌ Bạn không phải thành viên của nhóm này");
            return false;
        }
        
        String sql = "INSERT INTO group_messages (group_id, sender_id, message_text, sent_time) " +
                     "VALUES (?, (SELECT user_id FROM users WHERE username = ?), ?, CURRENT_TIMESTAMP)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, senderUsername);
            pstmt.setString(3, content);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Đã gửi tin nhắn nhóm");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi gửi tin nhắn nhóm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * LẤY DANH SÁCH TIN NHẮN NHÓM
     */
    public List<Map<String, Object>> getGroupMessages(int groupId, String username) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Kiểm tra quyền thành viên
        if (!isMember(groupId, username)) {
            System.err.println("❌ Bạn không phải thành viên của nhóm này");
            return messages;
        }
        
        return getGroupMessages(groupId);
    }
    
    /**
     * LẤY DANH SÁCH TIN NHẮN NHÓM (không kiểm tra quyền)
     */
    public List<Map<String, Object>> getGroupMessages(int groupId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        String sql = "SELECT gm.message_id, gm.message_text, gm.sent_time, u.username, u.full_name " +
                     "FROM group_messages gm " +
                     "JOIN users u ON gm.sender_id = u.user_id " +
                     "WHERE gm.group_id = ? " +
                     "ORDER BY gm.sent_time ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return messages;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("message_id", rs.getInt("message_id"));
                msg.put("message", rs.getString("message_text"));
                msg.put("sent_at", rs.getTimestamp("sent_time").toLocalDateTime());
                msg.put("sender_username", rs.getString("username"));
                msg.put("sender_full_name", rs.getString("full_name"));
                messages.add(msg);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy tin nhắn nhóm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return messages;
    }
    
    /**
     * LẤY DANH SÁCH NHÓM CỦA USER
     */
    public List<Map<String, Object>> getUserGroups(String username) {
        List<Map<String, Object>> groups = new ArrayList<>();
        
        String sql = "SELECT g.group_id, g.group_name, g.created_at, g.admin_id, " +
                     "(SELECT COUNT(*) FROM group_members WHERE group_id = g.group_id) as member_count, " +
                     "u.user_id " +
                     "FROM groups g " +
                     "JOIN group_members gm ON g.group_id = gm.group_id " +
                     "JOIN users u ON gm.user_id = u.user_id " +
                     "WHERE u.username = ? " +
                     "ORDER BY g.created_at DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return groups;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> group = new HashMap<>();
                int groupId = rs.getInt("group_id");
                int adminId = rs.getInt("admin_id");
                int userId = rs.getInt("user_id");
                
                group.put("id", groupId);
                group.put("group_name", rs.getString("group_name"));
                group.put("created_at", rs.getTimestamp("created_at"));
                group.put("role", (adminId == userId) ? "admin" : "member");
                group.put("member_count", rs.getInt("member_count"));
                groups.add(group);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách nhóm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return groups;
    }
    
    /**
     * LẤY DANH SÁCH THÀNH VIÊN NHÓM
     */
    public List<Map<String, Object>> getGroupMembers(int groupId) {
        List<Map<String, Object>> members = new ArrayList<>();
        
        String sql = "SELECT u.user_id, u.username, u.full_name, gm.joined_at, g.admin_id " +
                     "FROM group_members gm " +
                     "JOIN users u ON gm.user_id = u.user_id " +
                     "JOIN groups g ON gm.group_id = g.group_id " +
                     "WHERE gm.group_id = ? " +
                     "ORDER BY gm.joined_at ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return members;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> member = new HashMap<>();
                int userId = rs.getInt("user_id");
                int adminId = rs.getInt("admin_id");
                
                member.put("user_id", userId);
                member.put("username", rs.getString("username"));
                member.put("full_name", rs.getString("full_name"));
                member.put("role", (userId == adminId) ? "admin" : "member");
                member.put("joined_at", rs.getTimestamp("joined_at"));
                members.add(member);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách thành viên: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return members;
    }
    
    /**
     * LẤY THÔNG TIN NHÓM
     */
    public Map<String, Object> getGroupInfo(int groupId) {
        Map<String, Object> groupInfo = new HashMap<>();
        
        String sql = "SELECT g.group_id, g.group_name, g.admin_id, g.created_at, " +
                     "u.username as created_by, u.full_name as creator_name, " +
                     "(SELECT COUNT(*) FROM group_members WHERE group_id = g.group_id) as member_count " +
                     "FROM groups g " +
                     "JOIN users u ON g.admin_id = u.user_id " +
                     "WHERE g.group_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return groupInfo;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                groupInfo.put("group_id", rs.getInt("group_id"));
                groupInfo.put("group_name", rs.getString("group_name"));
                groupInfo.put("created_by", rs.getString("created_by"));
                groupInfo.put("creator_name", rs.getString("creator_name"));
                groupInfo.put("created_at", rs.getTimestamp("created_at"));
                groupInfo.put("member_count", rs.getInt("member_count"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy thông tin nhóm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return groupInfo;
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Kiểm tra user có phải admin của nhóm không
     */
    public boolean isAdmin(int groupId, String username) {
        String sql = "SELECT g.admin_id, u.user_id " +
                     "FROM groups g, users u " +
                     "WHERE g.group_id = ? AND u.username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, username);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int adminId = rs.getInt("admin_id");
                int userId = rs.getInt("user_id");
                return adminId == userId;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Kiểm tra user có phải thành viên của nhóm không
     */
    public boolean isMember(int groupId, String username) {
        String sql = "SELECT COUNT(*) FROM group_members " +
                     "WHERE group_id = ? AND user_id = (SELECT user_id FROM users WHERE username = ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            if (conn == null) return false;
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, username);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Đếm số admin trong nhóm (chỉ có 1 admin là admin_id)
     */
    public int countAdmins(int groupId) {
        // Với schema hiện tại, chỉ có 1 admin (admin_id)
        return 1;
    }
    
    /**
     * Lấy user_id từ username
     */
    private int getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        
        return -1;
    }
}
