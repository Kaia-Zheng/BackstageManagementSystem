package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.Activity;
import tech.wetech.admin3.sys.model.Activity.Status;

import java.util.List;

/**
 * 活动 Repository
 *
 * @author admin3
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

  @Query("from Activity a where (:title is null or a.title like concat('%', :title, '%')) " +
    "and (:status is null or a.status = :status) " +
    "and (:clubId is null or a.club.id = :clubId)")
  Page<Activity> findByConditions(@Param("title") String title,
                                   @Param("status") Status status,
                                   @Param("clubId") Long clubId,
                                   Pageable pageable);

  @Query("select count(a) from Activity a where a.status = :status and a.creator.id = :userId")
  long countByStatusAndCreatorId(@Param("status") Activity.Status status, @Param("userId") Long userId);

  @Query("from Activity a where a.creator.id = :userId order by a.createdTime desc")
  java.util.List<Activity> findByCreatorId(@Param("userId") Long userId);
}
