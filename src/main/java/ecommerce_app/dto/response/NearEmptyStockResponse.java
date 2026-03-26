package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

import ecommerce_app.constant.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product with near-empty or low stock level")
public class NearEmptyStockResponse {

  @Schema(description = "Unique product ID", example = "1")
  private Long id;

  @Schema(description = "Product name", example = "iPhone 15 Pro")
  private String name;

  @Schema(description = "Product price", example = "999.99")
  private BigDecimal price;

  @Schema(
      description = "Primary product image URL",
      example = "https://cdn.example.com/products/iphone15.jpg")
  private String primaryImage;

  @Schema(description = "Category name the product belongs to", example = "Smartphones")
  private String categoryName;

  @Schema(description = "Current stock quantity", example = "3")
  private int currentQuantity;

  @Schema(
      description = "Stock threshold below which the product is considered low stock",
      example = "10")
  private int threshold;

  @Schema(description = "Current stock status of the product", example = "LOW_STOCK")
  private StockStatus stockStatus;
}
