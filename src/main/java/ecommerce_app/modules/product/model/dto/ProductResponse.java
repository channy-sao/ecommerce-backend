package ecommerce_app.modules.product.model.dto;

import ecommerce_app.infrastructure.model.response.AuditUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductResponse", description = "Response object for product creation or update")
public class ProductResponse {

  @Schema(description = "Id of the product", example = "1")
  private Long id;

  @Schema(description = "UUID of the product")
  private UUID uuid;

  @Schema(description = "Name of the product", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Description of the product",
      example = "Latest Apple smartphone with A17 chip")
  private String description;

  @Schema(description = "Price of the product", example = "1299.99")
  private BigDecimal price;

  // CHANGED: split into primaryImage (convenience) + images (full list of URLs)
  @Schema(description = "Primary image URL (first by sort order)")
  private String primaryImage;

  @Schema(description = "All image URLs in sort order")
  private List<ProductImageDto> images;

  @Schema(description = "Category id of the product belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Category name of the product belongs to", example = "Electronic")
  private String categoryName;

  @Schema(description = "Indicates whether the product is featured", example = "true")
  private Boolean isFeature;

  @Schema(description = "Number of times the product has been favorited", example = "100")
  private Integer favoritesCount;

  @Schema(description = "Timestamp when the product was created")
  private LocalDateTime createdAt;

  @Schema(description = "Timestamp when the product was last updated")
  private LocalDateTime updatedAt;

  @Schema(description = "User who created the product")
  private AuditUserDto createdBy;

  @Schema(description = "User who last updated the product")
  private AuditUserDto updatedBy;
}
