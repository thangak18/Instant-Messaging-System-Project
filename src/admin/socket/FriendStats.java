package admin.socket;

import java.time.LocalDateTime;

/**
 * Model đại diện cho thống kê bạn bè của người dùng
 */
public class FriendStats {
    private int userId;
    private String username;
    private String fullName;
    private int friendCount;
    private int onlineFriends;
    private int friendsOfFriendsCount; // Số bạn của bạn
    private LocalDateTime createdAt;   // Ngày tạo tài khoản
    
    // Constructors
    public FriendStats() {}
    
    public FriendStats(int userId, String username, String fullName, int friendCount) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.friendCount = friendCount;
        this.onlineFriends = 0;
        this.friendsOfFriendsCount = 0;
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
    
    public int getFriendsOfFriendsCount() {
        return friendsOfFriendsCount;
    }
    
    public void setFriendsOfFriendsCount(int friendsOfFriendsCount) {
        this.friendsOfFriendsCount = friendsOfFriendsCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "FriendStats{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", friendCount=" + friendCount +
                ", friendsOfFriendsCount=" + friendsOfFriendsCount +
                '}';
    }
}
