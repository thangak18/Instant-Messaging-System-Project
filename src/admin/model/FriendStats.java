package admin.model;

/**
 * Model đại diện cho thống kê bạn bè của người dùng
 */
public class FriendStats {
    private int userId;
    private String username;
    private String fullName;
    private int friendCount;
    private int onlineFriends;
    
    // Constructors
    public FriendStats() {}
    
    public FriendStats(int userId, String username, String fullName, int friendCount) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.friendCount = friendCount;
        this.onlineFriends = 0;
    }
    
    // Getters and Setters
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
    
    public int getFriendCount() {
        return friendCount;
    }
    
    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }
    
    public int getOnlineFriends() {
        return onlineFriends;
    }
    
    public void setOnlineFriends(int onlineFriends) {
        this.onlineFriends = onlineFriends;
    }
    
    @Override
    public String toString() {
        return "FriendStats{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", friendCount=" + friendCount +
                '}';
    }
}
