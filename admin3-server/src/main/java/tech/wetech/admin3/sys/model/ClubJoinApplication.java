package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "club_join_application", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"club_id", "user_id"})
})
public class ClubJoinApplication extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private ApplicationStatus status = ApplicationStatus.PENDING;

  @Column(length = 500)
  private String reason;

  @Column(nullable = false)
  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  public enum ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED
  }

  @PrePersist
  protected void onCreate() {
    if (this.createTime == null) {
      this.createTime = LocalDateTime.now();
    }
  }

  public Club getClub() { return club; }
  public void setClub(Club club) { this.club = club; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public ApplicationStatus getStatus() { return status; }
  public void setStatus(ApplicationStatus status) { this.status = status; this.updateTime = LocalDateTime.now(); }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public LocalDateTime getCreateTime() { return createTime; }
  public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
  public LocalDateTime getUpdateTime() { return updateTime; }
  public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
