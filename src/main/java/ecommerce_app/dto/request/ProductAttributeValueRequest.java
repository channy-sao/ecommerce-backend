package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating or updating an attribute value */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueRequest {

  private Long id;

  @NotNull(message = "Attribute definition ID is required")
  private Long attributeDefinitionId;

  @NotBlank(message = "Attribute value is required")
  @Size(min = 1, max = 100, message = "Attribute value must be between 1 and 100 characters")
  private String value;

  @Builder.Default private Integer displayOrder = 0;

  @Builder.Default private Boolean isActive = true;
}
