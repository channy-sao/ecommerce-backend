package ecommerce_app.modules.product.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

  @Schema(description = "Image file for the product", type = "string", format = "binary")
  private MultipartFile image;

  @Schema(description = "ID of the category the product belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Indicates whether the product is featured", example = "true")
  private Boolean isFeature;
}
