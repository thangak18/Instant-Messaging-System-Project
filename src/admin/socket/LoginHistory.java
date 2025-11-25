package admin.socket;

import java.time.LocalDateTime;

/**
 * Model đại diện cho lịch sử đăng nhập
 */
public class LoginHistory {
    private int id;
    private int userId;
    private String username;
    private String fullName;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    
    // Constructors
    public LoginHistory() {}
    
    public LoginHistory(int id, int userId, String username, String fullName,
                       LocalDateTime loginTime, String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    @Override
    public String toString() {
        return "LoginHistory{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", loginTime=" + loginTime +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
