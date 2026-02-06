package ecommerce_app.modules.product.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.product.model.entity.Favorite;
import ecommerce_app.modules.product.repository.FavoriteRepository;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.product.service.FavoriteService;
import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @Transactional
  @Override
  public void favorite(Long userId, Long productId) {
    log.info("Add favorite product. userId: {}, productId: {}", userId, productId);
    if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
      log.info("Favorite already exists. userId: {}, productId: {}", userId, productId);
      return;
    }
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    final var product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    Favorite favorite = new Favorite();
    favorite.setProduct(product);
    favorite.setUser(user);

    favoriteRepository.save(favorite);

    product.setFavoritesCount(product.getFavoritesCount() + 1);
    productRepository.save(product);
    log.info("Favorite added. userId: {}, productId: {}", userId, productId);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void unfavorite(Long userId, Long productId) {
    log.info("Remove favorite product. userId: {}, productId: {}", userId, productId);
    favoriteRepository
        .findByUserIdAndProductId(userId, productId)
        .ifPresent(favoriteRepository::delete);

    productRepository
        .findById(productId)
        .ifPresent(
            product -> {
              if (product.getFavoritesCount() > 0) {
                product.setFavoritesCount(product.getFavoritesCount() - 1);
                productRepository.save(product);
              }
            });
    log.info("Favorite removed. userId: {}, productId: {}", userId, productId);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void toggleFavorite(Long userId, Long productId) {
    log.info("Toggle favorite product. userId: {}, productId: {}", userId, productId);
    if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
      this.unfavorite(userId, productId);
    } else {
      this.favorite(userId, productId);
    }
    log.info("Favorite toggled. userId: {}, productId: {}", userId, productId);
  }

  @Transactional(readOnly = true)
  @Override
  public boolean isFavorite(Long userId, Long productId) {
    return favoriteRepository.existsByUserIdAndProductId(userId, productId);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Long> getFavoriteProductIds(Long userId) {
    return favoriteRepository.findFavoriteProductIds(userId);
  }

  @Transactional(readOnly = true)
  @Override
  public long countFavorites(Long productId) {
    return favoriteRepository.countByProductId(productId);
  }
}
