package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 社团（高校社团管理）
 *
 * @author admin3
 */
@Entity
public class Club extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String name;  // 社团名称

  @Column(length = 1000)
  private String description;  // 社团简介

  @Column(nullable = false)
  private Category category;  // 社团类别

  private LocalDate foundedDate;  // 成立日期

  @Column(nullable = false)
  private State state = State.ACTIVE;  // 状态

  private String avatar;  // 社团头像

  @Column(nullable = false)
  private Integer memberCount = 0;  // 当前成员人数

  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;  // 社团负责人（关联用户）

  private LocalDateTime createdTime;

  @PrePersist
  protected void onCreate() {
    if (this.createdTime == null) {
      this.createdTime = LocalDateTime.now();
    }
  }

  public enum Category {
    ACADEMIC,   // 学术科技
    CULTURAL,   // 文化体育
    SPORTS,     // 运动竞技
    VOLUNTEER,  // 公益志愿
    OTHER       // 其他
  }

  public enum State {
    ACTIVE,   // 正常
    DISBANDED // 已解散
  }

  // ====== Getters / Setters ======

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public LocalDate getFoundedDate() {
    return foundedDate;
  }

  public void setFoundedDate(LocalDate foundedDate) {
    this.foundedDate = foundedDate;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public Integer getMemberCount() {
    return memberCount;
  }

  public void setMemberCount(Integer memberCount) {
    this.memberCount = memberCount;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(LocalDateTime createdTime) {
    this.createdTime = createdTime;
  }
}
