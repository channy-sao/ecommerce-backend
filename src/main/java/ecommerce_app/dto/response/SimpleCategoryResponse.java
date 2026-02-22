package ecommerce_app.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleCategoryResponse {
  private Long id;
  private String name;
  private String icon;
  private Integer displayOrder;
  private String description;
}
