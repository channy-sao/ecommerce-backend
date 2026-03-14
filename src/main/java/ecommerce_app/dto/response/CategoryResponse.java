package ecommerce_app.dto.response;

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

  @Schema(description = "Emoji icon representing the category", example = "📱", nullable = true)
  private String icon;

  @Schema(description = "Display order of the category", example = "1")
  private Integer displayOrder;

  @Schema(
      description = "Detailed description of the category",
      example = "Devices and gadgets including phones, laptops, etc.")
  private String description;

  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd:MM:yyyy HH:mm:ss")
  @Schema(
      description = "Timestamp when the category was created",
      example = "2025-06-01T12:34:56.123Z")
  private LocalDateTime createdAt;

  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd:MM:yyyy HH:mm:ss")
  @Schema(
      description = "Timestamp when the category was last updated",
      example = "2025-06-01T12:34:56.123Z")
  private LocalDateTime updatedAt;

  @Schema(description = "ID of the user who created the category")
  private AuditUserDto createdBy;

  @Schema(description = "ID of the user who last updated the category")
  private AuditUserDto updatedBy;

  @Schema(description = "Total number of products in this category", example = "42")
  private Integer productCount;
}
