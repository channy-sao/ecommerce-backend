package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Simplified category response for embedded or list use")
public class SimpleCategoryResponse {

  @Schema(description = "Unique category ID", example = "1")
  private Long id;

  @Schema(description = "Category name", example = "Smartphones")
  private String name;

  @Schema(description = "Icon name or URL for the category", example = "smartphone")
  private String icon;

  @Schema(description = "Display order priority (lower = higher priority)", example = "1")
  private Integer displayOrder;

  @Schema(
      description = "Short description of the category",
      example = "All smartphones and mobile devices")
  private String description;
}
