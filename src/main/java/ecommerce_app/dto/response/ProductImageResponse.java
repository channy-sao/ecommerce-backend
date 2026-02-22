package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    name = "ProductImageDto",
    description = "DTO representing a product image with its URL and sort order")
public class ProductImageResponse {
  @Schema(description = "Id of the product image", example = "1")
  private Long id;

  @Schema(
      description = "URL of the product image",
      example = "https://example.com/images/product1.jpg")
  private String imagePath; // full URL

  @Schema(description = "Sort order of the image for display purposes", example = "1")
  private Integer sortOrder;
}
