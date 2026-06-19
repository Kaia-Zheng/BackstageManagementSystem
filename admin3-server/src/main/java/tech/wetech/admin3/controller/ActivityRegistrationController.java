package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.common.authz.RequiresPermissions;
import tech.wetech.admin3.sys.model.ActivityRegistration;
import tech.wetech.admin3.sys.service.ActivityRegistrationService;
import tech.wetech.admin3.sys.service.dto.PageDTO;

/**
 * 活动报名 Controller
 *
 * @author admin3
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/activities/{activityId}/registrations")
public class ActivityRegistrationController {

  private final ActivityRegistrationService registrationService;

  public ActivityRegistrationController(ActivityRegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  /**
   * 查询活动的报名列表（活动管理员/社团负责人可见）
   */
  @RequiresPermissions("activity:view")
  @GetMapping
  public ResponseEntity<PageDTO<ActivityRegistration>> findByActivity(@PathVariable Long activityId,
                                                                        Pageable pageable) {
    return ResponseEntity.ok(registrationService.findByActivity(activityId, pageable));
  }

  /**
   * 报名活动（所有登录用户）
   */
  @PostMapping("/me")
  public ResponseEntity<ActivityRegistration> register(@PathVariable Long activityId) {
    return new ResponseEntity<>(registrationService.register(activityId), HttpStatus.CREATED);
  }

  /**
   * 取消报名（所有登录用户）
   */
  @DeleteMapping("/me")
  public ResponseEntity<Void> cancel(@PathVariable Long activityId) {
    registrationService.cancelRegistration(activityId);
    return ResponseEntity.noContent().build();
  }
}
