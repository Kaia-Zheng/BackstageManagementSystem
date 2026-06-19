package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.Activity;

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
                                   @Param("status") Activity.Status status,
                                   @Param("clubId") Long clubId,
                                   Pageable pageable);

  /**
   * 普通登录用户可见的活动（已发布、进行中、已结束），待审核和已取消只对管理员和创建者可见
   */
  @Query("from Activity a where (:title is null or a.title like concat('%', :title, '%')) " +
    "and (:clubId is null or a.club.id = :clubId) " +
    "and a.status in (tech.wetech.admin3.sys.model.Activity.Status.PUBLISHED, " +
    "tech.wetech.admin3.sys.model.Activity.Status.ONGOING, " +
    "tech.wetech.admin3.sys.model.Activity.Status.ENDED)")
  Page<Activity> findVisibleByConditions(@Param("title") String title,
                                           @Param("clubId") Long clubId,
                                           Pageable pageable);
}
