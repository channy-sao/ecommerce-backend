package ecommerce_app.modules.category.model.dto;

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
    name = "CategoryRequest",
    description = "Request object for category registration or update")
public class CategoryRequest {
  @Schema(description = "Name of the category", example = "Electronics")
  private String name;

  @Schema(
      description = "Detailed description of the category",
      example = "Devices and gadgets including phones, laptops, etc.")
  private String description;
}
