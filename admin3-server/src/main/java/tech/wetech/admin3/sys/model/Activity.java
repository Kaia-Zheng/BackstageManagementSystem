package tech.wetech.admin3.sys.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 活动（社团发布的活动）
 *
 * @author admin3
 */
@Entity
public class Activity extends BaseEntity {

  @Column(nullable = false)
  private String title;  // 活动标题

  @Column(length = 2000)
  private String description;  // 活动简介

  private String location;  // 活动地点

  private LocalDateTime activityTime;  // 活动时间

  @Column(nullable = false)
  private Status status = Status.PENDING;  // 状态

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;  // 所属社团

  private String coverImage;  // 活动封面图

  @Column(nullable = false)
  private Integer maxParticipants = 50;  // 最大报名人数

  @Column(nullable = false)
  private Integer currentParticipants = 0;  // 当前报名人数

  @Column(length = 500)
  private String rejectReason;  // 审核不通过原因

  @ManyToOne(fetch = FetchType.LAZY)
  private User creator;  // 创建人（社团负责人）

  private LocalDateTime createdTime;

  @PrePersist
  protected void onCreate() {
    if (this.createdTime == null) {
      this.createdTime = LocalDateTime.now();
    }
  }

  public enum Status {
    PENDING,   // 待审核
    PUBLISHED, // 已发布（报名中）
    ONGOING,   // 进行中
    ENDED,     // 已结束
    CANCELLED  // 已取消/审核不通过
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public LocalDateTime getActivityTime() {
    return activityTime;
  }

  public void setActivityTime(LocalDateTime activityTime) {
    this.activityTime = activityTime;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Club getClub() {
    return club;
  }

  public void setClub(Club club) {
    this.club = club;
  }

  public String getCoverImage() {
    return coverImage;
  }

  public void setCoverImage(String coverImage) {
    this.coverImage = coverImage;
  }

  public Integer getMaxParticipants() {
    return maxParticipants;
  }

  public void setMaxParticipants(Integer maxParticipants) {
    this.maxParticipants = maxParticipants;
  }

  public Integer getCurrentParticipants() {
    return currentParticipants;
  }

  public void setCurrentParticipants(Integer currentParticipants) {
    this.currentParticipants = currentParticipants;
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(LocalDateTime createdTime) {
    this.createdTime = createdTime;
  }
}
