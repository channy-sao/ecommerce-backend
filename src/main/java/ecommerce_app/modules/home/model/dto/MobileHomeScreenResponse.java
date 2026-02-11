package ecommerce_app.modules.home.model.dto;

import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.category.model.dto.SimpleCategoryResponse;
import ecommerce_app.modules.product.model.dto.MobileProductListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileHomeScreenResponse {

  // Add carousel/banners
  private List<BannerResponse> banners;
  // Top carousel banners (3-5 items max)
  private List<MobilePromotionListResponse> featuredPromotions;
  // Highlighted products (6-10 items)
  private List<MobileProductListResponse> featuredProducts;
  // Recent additions (6-10 items)
  private List<MobileProductListResponse> newArrivals;
  // Trending items (6-10 items)
  private List<MobileProductListResponse> popularProducts;
  // Simple category list for browsing
  private List<SimpleCategoryResponse> categories;
}
