package admin.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho hoạt động người dùng
 */
public class UserActivity {
    private int id;
    private int userId;
    private String username;
    private String fullName;
    private String activityType; // 'login', 'chat', 'group_chat', 'friend_request'
    private int activityCount;
    private LocalDateTime lastActivity;
    
    // Constructors
    public UserActivity() {}
    
    public UserActivity(int userId, String username, String fullName,
                       String activityType, int activityCount, LocalDateTime lastActivity) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.activityType = activityType;
        this.activityCount = activityCount;
        this.lastActivity = lastActivity;
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
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public int getActivityCount() {
        return activityCount;
    }
    
    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    @Override
    public String toString() {
        return "UserActivity{" +
                "username='" + username + '\'' +
                ", activityType='" + activityType + '\'' +
                ", activityCount=" + activityCount +
                ", lastActivity=" + lastActivity +
                '}';
    }
}
