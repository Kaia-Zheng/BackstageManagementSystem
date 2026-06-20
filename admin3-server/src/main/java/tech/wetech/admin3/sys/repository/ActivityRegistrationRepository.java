package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.ActivityRegistration;

import java.util.List;
import java.util.Optional;

/**
 * 活动报名 Repository
 *
 * @author admin3
 */
@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

  // 使用原生 SQL 查询已报名记录（用于统计报名人数）
  @Query(value = "select * from activity_registration ar where ar.activity_id = :activityId and ar.status = 'REGISTERED'", nativeQuery = true)
  Page<ActivityRegistration> findByActivityId(@Param("activityId") Long activityId, Pageable pageable);

  // 查询用户所有报名记录（不区分状态）
  @Query("from ActivityRegistration ar where ar.user.id = :userId")
  Page<ActivityRegistration> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("from ActivityRegistration ar where ar.activity.id = :activityId and ar.user.id = :userId")
  Optional<ActivityRegistration> findByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

  @Query("from ActivityRegistration ar where ar.activity.id = :activityId and ar.user.id = :userId")
  Page<ActivityRegistration> findPageByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId, Pageable pageable);

  @Query(value = "select count(*) from activity_registration ar where ar.activity_id = :activityId and ar.status = 'REGISTERED'", nativeQuery = true)
  long countByActivityId(@Param("activityId") Long activityId);

  @Modifying
  @Query("delete from ActivityRegistration ar where ar.activity.id = :activityId and ar.user.id = :userId")
  void deleteByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

  // 查询某个活动的所有报名记录（不区分状态）
  Page<ActivityRegistration> findAllByActivityId(Long activityId, Pageable pageable);

  @Query("from ActivityRegistration ar where ar.user.id = :userId")
  java.util.List<ActivityRegistration> findAllByUserId(@Param("userId") Long userId);
}
