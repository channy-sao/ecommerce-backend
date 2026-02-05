package ecommerce_app.modules.product.service;

import java.util.List;

public interface FavoriteService {
  void addFavorite(Long userId, Long productId);

  void removeFavorite(Long userId, Long productId);

  boolean isFavorite(Long userId, Long productId);

  List<Long> getFavoriteProductIds(Long userId);

  long countFavorites(Long productId);
}
