package ecommerce_app.modules.category.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
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
    name = "CategoryResponse",
    description = "Response object for category registration or update")
public class CategoryResponse {
  @Schema(description = "Category ID", example = "1")
  private Long id;

  @Schema(description = "Name of the category", example = "Electronics", nullable = true)
  private String name;

  @Schema(
      description = "Detailed description of the category",
      example = "Devices and gadgets including phones, laptops, etc.")
  private String description;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd:MM:yyyy HH:mm:ss")
  @Schema(description = "Timestamp when the category was created", example = "2025-06-01T12:34:56")
  private LocalDateTime createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd:MM:yyyy HH:mm:ss")
  @Schema(
      description = "Timestamp when the category was last updated",
      example = "2025-06-04T15:00:00")
  private LocalDateTime updatedAt;

  @Schema(description = "ID of the user who created the category", example = "101")
  private Long createdBy;

  @Schema(description = "ID of the user who last updated the category", example = "102")
  private Long updatedBy;

  @Schema(description = "Products in category", example = "[]")
  private List<ProductResponse> products;
}
