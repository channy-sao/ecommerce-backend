package ecommerce_app.modules.product.service;

import java.util.List;

public interface FavoriteService {
  void favorite(Long userId, Long productId);

  void unfavorite(Long userId, Long productId);

  void toggleFavorite(Long userId, Long productId);

  boolean isFavorite(Long userId, Long productId);

  List<Long> getFavoriteProductIds(Long userId);

  long countFavorites(Long productId);
}
