package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Mobile home screen response containing all sections for the home page")
public class MobileHomeScreenResponse {

  @Schema(description = "Hero carousel banners displayed at the top of the home screen")
  private List<BannerResponse> heroBanners;

  @Schema(description = "Middle section banners displayed between content blocks")
  private List<BannerResponse> middleBanners;

  @Schema(description = "Featured promotions displayed as a top carousel (3–5 items)")
  private List<MobilePromotionListResponse> featuredPromotions;

  @Schema(description = "Highlighted featured products (6–10 items)")
  private List<MobileProductListResponse> featuredProducts;

  @Schema(description = "List of all brands with logo and name for quick access")
  private List<BrandResponse> brands;

  @Schema(description = "Recently added products (6–10 items)")
  private List<MobileProductListResponse> newArrivals;

  @Schema(
      description =
          "Trending or popular products based on overall popularity, no login required (6–10 items)")
  private List<MobileProductListResponse> popularProducts;

  @Schema(description = "Simple category list for browsing")
  private List<SimpleCategoryResponse> categories;
}
