package tech.wetech.admin3.sys.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.sys.model.Club;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.ClubRepository;
import tech.wetech.admin3.sys.repository.ClubMemberRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.PageDTO;

import java.time.LocalDate;

/**
 * 社团 Service
 *
 * @author admin3
 */
@Service
public class ClubService {

  private final ClubRepository clubRepository;
  private final UserRepository userRepository;
  private final ClubMemberRepository clubMemberRepository;

  public ClubService(ClubRepository clubRepository,
                      UserRepository userRepository,
                      ClubMemberRepository clubMemberRepository) {
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
    this.clubMemberRepository = clubMemberRepository;
  }

  public PageDTO<Club> findClubs(Pageable pageable, String name, Club.Category category, Club.State state) {
    Page<Club> page = clubRepository.findByConditions(name, category, state, pageable);
    return new PageDTO<>(page.getContent(), page.getTotalElements());
  }

  public Club findClub(Long clubId) {
    return clubRepository.findById(clubId)
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "社团不存在"));
  }

  @Transactional
  public Club createClub(String name, String description, Club.Category category,
                          LocalDate foundedDate, String avatar, Long ownerId) {
    // 社团名称唯一
    if (clubRepository.existsByName(name)) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "社团名称已存在");
    }
    Club club = new Club();
    club.setName(name);
    club.setDescription(description);
    club.setCategory(category);
    club.setFoundedDate(foundedDate);
    club.setAvatar(avatar);
    club.setState(Club.State.ACTIVE);
    if (ownerId != null) {
      User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "负责人不存在"));
      club.setOwner(owner);
    }
    club = clubRepository.save(club);
    // 同步成员数
    club.setMemberCount((int) clubMemberRepository.countByClubId(club.getId()));
    return club;
  }

  @Transactional
  public Club updateClub(Long clubId, String name, String description, Club.Category category,
                          LocalDate foundedDate, Club.State state, String avatar, Long ownerId) {
    Club club = findClub(clubId);
    if (name != null && !name.equals(club.getName())) {
      // 检查新名称是否冲突
      if (clubRepository.existsByNameAndIdNot(name, clubId)) {
        throw new BusinessException(CommonResultStatus.PARAM_ERROR, "社团名称已存在");
      }
      club.setName(name);
    }
    if (description != null) club.setDescription(description);
    if (category != null) club.setCategory(category);
    if (foundedDate != null) club.setFoundedDate(foundedDate);
    if (state != null) club.setState(state);
    if (avatar != null) club.setAvatar(avatar);
    if (ownerId != null) {
      User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "负责人不存在"));
      club.setOwner(owner);
    }
    return clubRepository.save(club);
  }

  @Transactional
  public void deleteClub(Long clubId) {
    Club club = findClub(clubId);
    clubRepository.delete(club);
  }

  @Transactional
  public void refreshMemberCount(Long clubId) {
    Club club = findClub(clubId);
    club.setMemberCount((int) clubMemberRepository.countByClubId(clubId));
    clubRepository.save(club);
  }
}
