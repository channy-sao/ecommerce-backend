// Request
package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductSpecRequest {

  @NotBlank
  @Size(max = 255)
  private String specText;

  private Integer sortOrder = 0;
}
