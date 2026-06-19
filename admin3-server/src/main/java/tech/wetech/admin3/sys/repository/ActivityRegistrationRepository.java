package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.ActivityRegistration;

import java.util.Optional;

/**
 * 活动报名 Repository
 *
 * @author admin3
 */
@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

  @Query("from ActivityRegistration ar where ar.activity.id = :activityId and ar.status = tech.wetech.admin3.sys.model.ActivityRegistration.RegisterStatus.REGISTERED")
  Page<ActivityRegistration> findByActivityId(@Param("activityId") Long activityId, Pageable pageable);

  @Query("from ActivityRegistration ar where ar.user.id = :userId and ar.status = tech.wetech.admin3.sys.model.ActivityRegistration.RegisterStatus.REGISTERED")
  Page<ActivityRegistration> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("from ActivityRegistration ar where ar.activity.id = :activityId and ar.user.id = :userId")
  Optional<ActivityRegistration> findByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

  @Query("select count(ar) from ActivityRegistration ar where ar.activity.id = :activityId and ar.status = tech.wetech.admin3.sys.model.ActivityRegistration.RegisterStatus.REGISTERED")
  long countByActivityId(@Param("activityId") Long activityId);

  @Modifying
  @Query("delete from ActivityRegistration ar where ar.activity.id = :activityId and ar.user.id = :userId")
  void deleteByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
