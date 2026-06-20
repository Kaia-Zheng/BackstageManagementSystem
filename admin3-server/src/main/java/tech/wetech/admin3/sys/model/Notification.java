package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String content;

    @Column(nullable = false)
    private Boolean read = false;

    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
