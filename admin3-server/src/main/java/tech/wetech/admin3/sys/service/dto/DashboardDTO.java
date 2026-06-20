package tech.wetech.admin3.sys.service.dto;

import java.util.List;
import java.util.Map;

/**
 * 数据看板统计 DTO
 */
public class DashboardDTO {

  // 顶部统计卡片
  private long clubCount;
  private long activityCountThisMonth;
  private long registrationTotalCount;
  private long pendingActivityCount;

  // 近6个月活动发布趋势
  private List<String> monthLabels;
  private List<Long> monthActivityCounts;

  // 社团类别分布
  private List<CategoryItem> categoryDistribution;

  // 报名人数最多的前5个活动
  private List<ActivityRankItem> activityRank;

  // 最新5条报名记录
  private List<RecentRegistrationItem> recentRegistrations;

  // ===== 内部类 =====

  public static class CategoryItem {
    private String name;
    private long count;

    public CategoryItem() {}

    public CategoryItem(String name, long count) {
      this.name = name;
      this.count = count;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
  }

  public static class ActivityRankItem {
    private Long id;
    private String title;
    private long registrationCount;

    public ActivityRankItem() {}

    public ActivityRankItem(Long id, String title, long registrationCount) {
      this.id = id;
      this.title = title;
      this.registrationCount = registrationCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getRegistrationCount() { return registrationCount; }
    public void setRegistrationCount(long registrationCount) { this.registrationCount = registrationCount; }
  }

  public static class RecentRegistrationItem {
    private Long id;
    private String username;
    private String activityTitle;
    private String status;
    private String registerTime;

    public RecentRegistrationItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegisterTime() { return registerTime; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
  }

  // ===== Getters / Setters =====

  public long getClubCount() { return clubCount; }
  public void setClubCount(long clubCount) { this.clubCount = clubCount; }

  public long getActivityCountThisMonth() { return activityCountThisMonth; }
  public void setActivityCountThisMonth(long activityCountThisMonth) { this.activityCountThisMonth = activityCountThisMonth; }

  public long getRegistrationTotalCount() { return registrationTotalCount; }
  public void setRegistrationTotalCount(long registrationTotalCount) { this.registrationTotalCount = registrationTotalCount; }

  public long getPendingActivityCount() { return pendingActivityCount; }
  public void setPendingActivityCount(long pendingActivityCount) { this.pendingActivityCount = pendingActivityCount; }

  public List<String> getMonthLabels() { return monthLabels; }
  public void setMonthLabels(List<String> monthLabels) { this.monthLabels = monthLabels; }

  public List<Long> getMonthActivityCounts() { return monthActivityCounts; }
  public void setMonthActivityCounts(List<Long> monthActivityCounts) { this.monthActivityCounts = monthActivityCounts; }

  public List<CategoryItem> getCategoryDistribution() { return categoryDistribution; }
  public void setCategoryDistribution(List<CategoryItem> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

  public List<ActivityRankItem> getActivityRank() { return activityRank; }
  public void setActivityRank(List<ActivityRankItem> activityRank) { this.activityRank = activityRank; }

  public List<RecentRegistrationItem> getRecentRegistrations() { return recentRegistrations; }
  public void setRecentRegistrations(List<RecentRegistrationItem> recentRegistrations) { this.recentRegistrations = recentRegistrations; }
}
