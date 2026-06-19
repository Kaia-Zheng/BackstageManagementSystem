package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.common.authz.RequiresPermissions;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.service.ActivityService;
import tech.wetech.admin3.sys.service.dto.PageDTO;

import java.time.LocalDateTime;

/**
 * 活动管理 Controller
 *
 * @author admin3
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/activities")
public class ActivityController {

  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  /**
   * 查询活动列表（根据用户权限过滤可见活动）
   */
  @RequiresPermissions("activity:view")
  @GetMapping
  public ResponseEntity<PageDTO<Activity>> findActivities(Pageable pageable,
                                                             @RequestParam(required = false) String title,
                                                             @RequestParam(required = false) Long clubId,
                                                             @RequestParam(required = false) Activity.Status status) {
    return ResponseEntity.ok(activityService.findActivities(pageable, title, clubId, status));
  }

  /**
   * 查询单个活动
   */
  @RequiresPermissions("activity:view")
  @GetMapping("/{activityId}")
  public ResponseEntity<Activity> findActivity(@PathVariable Long activityId) {
    return ResponseEntity.ok(activityService.findActivity(activityId));
  }

  /**
   * 创建活动（社团负责人） - 状态为待审核
   */
  @RequiresPermissions("activity:create")
  @PostMapping
  public ResponseEntity<Activity> createActivity(@RequestBody @Valid ActivityRequest request) {
    Activity activity = activityService.createActivity(
      request.clubId(),
      request.title(),
      request.description(),
      request.location(),
      request.activityTime(),
      request.coverImage(),
      request.maxParticipants()
    );
    return new ResponseEntity<>(activity, HttpStatus.CREATED);
  }

  /**
   * 更新活动基本信息
   */
  @RequiresPermissions("activity:update")
  @PutMapping("/{activityId}")
  public ResponseEntity<Activity> updateActivity(@PathVariable Long activityId, @RequestBody ActivityRequest request) {
    return ResponseEntity.ok(activityService.updateActivity(
      activityId,
      request.title(),
      request.description(),
      request.location(),
      request.activityTime(),
      request.coverImage(),
      request.maxParticipants()
    ));
  }

  /**
   * 审核通过（管理员）
   */
  @RequiresPermissions("activity:audit")
  @PostMapping("/{activityId}/approve")
  public ResponseEntity<Activity> approveActivity(@PathVariable Long activityId) {
    return ResponseEntity.ok(activityService.approveActivity(activityId));
  }

  /**
   * 审核不通过（管理员）
   */
  @RequiresPermissions("activity:audit")
  @PostMapping("/{activityId}/reject")
  public ResponseEntity<Activity> rejectActivity(@PathVariable Long activityId,
                                                  @RequestBody(required = false) RejectRequest request) {
    String reason = request != null ? request.reason() : "不符合要求";
    return ResponseEntity.ok(activityService.rejectActivity(activityId, reason));
  }

  /**
   * 开始活动
   */
  @RequiresPermissions("activity:update")
  @PostMapping("/{activityId}/start")
  public ResponseEntity<Activity> startActivity(@PathVariable Long activityId) {
    return ResponseEntity.ok(activityService.startActivity(activityId));
  }

  /**
   * 结束活动
   */
  @RequiresPermissions("activity:update")
  @PostMapping("/{activityId}/end")
  public ResponseEntity<Activity> endActivity(@PathVariable Long activityId) {
    return ResponseEntity.ok(activityService.endActivity(activityId));
  }

  /**
   * 取消活动
   */
  @RequiresPermissions("activity:update")
  @PostMapping("/{activityId}/cancel")
  public ResponseEntity<Activity> cancelActivity(@PathVariable Long activityId,
                                                   @RequestBody(required = false) RejectRequest request) {
    String reason = request != null ? request.reason() : "活动取消";
    return ResponseEntity.ok(activityService.cancelActivity(activityId, reason));
  }

  /**
   * 删除活动
   */
  @RequiresPermissions("activity:delete")
  @DeleteMapping("/{activityId}")
  public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
    activityService.deleteActivity(activityId);
    return ResponseEntity.noContent().build();
  }

  record ActivityRequest(@NotBlank String title,
                          String description,
                          String location,
                          LocalDateTime activityTime,
                          String coverImage,
                          Integer maxParticipants,
                          Long clubId) {
  }

  record RejectRequest(String reason) {
  }
}
