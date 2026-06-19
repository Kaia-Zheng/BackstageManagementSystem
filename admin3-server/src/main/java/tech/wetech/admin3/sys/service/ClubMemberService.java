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
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.model.ClubMember;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.ClubMemberRepository;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.PageDTO;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

/**
 * 社团成员 Service
 *
 * @author admin3
 */
@Service
public class ClubMemberService {

  private final ClubMemberRepository clubMemberRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  public ClubMemberService(ClubMemberRepository clubMemberRepository,
                            ClubRepository clubRepository,
                            UserRepository userRepository) {
    this.clubMemberRepository = clubMemberRepository;
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
  }

  public PageDTO<ClubMember> findMembers(Long clubId, ClubMember.MemberRole role, Pageable pageable) {
    Page<ClubMember> page = clubMemberRepository.findByClubId(clubId, role, pageable);
    return new PageDTO<>(page.getContent(), page.getTotalElements());
  }

  /**
   * 添加成员
   * 仅限管理员和社团负责人操作
   */
  @Transactional
  public ClubMember addMember(Long clubId, Long userId, ClubMember.MemberRole role) {
    checkManagePermission(clubId);
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "社团不存在"));
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "用户不存在"));
    if (clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "该用户已是社团成员");
    }
    ClubMember member = new ClubMember();
    member.setClub(club);
    member.setUser(user);
    member.setRole(role == null ? ClubMember.MemberRole.MEMBER : role);
    member = clubMemberRepository.save(member);
    // 更新成员数
    club.setMemberCount((int) clubMemberRepository.countByClubId(clubId));
    clubRepository.save(club);
    return member;
  }

  /**
   * 移除成员
   */
  @Transactional
  public void removeMember(Long clubId, Long userId) {
    checkManagePermission(clubId);
    if (!clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
      throw new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "该用户不是社团成员");
    }
    clubMemberRepository.deleteByClubIdAndUserId(clubId, userId);
    Club club = clubRepository.findById(clubId).orElseThrow();
    club.setMemberCount((int) clubMemberRepository.countByClubId(clubId));
    clubRepository.save(club);
  }

  /**
   * 更新成员角色
   */
  @Transactional
  public ClubMember updateMemberRole(Long clubId, Long userId, ClubMember.MemberRole role) {
    checkManagePermission(clubId);
    ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, userId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "成员不存在"));
    member.setRole(role);
    return clubMemberRepository.save(member);
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
    if (!isAdmin && !isOwner) {
      throw new BusinessException(CommonResultStatus.FORBIDDEN, "没有权限管理社团成员");
    }
  }
}
