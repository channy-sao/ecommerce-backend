package ecommerce_app.modules.order.model.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
  private Long id;
  private String name;
  private String image;
  private BigDecimal revenue;
  private Integer quantity;
}
