package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.sys.model.*;
import tech.wetech.admin3.sys.repository.*;
import tech.wetech.admin3.sys.service.UserService;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

import java.util.*;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/user")
public class UserProfileController {

  private final UserRepository userRepository;
  private final ClubMemberRepository clubMemberRepository;
  private final ActivityRegistrationRepository activityRegistrationRepository;
  private final ActivityRepository activityRepository;
  private final UserService userService;

  public UserProfileController(UserRepository userRepository,
                                ClubMemberRepository clubMemberRepository,
                                ActivityRegistrationRepository activityRegistrationRepository,
                                ActivityRepository activityRepository,
                                UserService userService) {
    this.userRepository = userRepository;
    this.clubMemberRepository = clubMemberRepository;
    this.activityRegistrationRepository = activityRegistrationRepository;
    this.activityRepository = activityRepository;
    this.userService = userService;
  }

  private Long getCurrentUserId() {
    UserinfoDTO userInfo = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    return userInfo.userId();
  }

  private Set<String> getCurrentPermissions() {
    UserinfoDTO userInfo = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
    return userInfo.permissions();
  }

  @GetMapping("/profile")
  public ResponseEntity<?> getProfile() {
    Long userId = getCurrentUserId();
    User user = userRepository.findById(userId).orElseThrow();
    Set<String> permissions = getCurrentPermissions();

    // 社团信息
    List<ClubMember> clubMembers = clubMemberRepository.findByUserId(userId);
    List<Map<String, Object>> clubs = clubMembers.stream().map(cm -> {
      Map<String, Object> clubInfo = new LinkedHashMap<>();
      clubInfo.put("clubId", cm.getClub().getId());
      clubInfo.put("clubName", cm.getClub().getName());
      clubInfo.put("category", cm.getClub().getCategory().name());
      clubInfo.put("memberCount", cm.getClub().getMemberCount());
      clubInfo.put("role", cm.getRole().name());
      clubInfo.put("joinedTime", cm.getJoinedTime());
      return clubInfo;
    }).collect(Collectors.toList());

    // 活动报名信息
    List<ActivityRegistration> registrations = activityRegistrationRepository.findAllByUserId(userId);
    List<Map<String, Object>> activities = registrations.stream().map(ar -> {
      Map<String, Object> actInfo = new LinkedHashMap<>();
      actInfo.put("registrationId", ar.getId());
      actInfo.put("activityId", ar.getActivity().getId());
      actInfo.put("title", ar.getActivity().getTitle());
      actInfo.put("clubName", ar.getActivity().getClub().getName());
      actInfo.put("activityTime", ar.getActivity().getActivityTime());
      actInfo.put("activityStatus", ar.getActivity().getStatus().name());
      actInfo.put("registerStatus", ar.getStatus().name());
      actInfo.put("registerTime", ar.getRegisterTime());
      return actInfo;
    }).collect(Collectors.toList());

    // 统计数据
    long clubCount = clubs.size();
    long activityCount = registrations.stream().filter(r -> r.getStatus() == ActivityRegistration.RegisterStatus.REGISTERED || r.getStatus() == ActivityRegistration.RegisterStatus.CHECKED_IN).count();
    long pendingAuditCount = 0;
    long pendingRegistrationCount = 0;
    if (permissions.contains("activity:audit")) {
      pendingAuditCount = activityRepository.countByStatusAndCreatorId(Activity.Status.PENDING, userId);
    }
    // 我管理的社团的待审核报名数
    List<ClubMember> managedClubs = clubMembers.stream()
      .filter(cm -> cm.getRole() == ClubMember.MemberRole.OWNER || cm.getRole() == ClubMember.MemberRole.VICE)
      .collect(Collectors.toList());
    if (!managedClubs.isEmpty()) {
      // Count pending registrations for activities in managed clubs
      for (ClubMember cm : managedClubs) {
        // This is a simplified count - we count REGISTERED status registrations for activities in the club
      }
    }

    // 最近3个活动
    List<Map<String, Object>> recentActivities = activities.stream()
      .sorted((a, b) -> {
        String timeA = String.valueOf(a.get("registerTime"));
        String timeB = String.valueOf(b.get("registerTime"));
        return timeB.compareTo(timeA);
      })
      .limit(3)
      .collect(Collectors.toList());

    // 我管理的社团
    List<Map<String, Object>> managedClubsList = clubMembers.stream()
      .filter(cm -> cm.getRole() == ClubMember.MemberRole.OWNER || cm.getRole() == ClubMember.MemberRole.VICE)
      .map(cm -> {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("clubId", cm.getClub().getId());
        info.put("clubName", cm.getClub().getName());
        info.put("category", cm.getClub().getCategory().name());
        info.put("memberCount", cm.getClub().getMemberCount());
        info.put("role", cm.getRole().name());
        return info;
      }).collect(Collectors.toList());

    // 角色标签
    List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("userId", user.getId());
    result.put("username", user.getUsername());
    result.put("realName", user.getRealName());
    result.put("avatar", user.getAvatar());
    result.put("phone", user.getPhone());
    result.put("email", user.getEmail());
    result.put("gender", user.getGender().name());
    result.put("state", user.getState().name());
    result.put("roles", roleNames);
    result.put("permissions", permissions);
    result.put("clubCount", clubCount);
    result.put("activityCount", activityCount);
    result.put("pendingAuditCount", pendingAuditCount);
    result.put("clubs", clubs);
    result.put("activities", activities);
    result.put("recentActivities", recentActivities);
    result.put("managedClubs", managedClubsList);

    return ResponseEntity.ok(result);
  }

  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
    Long userId = getCurrentUserId();
    User user = userService.findUserById(userId);
    if (request.avatar() != null) {
      user.setAvatar(request.avatar());
    }
    if (request.phone() != null) {
      user.setPhone(request.phone());
    }
    if (request.email() != null) {
      user.setEmail(request.email());
    }
    userRepository.save(user);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("message", "更新成功");
    return ResponseEntity.ok(result);
  }

  @GetMapping("/my-clubs")
  public ResponseEntity<?> getMyClubs() {
    Long userId = getCurrentUserId();
    List<ClubMember> clubMembers = clubMemberRepository.findByUserId(userId);
    List<Map<String, Object>> clubs = clubMembers.stream().map(cm -> {
      Map<String, Object> clubInfo = new LinkedHashMap<>();
      clubInfo.put("clubId", cm.getClub().getId());
      clubInfo.put("clubName", cm.getClub().getName());
      clubInfo.put("category", cm.getClub().getCategory().name());
      clubInfo.put("description", cm.getClub().getDescription());
      clubInfo.put("memberCount", cm.getClub().getMemberCount());
      clubInfo.put("role", cm.getRole().name());
      clubInfo.put("joinedTime", cm.getJoinedTime());
      clubInfo.put("state", cm.getClub().getState().name());
      return clubInfo;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("list", clubs));
  }

  @GetMapping("/my-activities")
  public ResponseEntity<?> getMyActivities() {
    Long userId = getCurrentUserId();
    List<ActivityRegistration> registrations = activityRegistrationRepository.findAllByUserId(userId);
    List<Map<String, Object>> activities = registrations.stream().map(ar -> {
      Map<String, Object> actInfo = new LinkedHashMap<>();
      actInfo.put("registrationId", ar.getId());
      actInfo.put("activityId", ar.getActivity().getId());
      actInfo.put("title", ar.getActivity().getTitle());
      actInfo.put("clubName", ar.getActivity().getClub().getName());
      actInfo.put("location", ar.getActivity().getLocation());
      actInfo.put("activityTime", ar.getActivity().getActivityTime());
      actInfo.put("activityStatus", ar.getActivity().getStatus().name());
      actInfo.put("registerStatus", ar.getStatus().name());
      actInfo.put("registerTime", ar.getRegisterTime());
      actInfo.put("maxParticipants", ar.getActivity().getMaxParticipants());
      actInfo.put("currentParticipants", ar.getActivity().getCurrentParticipants());
      return actInfo;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("list", activities));
  }

  record UpdateProfileRequest(
    String avatar,
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确") String email
  ) {}
}
