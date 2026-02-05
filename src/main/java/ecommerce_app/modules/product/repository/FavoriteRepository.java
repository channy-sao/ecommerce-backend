package ecommerce_app.modules.product.repository;

import ecommerce_app.modules.product.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  boolean existsByUserIdAndProductId(Long userId, Long productId);

  Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

  @Query(
      """
    select f.product.id
    from Favorite f
    where f.user.id = :userId
  """)
  List<Long> findFavoriteProductIds(@Param("userId") Long userId);

  @Query(
      """
    select count(f)
    from Favorite f
    where f.product.id = :productId
  """)
  long countByProductId(@Param("productId") Long productId);
}
