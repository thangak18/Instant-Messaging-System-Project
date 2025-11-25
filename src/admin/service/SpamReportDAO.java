package admin.service;

import admin.socket.SpamReport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Spam Report
 */
public class SpamReportDAO {
    private DatabaseConnection dbConnection;
    
    public SpamReportDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Lấy tất cả báo cáo spam
     */
    public List<SpamReport> getAllSpamReports() throws SQLException {
        List<SpamReport> reports = new ArrayList<>();
        String sql = "SELECT sr.*, " +
                    "u1.full_name as reporter_name, " +
                    "u2.full_name as reported_user_name " +
                    "FROM spam_reports sr " +
                    "JOIN users u1 ON sr.reporter_id = u1.id " +
                    "JOIN users u2 ON sr.reported_user_id = u2.id " +
                    "ORDER BY sr.created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reports.add(extractSpamReportFromResultSet(rs));
            }
        }
        return reports;
    }
    
    /**
     * Lấy báo cáo theo trạng thái
     */
    public List<SpamReport> getReportsByStatus(String status) throws SQLException {
        List<SpamReport> reports = new ArrayList<>();
        String sql = "SELECT sr.*, " +
                    "u1.full_name as reporter_name, " +
                    "u2.full_name as reported_user_name " +
                    "FROM spam_reports sr " +
                    "JOIN users u1 ON sr.reporter_id = u1.id " +
                    "JOIN users u2 ON sr.reported_user_id = u2.id " +
                    "WHERE sr.status = ? " +
                    "ORDER BY sr.created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractSpamReportFromResultSet(rs));
                }
            }
        }
        return reports;
    }
    
    /**
     * Cập nhật trạng thái báo cáo
     */
    public boolean updateReportStatus(int reportId, String status) throws SQLException {
        String sql = "UPDATE spam_reports SET status = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, reportId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Đếm số báo cáo spam
     */
    public int getTotalReports() throws SQLException {
        String sql = "SELECT COUNT(*) FROM spam_reports";
        
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
     * Đếm số báo cáo theo trạng thái
     */
    public int countReportsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM spam_reports WHERE status = ?";
        
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
     * Helper method: Extract SpamReport from ResultSet
     */
    private SpamReport extractSpamReportFromResultSet(ResultSet rs) throws SQLException {
        SpamReport report = new SpamReport();
        report.setId(rs.getInt("id"));
        report.setReporterId(rs.getInt("reporter_id"));
        report.setReporterName(rs.getString("reporter_name"));
        report.setReportedUserId(rs.getInt("reported_user_id"));
        report.setReportedUserName(rs.getString("reported_user_name"));
        report.setReason(rs.getString("reason"));
        report.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            report.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return report;
    }
}
