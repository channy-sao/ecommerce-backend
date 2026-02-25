// ApplyCouponRequest.java — mobile sends this at checkout
package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {

  @NotBlank private String code;

  @NotNull private BigDecimal orderTotal; // current cart total to calculate discount
}
