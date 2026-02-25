// CouponUsageRepository.java
package ecommerce_app.repository;

import ecommerce_app.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

  // How many times has this user used this coupon?
  int countByCouponIdAndUserId(Long couponId, Long userId);

  // Has this user already used this coupon on a specific order?
  boolean existsByCouponIdAndOrderId(Long couponId, Long orderId);
}
