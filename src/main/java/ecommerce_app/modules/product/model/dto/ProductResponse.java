package ecommerce_app.modules.product.model.dto;

import ecommerce_app.infrastructure.model.response.AuditUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Schema(description = "Image file for the product", type = "string")
  private String image;

  @Schema(description = "Category id of the product belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Category name of the product belongs to", example = "Electronic")
  private String categoryName;

  @Schema(description = "Indicates whether the product is featured", example = "true")
  private Boolean isFeature;

  @Schema(description = "Timestamp when the product was created", example = "2025-06-01T12:34:56")
  private Instant createdAt;

  @Schema(
      description = "Timestamp when the product was last updated",
      example = "2025-06-04T15:00:00")
  private Instant updatedAt;

  @Schema(description = "ID of the user who created the product")
  private AuditUserDto createdBy;

  @Schema(description = "ID of the user who last updated the product")
  private AuditUserDto updatedBy;
}
