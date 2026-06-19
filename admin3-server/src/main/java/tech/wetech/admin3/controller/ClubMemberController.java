package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.common.authz.RequiresPermissions;
import tech.wetech.admin3.sys.model.ClubMember;
import tech.wetech.admin3.sys.service.ClubMemberService;
import tech.wetech.admin3.sys.service.dto.PageDTO;

/**
 * 社团成员管理 Controller
 *
 * @author admin3
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/clubs/{clubId}/members")
public class ClubMemberController {

  private final ClubMemberService memberService;

  public ClubMemberController(ClubMemberService memberService) {
    this.memberService = memberService;
  }

  @GetMapping
  public ResponseEntity<PageDTO<ClubMember>> findMembers(@PathVariable Long clubId,
                                                          @RequestParam(required = false) ClubMember.MemberRole role,
                                                          Pageable pageable) {
    return ResponseEntity.ok(memberService.findMembers(clubId, role, pageable));
  }

  @RequiresPermissions("club:manage")
  @PostMapping
  public ResponseEntity<ClubMember> addMember(@PathVariable Long clubId, @RequestBody AddMemberRequest request) {
    ClubMember member = memberService.addMember(clubId, request.userId(), request.role());
    return new ResponseEntity<>(member, HttpStatus.CREATED);
  }

  @RequiresPermissions("club:manage")
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> removeMember(@PathVariable Long clubId, @PathVariable Long userId) {
    memberService.removeMember(clubId, userId);
    return ResponseEntity.noContent().build();
  }

  @RequiresPermissions("club:manage")
  @PutMapping("/{userId}/role")
  public ResponseEntity<ClubMember> updateRole(@PathVariable Long clubId,
                                                @PathVariable Long userId,
                                                @RequestBody UpdateRoleRequest request) {
    return ResponseEntity.ok(memberService.updateMemberRole(clubId, userId, request.role()));
  }

  record AddMemberRequest(Long userId, ClubMember.MemberRole role) {}
  record UpdateRoleRequest(ClubMember.MemberRole role) {}
}
