package ecommerce_app.modules.product.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductRequest", description = "Request object for product creation or update")
public class ProductRequest {

  @Schema(description = "Name of the product", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Description of the product",
      example = "Latest Apple smartphone with A17 chip")
  private String description;

  @Schema(description = "Price of the product", example = "1299.99")
  private BigDecimal price;

  // CHANGED: List<MultipartFile> instead of single MultipartFile
  @Schema(description = "Image files for the product", type = "array", format = "binary")
  private List<MultipartFile> images;

  // IDs of existing images to remove
  @Schema(description = "IDs of existing images to remove (for updates)", example = "[1, 2]")
  private List<Long> removeImageIds;

  // Full ordered list of existing image IDs (after removals) for reordering
  @Schema(
      description = "Ordered list of existing image IDs for reordering (after removals)",
      example = "[3, 4, 5]")
  private List<Long> imageOrder;

  @Schema(description = "ID of the category the product belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Indicates whether the product is featured", example = "true")
  private Boolean isFeature;
}
