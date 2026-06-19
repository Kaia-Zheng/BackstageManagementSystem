package tech.wetech.admin3.sys.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.common.authz.PermissionHelper;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.model.ActivityRegistration;
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.ActivityRegistrationRepository;
import tech.wetech.admin3.sys.repository.ActivityRepository;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.PageDTO;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

/**
 * 活动报名 Service
 *
 * @author admin3
 */
@Service
public class ActivityRegistrationService {

  private final ActivityRegistrationRepository registrationRepository;
  private final ActivityRepository activityRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  public ActivityRegistrationService(ActivityRegistrationRepository registrationRepository,
                                     ActivityRepository activityRepository,
                                     ClubRepository clubRepository,
                                     UserRepository userRepository) {
    this.registrationRepository = registrationRepository;
    this.activityRepository = activityRepository;
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
  }

  public PageDTO<ActivityRegistration> findByActivity(Long activityId, Pageable pageable) {
    Page<ActivityRegistration> page = registrationRepository.findByActivityId(activityId, pageable);
    return new PageDTO<>(page.getContent(), page.getTotalElements());
  }

  public PageDTO<ActivityRegistration> findByUser(Pageable pageable) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    Page<ActivityRegistration> page = registrationRepository.findByUserId(currentUser.userId(), pageable);
    return new PageDTO<>(page.getContent(), page.getTotalElements());
  }

  /**
   * 报名活动
   * 所有登录用户都可以报名已发布（PUBLISHED）状态的活动
   */
  @Transactional
  public ActivityRegistration register(Long activityId) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    Activity activity = activityRepository.findById(activityId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "活动不存在"));
    if (activity.getStatus() != Activity.Status.PUBLISHED) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "只能报名已发布的活动");
    }
    // 检查是否已报名
    if (registrationRepository.findByActivityIdAndUserId(activityId, currentUser.userId()).isPresent()) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "您已报名此活动");
    }
    // 检查活动人数
    long registered = registrationRepository.countByActivityId(activityId);
    if (registered >= activity.getMaxParticipants()) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "活动报名人数已满");
    }
    User user = userRepository.findById(currentUser.userId())
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "用户不存在"));
    ActivityRegistration registration = new ActivityRegistration();
    registration.setActivity(activity);
    registration.setUser(user);
    registration.setStatus(ActivityRegistration.RegisterStatus.REGISTERED);
    registration = registrationRepository.save(registration);

    // 更新活动当前报名人数
    activity.setCurrentParticipants((int) registrationRepository.countByActivityId(activityId));
    activityRepository.save(activity);
    return registration;
  }

  /**
   * 取消报名
   */
  @Transactional
  public void cancelRegistration(Long activityId) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    ActivityRegistration registration = registrationRepository.findByActivityIdAndUserId(activityId, currentUser.userId())
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "您还未报名此活动"));
    registrationRepository.delete(registration);
    // 更新活动当前报名人数
    Activity activity = activityRepository.findById(activityId).orElseThrow();
    activity.setCurrentParticipants((int) registrationRepository.countByActivityId(activityId));
    activityRepository.save(activity);
  }

  /**
   * 签到报名者
   * 仅活动所属社团负责人和管理员可操作
   */
  @Transactional
  public ActivityRegistration checkIn(Long activityId, Long userId) {
    checkManagePermission(activityId);
    ActivityRegistration registration = registrationRepository.findByActivityIdAndUserId(activityId, userId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "该用户未报名此活动"));
    if (registration.getStatus() == ActivityRegistration.RegisterStatus.CHECKED_IN) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该用户已签到");
    }
    if (registration.getStatus() == ActivityRegistration.RegisterStatus.CANCELLED) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该报名已取消，无法签到");
    }
    registration.setStatus(ActivityRegistration.RegisterStatus.CHECKED_IN);
    return registrationRepository.save(registration);
  }

  /**
   * 获取当前用户对指定活动的报名状态
   */
  public ActivityRegistration getMyRegistration(Long activityId) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      return null;
    }
    return registrationRepository.findByActivityIdAndUserId(activityId, currentUser.userId()).orElse(null);
  }

  private void checkManagePermission(Long activityId) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    // 检查是否是管理员
    if (PermissionHelper.hasPermission(currentUser.permissions(), "activity:update")) {
      return;
    }
    // 检查是否是社团负责人
    Activity activity = activityRepository.findById(activityId).orElse(null);
    if (activity != null && activity.getClub() != null) {
      Club club = activity.getClub();
      if (club.getOwner() != null && club.getOwner().getId().equals(currentUser.userId())) {
        return;
      }
    }
    throw new BusinessException(CommonResultStatus.FORBIDDEN, "没有权限管理此活动的报名");
  }
}
