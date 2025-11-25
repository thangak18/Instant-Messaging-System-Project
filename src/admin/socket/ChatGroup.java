package admin.socket;

import java.time.LocalDateTime;

/**
 * Model đại diện cho nhóm chat
 */
public class ChatGroup {
    private int id;
    private String groupName;
    private String description;
    private int createdBy;
    private String creatorName;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public ChatGroup() {}
    
    public ChatGroup(int id, String groupName, String description, 
                    int createdBy, String creatorName, int memberCount,
                    LocalDateTime createdAt) {
        this.id = id;
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.creatorName = creatorName;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "ChatGroup{" +
                "id=" + id +
                ", groupName='" + groupName + '\'' +
                ", memberCount=" + memberCount +
                ", creatorName='" + creatorName + '\'' +
                '}';
    }
}
