// Response
package ecommerce_app.dto.response;

import lombok.Data;

@Data
public class ProductSpecResponse {
  private Long id;
  private String specText;
  private Integer sortOrder;
}