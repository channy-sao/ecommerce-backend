package ecommerce_app.modules.cart.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {
  private Long id;
  private ProductResponse product;
  private Integer quantity;
  private BigDecimal itemTotal;  // product.price * quantity
}
