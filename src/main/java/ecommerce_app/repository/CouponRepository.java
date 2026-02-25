// CouponRepository.java
package ecommerce_app.repository;

import ecommerce_app.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CouponRepository
    extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {

  Optional<Coupon> findByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCase(String code);

  @Query(
      """
        SELECT c FROM Coupon c
        WHERE UPPER(c.code) = UPPER(:code)
        AND c.isActive = true
        AND (c.startDate IS NULL OR c.startDate <= :now)
        AND (c.endDate   IS NULL OR c.endDate   >= :now)
        """)
  Optional<Coupon> findValidByCode(@Param("code") String code, @Param("now") LocalDateTime now);
}
