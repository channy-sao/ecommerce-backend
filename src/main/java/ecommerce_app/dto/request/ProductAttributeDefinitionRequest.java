package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating or updating an attribute definition */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeDefinitionRequest {

  private Long id;

  @NotBlank(message = "Attribute name is required")
  @Size(min = 2, max = 50, message = "Attribute name must be between 2 and 50 characters")
  private String name;

  @Size(max = 100, message = "Display name must not exceed 100 characters")
  private String displayName;

  @Builder.Default private Boolean isActive = true;
}
