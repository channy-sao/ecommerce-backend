package ecommerce_app.modules.home.service;

import ecommerce_app.modules.category.service.impl.MobileCategoryService;
import ecommerce_app.modules.home.model.dto.MobileHomeScreenResponse;
import ecommerce_app.modules.product.service.MobileProductService;
import ecommerce_app.modules.promotion.service.MobilePromotionService;
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
  private final Executor taskExecutor;

  public MobileHomeScreenResponse getHomeScreenData(
      int featuredPromotionsSize,
      int featuredProductsSize,
      int newArrivalsSize,
      int popularProductsSize) {

    // Execute all queries in parallel - use proper return types
    var promotionsFuture =
        CompletableFuture.supplyAsync(
            () -> promotionService.getFeaturedPromotions(featuredPromotionsSize), taskExecutor);

    var featuredProductsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getFeaturedProducts(featuredProductsSize), taskExecutor);

    var newArrivalsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getNewArrivals(newArrivalsSize), taskExecutor);

    var popularProductsFuture =
        CompletableFuture.supplyAsync(
            () -> productService.getPopularProducts(popularProductsSize), taskExecutor);

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
        .featuredPromotions(promotionsFuture.join())
        .featuredProducts(featuredProductsFuture.join())
        .newArrivals(newArrivalsFuture.join())
        .popularProducts(popularProductsFuture.join())
        .categories(categoriesFuture.join())
        .build();
  }
}
