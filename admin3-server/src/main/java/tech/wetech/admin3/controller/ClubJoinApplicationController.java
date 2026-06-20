package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.sys.model.ClubJoinApplication;
import tech.wetech.admin3.sys.service.ClubJoinApplicationService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/clubs")
public class ClubJoinApplicationController {

  private final ClubJoinApplicationService applicationService;

  public ClubJoinApplicationController(ClubJoinApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @PostMapping("/{clubId}/join")
  public ResponseEntity<?> applyToJoin(@PathVariable Long clubId) {
    ClubJoinApplication app = applicationService.applyToJoin(clubId);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", app.getId());
    result.put("clubId", app.getClub().getId());
    result.put("status", app.getStatus().name());
    result.put("message", "申请已提交，等待社团负责人审核");
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{clubId}/join-status")
  public ResponseEntity<?> getJoinStatus(@PathVariable Long clubId) {
    String status = applicationService.getJoinStatus(clubId);
    return ResponseEntity.ok(Map.of("status", status));
  }

  @GetMapping("/join-applications/my")
  public ResponseEntity<?> getMyApplications() {
    List<ClubJoinApplication> apps = applicationService.getMyApplications();
    List<Map<String, Object>> list = apps.stream().map(app -> {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", app.getId());
      item.put("clubId", app.getClub().getId());
      item.put("clubName", app.getClub().getName());
      item.put("status", app.getStatus().name());
      item.put("reason", app.getReason());
      item.put("createTime", app.getCreateTime());
      item.put("updateTime", app.getUpdateTime());
      return item;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("list", list));
  }

  @GetMapping("/{clubId}/applications")
  public ResponseEntity<?> getClubApplications(@PathVariable Long clubId) {
    List<ClubJoinApplication> apps = applicationService.getClubApplications(clubId);
    List<Map<String, Object>> list = apps.stream().map(app -> {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", app.getId());
      item.put("clubId", app.getClub().getId());
      item.put("clubName", app.getClub().getName());
      item.put("userId", app.getUser().getId());
      item.put("username", app.getUser().getUsername());
      item.put("realName", app.getUser().getRealName());
      item.put("status", app.getStatus().name());
      item.put("reason", app.getReason());
      item.put("createTime", app.getCreateTime());
      item.put("updateTime", app.getUpdateTime());
      return item;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("list", list));
  }

  @PostMapping("/applications/{applicationId}/approve")
  public ResponseEntity<?> approve(@PathVariable Long applicationId) {
    ClubJoinApplication app = applicationService.approve(applicationId);
    return ResponseEntity.ok(Map.of("message", "已通过申请", "status", app.getStatus().name()));
  }

  @PostMapping("/applications/{applicationId}/reject")
  public ResponseEntity<?> reject(@PathVariable Long applicationId, @RequestBody(required = false) RejectRequest request) {
    String reason = request != null ? request.reason() : null;
    ClubJoinApplication app = applicationService.reject(applicationId, reason);
    return ResponseEntity.ok(Map.of("message", "已拒绝申请", "status", app.getStatus().name()));
  }

  record RejectRequest(String reason) {}
}
