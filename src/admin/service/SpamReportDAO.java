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
        String sql = "SELECT sr.report_id, sr.reporter_id, sr.reported_user_id, sr.reason, " +
                "sr.report_time, sr.status, " +
                "COALESCE(u1.full_name, 'User #' || sr.reporter_id) as reporter_name, " +
                "COALESCE(u2.full_name, 'User #' || sr.reported_user_id) as reported_user_name " +
                "FROM spam_reports sr " +
                "LEFT JOIN users u1 ON sr.reporter_id = u1.user_id " +
                "LEFT JOIN users u2 ON sr.reported_user_id = u2.user_id " +
                "ORDER BY sr.report_time DESC";

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
        String sql = "SELECT sr.report_id, sr.reporter_id, sr.reported_user_id, sr.reason, " +
                "sr.report_time, sr.status, " +
                "COALESCE(u1.full_name, 'User #' || sr.reporter_id) as reporter_name, " +
                "COALESCE(u2.full_name, 'User #' || sr.reported_user_id) as reported_user_name " +
                "FROM spam_reports sr " +
                "LEFT JOIN users u1 ON sr.reporter_id = u1.user_id " +
                "LEFT JOIN users u2 ON sr.reported_user_id = u2.user_id " +
                "WHERE sr.status = ? " +
                "ORDER BY sr.report_time DESC";

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
        String sql = "UPDATE spam_reports SET status = ? WHERE report_id = ?";

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
     * Tìm kiếm báo cáo spam với bộ lọc đầy đủ
     * 
     * @param searchType: "reporter" hoặc "reported"
     * @param keyword:    từ khóa tìm kiếm
     * @param timeFilter: "all", "today", "7days", "30days", "month"
     * @param status:     "all", "pending", "resolved", "rejected"
     * @param sortOption: cách sắp xếp
     */
    public List<SpamReport> searchSpamReports(String searchType, String keyword,
            String timeFilter, String status,
            String sortOption) throws SQLException {
        List<SpamReport> reports = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT sr.report_id, sr.reporter_id, sr.reported_user_id, sr.reason, " +
                        "sr.report_time, sr.status, " +
                        "COALESCE(u1.full_name, 'User #' || sr.reporter_id) as reporter_name, u1.username as reporter_username, "
                        +
                        "COALESCE(u2.full_name, 'User #' || sr.reported_user_id) as reported_user_name, u2.username as reported_username "
                        +
                        "FROM spam_reports sr " +
                        "LEFT JOIN users u1 ON sr.reporter_id = u1.user_id " +
                        "LEFT JOIN users u2 ON sr.reported_user_id = u2.user_id WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Lọc theo từ khóa (tên đăng nhập hoặc email)
        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("reporter".equals(searchType)) {
                sql.append(" AND (u1.full_name LIKE ? OR u1.username LIKE ? OR u1.email LIKE ?)");
            } else {
                sql.append(" AND (u2.full_name LIKE ? OR u2.username LIKE ? OR u2.email LIKE ?)");
            }
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        // Lọc theo thời gian
        if (timeFilter != null && !"Tất cả thời gian".equals(timeFilter)) {
            switch (timeFilter) {
                case "Hôm nay":
                    sql.append(" AND DATE(sr.report_time) = CURRENT_DATE");
                    break;
                case "7 ngày qua":
                    sql.append(" AND sr.report_time >= NOW() - INTERVAL '7 days'");
                    break;
                case "30 ngày qua":
                    sql.append(" AND sr.report_time >= NOW() - INTERVAL '30 days'");
                    break;
                case "Tháng này":
                    sql.append(" AND EXTRACT(MONTH FROM sr.report_time) = EXTRACT(MONTH FROM NOW()) " +
                            "AND EXTRACT(YEAR FROM sr.report_time) = EXTRACT(YEAR FROM NOW())");
                    break;
            }
        }

        // Lọc theo trạng thái
        if (status != null && !"Tất cả trạng thái".equals(status)) {
            String statusValue;
            switch (status) {
                case "Chờ xử lý":
                    statusValue = "pending";
                    break;
                case "Đã xử lý":
                    statusValue = "resolved";
                    break;
                case "Từ chối":
                    statusValue = "rejected";
                    break;
                default:
                    statusValue = status.toLowerCase();
            }
            sql.append(" AND sr.status = ?");
            params.add(statusValue);
        }

        // Sắp xếp
        if (sortOption != null) {
            switch (sortOption) {
                case "Sắp xếp theo thời gian (Mới nhất)":
                    sql.append(" ORDER BY sr.report_time DESC");
                    break;
                case "Sắp xếp theo thời gian (Cũ nhất)":
                    sql.append(" ORDER BY sr.report_time ASC");
                    break;
                case "Sắp xếp theo người bị báo cáo (A-Z)":
                    sql.append(" ORDER BY u2.full_name ASC");
                    break;
                case "Sắp xếp theo người bị báo cáo (Z-A)":
                    sql.append(" ORDER BY u2.full_name DESC");
                    break;
                case "Sắp xếp theo người báo cáo (A-Z)":
                    sql.append(" ORDER BY u1.full_name ASC");
                    break;
                case "Sắp xếp theo người báo cáo (Z-A)":
                    sql.append(" ORDER BY u1.full_name DESC");
                    break;
                default:
                    sql.append(" ORDER BY sr.report_time DESC");
            }
        } else {
            sql.append(" ORDER BY sr.report_time DESC");
        }
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractSpamReportFromResultSet(rs));
                }
            }
        }
        return reports;
    }

    /**
     * Helper method: Extract SpamReport from ResultSet
     */
    private SpamReport extractSpamReportFromResultSet(ResultSet rs) throws SQLException {
        SpamReport report = new SpamReport();
        report.setId(rs.getInt("report_id"));
        report.setReporterId(rs.getInt("reporter_id"));
        report.setReporterName(rs.getString("reporter_name"));
        report.setReportedUserId(rs.getInt("reported_user_id"));
        report.setReportedUserName(rs.getString("reported_user_name"));
        report.setReason(rs.getString("reason"));
        report.setStatus(rs.getString("status"));

        Timestamp reportTime = rs.getTimestamp("report_time");
        if (reportTime != null) {
            report.setCreatedAt(reportTime.toLocalDateTime());
        }

        return report;
    }
}
