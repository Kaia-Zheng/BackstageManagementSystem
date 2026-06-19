package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 活动报名（用户 -> 活动 的多对多关系表）
 *
 * @author admin3
 */
@Entity
@Table(name = "activity_registration", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"activity_id", "user_id"})
})
public class ActivityRegistration extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private RegisterStatus status = RegisterStatus.REGISTERED;

  private LocalDateTime registerTime;

  @PrePersist
  protected void onCreate() {
    if (this.registerTime == null) {
      this.registerTime = LocalDateTime.now();
    }
  }

  public enum RegisterStatus {
    REGISTERED,  // 已报名
    CANCELLED     // 已取消
  }

  public Activity getActivity() {
    return activity;
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public RegisterStatus getStatus() {
    return status;
  }

  public void setStatus(RegisterStatus status) {
    this.status = status;
  }

  public LocalDateTime getRegisterTime() {
    return registerTime;
  }

  public void setRegisterTime(LocalDateTime registerTime) {
    this.registerTime = registerTime;
  }
}
