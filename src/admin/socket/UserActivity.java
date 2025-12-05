package admin.socket;

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
    private LocalDateTime createdAt; // Ngày tạo tài khoản

    // New fields for comprehensive activity tracking
    private int loginCount;
    private int privateChatCount;
    private int groupChatCount;

    // Constructors
    public UserActivity() {
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getPrivateChatCount() {
        return privateChatCount;
    }

    public void setPrivateChatCount(int privateChatCount) {
        this.privateChatCount = privateChatCount;
    }

    public int getGroupChatCount() {
        return groupChatCount;
    }

    public void setGroupChatCount(int groupChatCount) {
        this.groupChatCount = groupChatCount;
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
