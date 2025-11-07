package admin.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho báo cáo spam
 */
public class SpamReport {
    private int id;
    private int reporterId;
    private String reporterName;
    private int reportedUserId;
    private String reportedUserName;
    private String reason;
    private String status; // 'pending', 'resolved', 'dismissed'
    private LocalDateTime createdAt;
    
    // Constructors
    public SpamReport() {}
    
    public SpamReport(int id, int reporterId, String reporterName,
                     int reportedUserId, String reportedUserName,
                     String reason, String status, LocalDateTime createdAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getReporterId() {
        return reporterId;
    }
    
    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    
    public int getReportedUserId() {
        return reportedUserId;
    }
    
    public void setReportedUserId(int reportedUserId) {
        this.reportedUserId = reportedUserId;
    }
    
    public String getReportedUserName() {
        return reportedUserName;
    }
    
    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "SpamReport{" +
                "id=" + id +
                ", reporterName='" + reporterName + '\'' +
                ", reportedUserName='" + reportedUserName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
