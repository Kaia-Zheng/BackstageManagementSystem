package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.ClubMember;

import java.util.List;
import java.util.Optional;

/**
 * 社团成员 Repository
 *
 * @author admin3
 */
@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

  @Query("from ClubMember cm where cm.club.id = :clubId and (:role is null or cm.role = :role)")
  Page<ClubMember> findByClubId(@Param("clubId") Long clubId,
                                 @Param("role") ClubMember.MemberRole role,
                                 Pageable pageable);

  @Query("from ClubMember cm where cm.club.id = :clubId and cm.user.id = :userId")
  Optional<ClubMember> findByClubIdAndUserId(@Param("clubId") Long clubId, @Param("userId") Long userId);

  @Query("select count(cm) from ClubMember cm where cm.club.id = :clubId")
  long countByClubId(@Param("clubId") Long clubId);

  @Modifying
  @Query("delete from ClubMember cm where cm.club.id = :clubId and cm.user.id = :userId")
  void deleteByClubIdAndUserId(@Param("clubId") Long clubId, @Param("userId") Long userId);

  @Query("select case when count(cm) > 0 then true else false end from ClubMember cm " +
    "where cm.club.id = :clubId and cm.user.id = :userId")
  boolean existsByClubIdAndUserId(@Param("clubId") Long clubId, @Param("userId") Long userId);

  @Query("from ClubMember cm where cm.user.id = :userId")
  java.util.List<ClubMember> findByUserId(@Param("userId") Long userId);
}
