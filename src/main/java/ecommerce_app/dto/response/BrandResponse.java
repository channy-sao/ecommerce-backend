package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
  private Long id;
  private String name;
  private String description;
  private String logo; // full URL
  private Boolean isActive;
  private Integer displayOrder;
  private String createdAt;
}
