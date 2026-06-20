package tech.wetech.admin3.sys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.ClubJoinApplication;
import tech.wetech.admin3.sys.model.ClubJoinApplication.ApplicationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubJoinApplicationRepository extends JpaRepository<ClubJoinApplication, Long> {

  @Query("from ClubJoinApplication a where a.user.id = :userId order by a.createTime desc")
  List<ClubJoinApplication> findByUserId(@Param("userId") Long userId);

  @Query("from ClubJoinApplication a where a.club.id = :clubId and a.status = :status order by a.createTime desc")
  List<ClubJoinApplication> findByClubIdAndStatus(@Param("clubId") Long clubId, @Param("status") ApplicationStatus status);

  @Query("from ClubJoinApplication a where a.club.id = :clubId order by a.createTime desc")
  List<ClubJoinApplication> findByClubId(@Param("clubId") Long clubId);

  @Query("from ClubJoinApplication a where a.club.id = :clubId and a.user.id = :userId")
  Optional<ClubJoinApplication> findByClubIdAndUserId(@Param("clubId") Long clubId, @Param("userId") Long userId);

  @Query("select case when count(a) > 0 then true else false end from ClubJoinApplication a " +
    "where a.club.id = :clubId and a.user.id = :userId and a.status = :status")
  boolean existsByClubIdAndUserIdAndStatus(@Param("clubId") Long clubId, @Param("userId") Long userId, @Param("status") ApplicationStatus status);
}
