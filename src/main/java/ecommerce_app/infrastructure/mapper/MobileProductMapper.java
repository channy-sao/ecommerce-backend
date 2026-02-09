package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.product.model.dto.MobileProductDetailResponse;
import ecommerce_app.modules.product.model.dto.MobileProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import org.springframework.stereotype.Service;

@Service
public class MobileProductMapper {

  private static final int SHORT_DESCRIPTION_LENGTH = 100;

  public MobileProductResponse toListResponse(Product product) {
    return MobileProductResponse.builder()
        .id(product.getId())
        .uuid(product.getUuid())
        .name(product.getName())
        .shortDescription(truncateDescription(product.getDescription()))
        .price(product.getPrice())
        .image(product.getImage())
        .isFeature(product.getIsFeature())
        .favoritesCount(product.getFavoritesCount())
        .stockQuantity(product.getStockQuantity())
        .inStock(product.getStockQuantity() > 0)
        .categoryId(product.getCategory().getId())
        .categoryName(product.getCategory().getName())
        .build();
  }

  public MobileProductDetailResponse toDetailResponse(Product product) {
    return MobileProductDetailResponse.builder()
        .id(product.getId())
        .uuid(product.getUuid())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .image(product.getImage())
        .isFeature(product.getIsFeature())
        .favoritesCount(product.getFavoritesCount())
        .stockQuantity(product.getStockQuantity())
        .inStock(product.getStockQuantity() > 0)
        .categoryId(product.getCategory().getId())
        .categoryName(product.getCategory().getName())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }

  private String truncateDescription(String description) {
    if (description == null) return null;
    return description.length() > SHORT_DESCRIPTION_LENGTH
        ? description.substring(0, SHORT_DESCRIPTION_LENGTH) + "..."
        : description;
  }
}
