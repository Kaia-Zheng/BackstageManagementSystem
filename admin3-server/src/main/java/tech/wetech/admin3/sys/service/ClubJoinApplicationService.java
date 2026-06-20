package tech.wetech.admin3.sys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.common.authz.PermissionHelper;
import tech.wetech.admin3.sys.model.*;
import tech.wetech.admin3.sys.repository.ClubJoinApplicationRepository;
import tech.wetech.admin3.sys.repository.ClubMemberRepository;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

import java.util.List;
import java.util.Set;

@Service
public class ClubJoinApplicationService {

  private final ClubJoinApplicationRepository applicationRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;
  private final ClubMemberRepository clubMemberRepository;
  private final ClubMemberService clubMemberService;
  private final NotificationService notificationService;

  public ClubJoinApplicationService(ClubJoinApplicationRepository applicationRepository,
                                     ClubRepository clubRepository,
                                     UserRepository userRepository,
                                     ClubMemberRepository clubMemberRepository,
                                     ClubMemberService clubMemberService,
                                     NotificationService notificationService) {
    this.applicationRepository = applicationRepository;
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
    this.clubMemberRepository = clubMemberRepository;
    this.clubMemberService = clubMemberService;
    this.notificationService = notificationService;
  }

  private Long getCurrentUserId() {
    UserinfoDTO userInfo = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    return userInfo.userId();
  }

  private Set<String> getCurrentPermissions() {
    UserinfoDTO userInfo = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    return userInfo.permissions();
  }

  /**
   * 申请加入社团
   */
  @Transactional
  public ClubJoinApplication applyToJoin(Long clubId) {
    Long userId = getCurrentUserId();
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "社团不存在"));

    if (club.getState() == Club.State.DISBANDED) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该社团已解散，无法申请加入");
    }

    // 检查是否已是成员
    if (clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "您已是该社团成员");
    }

    // 检查是否有待审核的申请
    if (applicationRepository.existsByClubIdAndUserIdAndStatus(clubId, userId, ClubJoinApplication.ApplicationStatus.PENDING)) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "您已提交过加入申请，请等待审核");
    }

    // 检查是否已被拒绝（允许重新申请，但先删除旧记录）
    applicationRepository.findByClubIdAndUserId(clubId, userId).ifPresent(existing -> {
      if (existing.getStatus() == ClubJoinApplication.ApplicationStatus.REJECTED) {
        applicationRepository.delete(existing);
      }
    });

    User user = userRepository.findById(userId).orElseThrow();
    ClubJoinApplication application = new ClubJoinApplication();
    application.setClub(club);
    application.setUser(user);
    application.setStatus(ClubJoinApplication.ApplicationStatus.PENDING);
    return applicationRepository.save(application);
  }

  /**
   * 查询当前用户对某社团的加入状态
   */
  public String getJoinStatus(Long clubId) {
    Long userId = getCurrentUserId();
    // 已是成员
    if (clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
      return "JOINED";
    }
    // 查申请记录
    var application = applicationRepository.findByClubIdAndUserId(clubId, userId);
    if (application.isPresent()) {
      return application.get().getStatus().name(); // PENDING / APPROVED / REJECTED
    }
    return "NONE";
  }

  /**
   * 获取当前用户的所有加入申请
   */
  public List<ClubJoinApplication> getMyApplications() {
    Long userId = getCurrentUserId();
    return applicationRepository.findByUserId(userId);
  }

  /**
   * 获取某社团的待审核申请列表
   */
  public List<ClubJoinApplication> getClubApplications(Long clubId) {
    checkManagePermission(clubId);
    return applicationRepository.findByClubIdAndStatus(clubId, ClubJoinApplication.ApplicationStatus.PENDING);
  }

  /**
   * 获取某社团的所有申请列表
   */
  public List<ClubJoinApplication> getAllClubApplications(Long clubId) {
    checkManagePermission(clubId);
    return applicationRepository.findByClubId(clubId);
  }

  /**
   * 通过申请
   */
  @Transactional
  public ClubJoinApplication approve(Long applicationId) {
    ClubJoinApplication application = applicationRepository.findById(applicationId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "申请不存在"));

    checkManagePermission(application.getClub().getId());

    if (application.getStatus() != ClubJoinApplication.ApplicationStatus.PENDING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该申请已处理");
    }

    application.setStatus(ClubJoinApplication.ApplicationStatus.APPROVED);

    // 将用户加入社团
    clubMemberService.addMember(application.getClub().getId(), application.getUser().getId(), ClubMember.MemberRole.MEMBER);

    // 发送通知
    String clubName = application.getClub().getName();
    notificationService.sendNotification(
      application.getUser().getId(),
      "入社申请已通过",
      "您申请加入'" + clubName + "'的请求已通过审核，欢迎加入！"
    );

    return applicationRepository.save(application);
  }

  /**
   * 拒绝申请
   */
  @Transactional
  public ClubJoinApplication reject(Long applicationId, String reason) {
    ClubJoinApplication application = applicationRepository.findById(applicationId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "申请不存在"));

    checkManagePermission(application.getClub().getId());

    if (application.getStatus() != ClubJoinApplication.ApplicationStatus.PENDING) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该申请已处理");
    }

    application.setStatus(ClubJoinApplication.ApplicationStatus.REJECTED);
    application.setReason(reason);

    // 发送通知
    String clubName = application.getClub().getName();
    String content = "很遗憾，您申请加入'" + clubName + "'的请求被拒绝。";
    if (reason != null && !reason.isBlank()) {
      content += "原因：" + reason;
    }
    notificationService.sendNotification(
      application.getUser().getId(),
      "入社申请被拒绝",
      content
    );

    return applicationRepository.save(application);
  }

  private void checkManagePermission(Long clubId) {
    UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    if (currentUser == null) {
      throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
    }
    boolean isAdmin = PermissionHelper.hasPermission(currentUser.permissions(), "club:manage");
    boolean isOwner = false;
    Club club = clubRepository.findById(clubId).orElse(null);
    if (club != null && club.getOwner() != null) {
      isOwner = club.getOwner().getId().equals(currentUser.userId());
    }
    // Also check if user is VICE of the club
    boolean isVice = false;
    if (club != null) {
      var memberOpt = clubMemberRepository.findByClubIdAndUserId(clubId, currentUser.userId());
      if (memberOpt.isPresent() && memberOpt.get().getRole() == ClubMember.MemberRole.VICE) {
        isVice = true;
      }
    }
    if (!isAdmin && !isOwner && !isVice) {
      throw new BusinessException(CommonResultStatus.FORBIDDEN, "没有权限管理社团申请");
    }
  }
}
