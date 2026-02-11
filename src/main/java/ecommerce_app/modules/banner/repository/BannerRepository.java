package ecommerce_app.modules.banner.repository;

import ecommerce_app.modules.banner.model.entity.Banner;
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
  List<Banner> findActiveHomeBanners(
      @Param("position") String position,
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
}