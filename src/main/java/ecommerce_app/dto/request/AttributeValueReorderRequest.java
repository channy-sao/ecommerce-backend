package ecommerce_app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Request DTO for reordering attribute values */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueReorderRequest {

  @NotEmpty(message = "Value order list cannot be empty")
  private List<Long> valueIds;
}
