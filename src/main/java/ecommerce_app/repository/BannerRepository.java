package ecommerce_app.repository;

import ecommerce_app.constant.enums.BannerPosition;
import ecommerce_app.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

  /**
   * Get active banners for home carousel
   * Ordered by display_order
   */

  @Query("""
            SELECT b FROM Banner b
            WHERE b.isActive = true
            AND b.position = :position
            AND (b.startDate IS NULL OR b.startDate <= :now)
            AND (b.endDate IS NULL OR b.endDate >= :now)
            ORDER BY b.displayOrder ASC
            """)
  List<Banner> findActiveByPosition(@Param("position") BannerPosition position,
                                    @Param("now") LocalDateTime now);

  /**
   * Get all active banners
   */
  @Query("""
      SELECT b FROM Banner b 
      WHERE b.isActive = true 
      ORDER BY b.displayOrder ASC
      """)
  List<Banner> findAllActive();

  @Query("""
    SELECT b
    FROM Banner b
    WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :filter, '%')) and b.isActive=:isActive
""")
  Page<Banner> findAllByTitleLike(@Param("filter") String filter, @Param("isActive") boolean isActive, Pageable pageable);

  @Query("""
    SELECT b
    FROM Banner b
    WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :filter, '%'))
""")
  Page<Banner> findAllByTitleLike(@Param("filter") String filter, Pageable pageable);
}