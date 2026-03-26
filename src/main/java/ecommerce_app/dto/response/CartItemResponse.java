package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Cart item response payload")
public class CartItemResponse {

  @Schema(description = "Unique cart item ID", example = "1")
  private Long id;

  @Schema(description = "Product details for this cart item")
  private ProductResponse product;

  @Schema(description = "Quantity of the product in the cart", example = "2")
  private Integer quantity;

  @Schema(description = "Total price for this item (product price × quantity)", example = "99.98")
  private BigDecimal itemTotal;
}
