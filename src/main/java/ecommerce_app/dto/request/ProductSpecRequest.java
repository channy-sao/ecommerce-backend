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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductSpecRequest", description = "Request object for product specification")
public class ProductSpecRequest {

  @NotBlank(message = "Spec text is required")
  @Size(max = 255, message = "Spec text must not exceed 255 characters")
  @Schema(description = "Specification text", example = "Bluetooth 5.1")
  private String specText;

  @Min(value = 0, message = "Sort order must be 0 or greater")
  @Builder.Default
  @Schema(description = "Display sort order", example = "0", defaultValue = "0")
  private Integer sortOrder = 0;
}
