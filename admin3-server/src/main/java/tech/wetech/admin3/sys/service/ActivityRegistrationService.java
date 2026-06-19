package tech.wetech.admin3.sys.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.model.ActivityRegistration;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.ActivityRegistrationRepository;
import tech.wetech.admin3.sys.repository.ActivityRepository;
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
  private final UserRepository userRepository;

  public ActivityRegistrationService(ActivityRegistrationRepository registrationRepository,
                                       ActivityRepository activityRepository,
                                       UserRepository userRepository) {
    this.registrationRepository = registrationRepository;
    this.activityRepository = activityRepository;
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
}
