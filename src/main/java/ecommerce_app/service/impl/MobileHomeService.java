package ecommerce_app.service.impl;

import ecommerce_app.dto.response.MobileHomeScreenResponse;
import ecommerce_app.service.MobileProductService;
import ecommerce_app.service.MobilePromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class MobileHomeService {

  private final MobileProductService productService;
  private final MobilePromotionService promotionService;
  private final MobileCategoryService mobileCategoryService;
  private final MobileBannerServiceImpl bannerService;
  private final Executor taskExecutor;

  public MobileHomeScreenResponse getHomeScreenData(
      int bannerSize,
      int featuredPromotionsSize,
      int featuredProductsSize,
      int newArrivalsSize,
      int popularProductsSize) {

    // Execute all queries in parallel - use proper return types
    var bannersFuture =
        CompletableFuture.supplyAsync(() -> bannerService.getHomeBanners(bannerSize), taskExecutor);

    var promotionsFuture =
        CompletableFuture.supplyAsync(
            () -> promotionService.getFeaturedPromotions(featuredPromotionsSize), taskExecutor);

    var featuredProductsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getFeaturedProducts(1, featuredProductsSize).getContent(),
            taskExecutor);

    var newArrivalsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getNewArrivals(1, newArrivalsSize).getContent(), taskExecutor);

    var popularProductsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getPopularProducts(1, popularProductsSize).getContent(),
            taskExecutor);

    var categoriesFuture =
        CompletableFuture.supplyAsync(mobileCategoryService::getMinimalCategories, taskExecutor);

    // Wait for all to complete and build response
    CompletableFuture.allOf(
            promotionsFuture,
            featuredProductsFuture,
            newArrivalsFuture,
            popularProductsFuture,
            categoriesFuture)
        .join();

    return MobileHomeScreenResponse.builder()
        .banners(bannersFuture.join())
        .featuredPromotions(promotionsFuture.join())
        .featuredProducts(featuredProductsFuture.join())
        .newArrivals(newArrivalsFuture.join())
        .popularProducts(popularProductsFuture.join())
        .categories(categoriesFuture.join())
        .build();
  }
}
