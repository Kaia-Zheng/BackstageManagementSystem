package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.common.authz.RequiresPermissions;
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.service.ClubService;
import tech.wetech.admin3.sys.service.dto.PageDTO;

import java.time.LocalDate;

/**
 * 社团管理 Controller
 *
 * @author admin3
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/clubs")
public class ClubController {

  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  /**
   * 查询社团列表（所有登录用户可见）
   */
  @GetMapping
  public ResponseEntity<PageDTO<Club>> findClubs(Pageable pageable,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) Club.Category category,
                                                   @RequestParam(required = false) Club.State state) {
    return ResponseEntity.ok(clubService.findClubs(pageable, name, category, state));
  }

  /**
   * 查询单个社团
   */
  @GetMapping("/{clubId}")
  public ResponseEntity<Club> findClub(@PathVariable Long clubId) {
    return ResponseEntity.ok(clubService.findClub(clubId));
  }

  /**
   * 创建社团（管理员）
   */
  @RequiresPermissions("club:create")
  @PostMapping
  public ResponseEntity<Club> createClub(@RequestBody @Valid ClubRequest request) {
    Club club = clubService.createClub(
      request.name(),
      request.description(),
      request.category(),
      request.foundedDate(),
      request.avatar(),
      request.ownerId()
    );
    return new ResponseEntity<>(club, HttpStatus.CREATED);
  }

  /**
   * 更新社团（管理员）
   */
  @RequiresPermissions("club:update")
  @PutMapping("/{clubId}")
  public ResponseEntity<Club> updateClub(@PathVariable Long clubId, @RequestBody ClubRequest request) {
    return ResponseEntity.ok(clubService.updateClub(
      clubId,
      request.name(),
      request.description(),
      request.category(),
      request.foundedDate(),
      request.state(),
      request.avatar(),
      request.ownerId()
    ));
  }

  /**
   * 删除社团（管理员）
   */
  @RequiresPermissions("club:delete")
  @DeleteMapping("/{clubId}")
  public ResponseEntity<Void> deleteClub(@PathVariable Long clubId) {
    clubService.deleteClub(clubId);
    return ResponseEntity.noContent().build();
  }

  record ClubRequest(@NotBlank String name,
                      String description,
                      @NotNull Club.Category category,
                      LocalDate foundedDate,
                      Club.State state,
                      String avatar,
                      Long ownerId) {
  }
}
