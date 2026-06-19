package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 社团成员（User 与 Club 的多对多中间表，附带成员角色）
 *
 * @author admin3
 */
@Entity
@Table(name = "club_member", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"club_id", "user_id"})
})
public class ClubMember extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private MemberRole role = MemberRole.MEMBER;  // 成员角色

  private LocalDateTime joinedTime;  // 加入时间

  @PrePersist
  protected void onCreate() {
    if (this.joinedTime == null) {
      this.joinedTime = LocalDateTime.now();
    }
  }

  public enum MemberRole {
    OWNER,   // 负责人
    VICE,    // 副社长
    MEMBER   // 普通成员
  }

  public Club getClub() {
    return club;
  }

  public void setClub(Club club) {
    this.club = club;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public MemberRole getRole() {
    return role;
  }

  public void setRole(MemberRole role) {
    this.role = role;
  }

  public LocalDateTime getJoinedTime() {
    return joinedTime;
  }

  public void setJoinedTime(LocalDateTime joinedTime) {
    this.joinedTime = joinedTime;
  }
}
