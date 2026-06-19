package tech.wetech.admin3.sys.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.wetech.admin3.sys.model.Club;

/**
 * 社团 Repository
 *
 * @author admin3
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

  @Query("from Club c where (:name is null or c.name like concat('%', :name, '%')) " +
    "and (:category is null or c.category = :category) " +
    "and (:state is null or c.state = :state)")
  Page<Club> findByConditions(@Param("name") String name,
                               @Param("category") Club.Category category,
                               @Param("state") Club.State state,
                               Pageable pageable);
}
