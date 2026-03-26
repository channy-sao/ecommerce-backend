package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brand response payload")
public class BrandResponse {

  @Schema(description = "Unique brand ID", example = "1")
  private Long id;

  @Schema(description = "Brand name", example = "Nike")
  private String name;

  @Schema(description = "Brand description", example = "Just Do It")
  private String description;

  @Schema(
      description = "Full URL of the brand logo",
      example = "https://example.com/uploads/brands/nike.png")
  private String logo;

  @Schema(description = "Whether the brand is currently active", example = "true")
  private Boolean isActive;

  @Schema(description = "Display order priority (lower = higher priority)", example = "1")
  private Integer displayOrder;

  @Schema(description = "Date and time the brand was created", example = "2024-01-01T10:00:00")
  private String createdAt;
}
