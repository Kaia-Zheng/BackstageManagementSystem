package tech.wetech.admin3.sys.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.common.authz.PermissionHelper;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.ActivityRepository;
import tech.wetech.admin3.sys.repository.ClubMemberRepository;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.PageDTO;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

import java.util.List;

import java.time.LocalDateTime;

/**
 * 活动 Service - 包含核心状态流转逻辑
 *
 * @author admin3
 */
@Service
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;
  private final ClubMemberRepository clubMemberRepository;

  public ActivityService(ActivityRepository activityRepository,
                          ClubRepository clubRepository,
                          UserRepository userRepository,
                          ClubMemberRepository clubMemberRepository) {
    this.activityRepository = activityRepository;
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
    this.clubMemberRepository = clubMemberRepository;
  }

  /**
   * 查询活动列表（根据用户权限过滤）
   * 管理员可见全部；社团负责人可见自己社团的全部（包括待审核/已取消）；
   * 普通用户只可见 PUBLISHED / ONGOING / ENDED
   */
  public PageDTO<Activity> findActivities(Pageable pageable, String title, Long clubId, Activity.Status status) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    boolean isAdmin = currentUser != null && PermissionHelper.hasPermission(currentUser.permissions(), "activity:audit");

    Page<Activity> page;
    if (isAdmin || (status != null && (status == Activity.Status.PENDING || status == Activity.Status.CANCELLED))) {
      // 管理员或显式查询待审核/已取消状态时用全量查询
      page = activityRepository.findByConditions(title, status, clubId, pageable);
    } else {
      // 非管理员查询可见活动（已发布/进行中/已结束）
      // 先查询所有活动，然后在内存中过滤可见状态
      Page<Activity> allPage = activityRepository.findByConditions(title, null, clubId, pageable);
      List<Activity> visible = allPage.getContent().stream()
        .filter(a -> a.getStatus() == Activity.Status.PUBLISHED
                  || a.getStatus() == Activity.Status.ONGOING
                  || a.getStatus() == Activity.Status.ENDED)
        .toList();
      page = new PageImpl<>(visible, pageable, visible.size());
    }
    return new PageDTO<>(page.getContent(), page.getTotalElements());
  }

  public Activity findActivity(Long activityId) {
    return activityRepository.findById(activityId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "活动不存在"));
  }

  /**
   * 创建活动（社团负责人）—— 状态初始为待审核 PENDING
   */
  @Transactional
  public Activity createActivity(Long clubId, String title, String description,
                                  String location, LocalDateTime activityTime,
                                  String coverImage, Integer maxParticipants) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "社团不存在"));

    // 权限：管理员或者社团负责人才能创建活动
    boolean isAdmin = PermissionHelper.hasPermission(currentUser.permissions(), "activity:audit");
    boolean isOwner = club.getOwner() != null && club.getOwner().getId().equals(currentUser.userId());
    if (!isAdmin && !isOwner) {
      throw new BusinessException(CommonResultStatus.FORBIDDEN, "只有管理员或社团负责人能发布活动");
    }

    Activity activity = new Activity();
    activity.setClub(club);
    activity.setTitle(title);
    activity.setDescription(description);
    activity.setLocation(location);
    activity.setActivityTime(activityTime);
    activity.setCoverImage(coverImage);
    activity.setMaxParticipants(maxParticipants != null ? maxParticipants : 50);
    activity.setCurrentParticipants(0);
    activity.setStatus(Activity.Status.PENDING); // 新创建的活动默认为待审核

    User creator = userRepository.findById(currentUser.userId()).orElse(null);
    activity.setCreator(creator);
    return activityRepository.save(activity);
  }

  /**
   * 更新活动基本信息（社团负责人/管理员）
   */
  @Transactional
  public Activity updateActivity(Long activityId, String title, String description,
                                  String location, LocalDateTime activityTime,
                                  String coverImage, Integer maxParticipants) {
    Activity activity = findActivity(activityId);
    checkUpdatePermission(activity);
    if (title != null) activity.setTitle(title);
    if (description != null) activity.setDescription(description);
    if (location != null) activity.setLocation(location);
    if (activityTime != null) activity.setActivityTime(activityTime);
    if (coverImage != null) activity.setCoverImage(coverImage);
    if (maxParticipants != null) activity.setMaxParticipants(maxParticipants);
    return activityRepository.save(activity);
  }

  /**
   * 审核通过（管理员） PENDING -> PUBLISHED
   */
  @Transactional
  public Activity approveActivity(Long activityId) {
    Activity activity = findActivity(activityId);
    if (activity.getStatus() != Activity.Status.PENDING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只有待审核的活动可以审核通过");
    }
    activity.setStatus(Activity.Status.PUBLISHED);
    return activityRepository.save(activity);
  }

  /**
   * 审核不通过（管理员） PENDING -> CANCELLED
   */
  @Transactional
  public Activity rejectActivity(Long activityId, String reason) {
    Activity activity = findActivity(activityId);
    if (activity.getStatus() != Activity.Status.PENDING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只有待审核的活动可以审核不通过");
    }
    activity.setStatus(Activity.Status.CANCELLED);
    activity.setRejectReason(reason);
    return activityRepository.save(activity);
  }

  /**
   * 开始活动（社团负责人/管理员） PUBLISHED -> ONGOING
   */
  @Transactional
  public Activity startActivity(Long activityId) {
    Activity activity = findActivity(activityId);
    checkUpdatePermission(activity);
    if (activity.getStatus() != Activity.Status.PUBLISHED) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只有已发布的活动可以开始");
    }
    activity.setStatus(Activity.Status.ONGOING);
    return activityRepository.save(activity);
  }

  /**
   * 结束活动（社团负责人/管理员） ONGOING -> ENDED
   */
  @Transactional
  public Activity endActivity(Long activityId) {
    Activity activity = findActivity(activityId);
    checkUpdatePermission(activity);
    if (activity.getStatus() != Activity.Status.ONGOING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只有进行中的活动可以结束");
    }
    activity.setStatus(Activity.Status.ENDED);
    return activityRepository.save(activity);
  }

  /**
   * 取消活动（社团负责人/管理员） PUBLISHED -> CANCELLED
   */
  @Transactional
  public Activity cancelActivity(Long activityId, String reason) {
    Activity activity = findActivity(activityId);
    checkUpdatePermission(activity);
    if (activity.getStatus() != Activity.Status.PUBLISHED && activity.getStatus() != Activity.Status.ONGOING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只有已发布或进行中的活动可以取消");
    }
    activity.setStatus(Activity.Status.CANCELLED);
    activity.setRejectReason(reason);
    return activityRepository.save(activity);
  }

  /**
   * 删除活动（社团负责人/管理员）
   */
  @Transactional
  public void deleteActivity(Long activityId) {
    Activity activity = findActivity(activityId);
    checkUpdatePermission(activity);
    activityRepository.delete(activity);
  }

  /**
   * 校验是否有权限修改活动：管理员或所属社团的负责人
   */
  private void checkUpdatePermission(Activity activity) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    boolean isAdmin = PermissionHelper.hasPermission(currentUser.permissions(), "activity:audit");
    boolean isOwner = activity.getClub().getOwner() != null
      && activity.getClub().getOwner().getId().equals(currentUser.userId());
    if (!isAdmin && !isOwner) {
      throw new BusinessException(CommonResultStatus.FORBIDDEN, "没有权限操作此活动");
    }
  }
}
