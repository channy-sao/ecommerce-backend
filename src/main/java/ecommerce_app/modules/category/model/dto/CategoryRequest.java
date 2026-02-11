package ecommerce_app.modules.category.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
    name = "CategoryRequest",
    description = "Request object for category registration or update")
public class CategoryRequest {
  @Schema(description = "Name of the category", example = "Electronics")
  private String name;

  @Schema(
      description = "Detailed description of the category",
      example = "Devices and gadgets including phones, laptops, etc.")
  private String description;

  @Size(max = 10, message = "Icon must not exceed 10 characters")
  @Schema(description = "Emoji icon for the category", example = "📱", maxLength = 10)
  private String icon;

  @Min(value = 0, message = "Display order must be 0 or greater")
  @Schema(
      description = "Display order for sorting categories (lower numbers appear first)",
      example = "1",
      minimum = "0")
  private Integer displayOrder;
}
