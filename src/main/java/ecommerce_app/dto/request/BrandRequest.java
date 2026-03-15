package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(name = "BrandRequest", description = "Request object for brand creation or update")
public class BrandRequest {

  @NotBlank(message = "Brand name is required")
  @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
  @Schema(description = "Name of the brand", example = "Apple")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(
      description = "Description of the brand",
      example = "American multinational technology company")
  private String description;

  @Schema(description = "Brand logo image (jpg, jpeg, png only, max 2MB)")
  private MultipartFile logo;

  @Builder.Default
  @Schema(description = "Whether the brand is active", example = "true", defaultValue = "true")
  private Boolean isActive = true;

  @Min(value = 0, message = "Display order must be 0 or greater")
  @Builder.Default
  @Schema(
      description = "Display order for sorting brands (lower numbers appear first)",
      example = "0",
      minimum = "0")
  private Integer displayOrder = 0;
}
