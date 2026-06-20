package tech.wetech.admin3.sys.service;

import org.springframework.stereotype.Service;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.model.ClubMember;
import tech.wetech.admin3.sys.repository.ActivityRegistrationRepository;
import tech.wetech.admin3.sys.repository.ActivityRepository;
import tech.wetech.admin3.sys.repository.ClubMemberRepository;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.service.dto.DashboardDTO;
import tech.wetech.admin3.sys.service.dto.DashboardDTO.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据看板统计 Service
 */
@Service
public class StatisticsService {

  private final ClubRepository clubRepository;
  private final ActivityRepository activityRepository;
  private final ActivityRegistrationRepository registrationRepository;
  private final ClubMemberRepository clubMemberRepository;
  private final EntityManager entityManager;

  public StatisticsService(ClubRepository clubRepository,
                           ActivityRepository activityRepository,
                           ActivityRegistrationRepository registrationRepository,
                           ClubMemberRepository clubMemberRepository,
                           EntityManager entityManager) {
    this.clubRepository = clubRepository;
    this.activityRepository = activityRepository;
    this.registrationRepository = registrationRepository;
    this.clubMemberRepository = clubMemberRepository;
    this.entityManager = entityManager;
  }

  public DashboardDTO getDashboard() {
    DashboardDTO dto = new DashboardDTO();

    // 1. 顶部统计卡片
    dto.setClubCount(clubRepository.count());
    dto.setPendingActivityCount(activityRepository.count()
      - activityRepository.findByConditions(null, Activity.Status.PUBLISHED, null, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements()
      - activityRepository.findByConditions(null, Activity.Status.ONGOING, null, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements()
      - activityRepository.findByConditions(null, Activity.Status.ENDED, null, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements()
      - activityRepository.findByConditions(null, Activity.Status.CANCELLED, null, org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements());

    // 本月活动数
    LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
    LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
    Query monthQuery = entityManager.createQuery(
      "select count(a) from Activity a where a.createdTime >= :start and a.createdTime < :end");
    monthQuery.setParameter("start", monthStart);
    monthQuery.setParameter("end", monthEnd);
    dto.setActivityCountThisMonth((Long) monthQuery.getSingleResult());

    // 报名总人次
    dto.setRegistrationTotalCount(registrationRepository.count());

    // 2. 近6个月活动发布趋势
    List<String> monthLabels = new ArrayList<>();
    List<Long> monthCounts = new ArrayList<>();
    DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("yyyy-MM");
    for (int i = 5; i >= 0; i--) {
      YearMonth ym = YearMonth.now().minusMonths(i);
      monthLabels.add(ym.format(labelFmt));
      LocalDateTime start = ym.atDay(1).atStartOfDay();
      LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
      Query mQuery = entityManager.createQuery(
        "select count(a) from Activity a where a.createdTime >= :start and a.createdTime < :end");
      mQuery.setParameter("start", start);
      mQuery.setParameter("end", end);
      monthCounts.add((Long) mQuery.getSingleResult());
    }
    dto.setMonthLabels(monthLabels);
    dto.setMonthActivityCounts(monthCounts);

    // 3. 社团类别分布
    List<CategoryItem> categoryItems = new ArrayList<>();
    for (Club.Category cat : Club.Category.values()) {
      long count = clubRepository.findByConditions(null, cat, null,
        org.springframework.data.domain.Pageable.ofSize(1)).getTotalElements();
      String label = getCategoryLabel(cat);
      categoryItems.add(new CategoryItem(label, count));
    }
    dto.setCategoryDistribution(categoryItems);

    // 4. 报名人数最多的前5个活动
    Query rankQuery = entityManager.createQuery(
      "select a.id, a.title, count(ar) from ActivityRegistration ar join ar.activity a " +
        "where ar.status = tech.wetech.admin3.sys.model.ActivityRegistration$RegisterStatus.REGISTERED " +
        "group by a.id, a.title order by count(ar) desc");
    rankQuery.setMaxResults(5);
    @SuppressWarnings("unchecked")
    List<Object[]> rankResults = rankQuery.getResultList();
    List<ActivityRankItem> rankItems = new ArrayList<>();
    for (Object[] row : rankResults) {
      rankItems.add(new ActivityRankItem((Long) row[0], (String) row[1], (Long) row[2]));
    }
    dto.setActivityRank(rankItems);

    // 5. 最新5条报名记录
    Query recentQuery = entityManager.createQuery(
      "select ar.id, u.username, a.title, ar.status, ar.registerTime " +
        "from ActivityRegistration ar join ar.user u join ar.activity a " +
        "order by ar.registerTime desc");
    recentQuery.setMaxResults(5);
    @SuppressWarnings("unchecked")
    List<Object[]> recentResults = recentQuery.getResultList();
    List<RecentRegistrationItem> recentItems = new ArrayList<>();
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (Object[] row : recentResults) {
      RecentRegistrationItem item = new RecentRegistrationItem();
      item.setId((Long) row[0]);
      item.setUsername((String) row[1]);
      item.setActivityTitle((String) row[2]);
      item.setStatus(row[3].toString());
      item.setRegisterTime(row[4] != null ? ((LocalDateTime) row[4]).format(timeFmt) : "");
      recentItems.add(item);
    }
    dto.setRecentRegistrations(recentItems);

    return dto;
  }

  private String getCategoryLabel(Club.Category cat) {
    return switch (cat) {
      case ACADEMIC -> "学术科技";
      case CULTURAL -> "文化体育";
      case SPORTS -> "运动竞技";
      case VOLUNTEER -> "公益志愿";
      case OTHER -> "其他";
    };
  }
}
