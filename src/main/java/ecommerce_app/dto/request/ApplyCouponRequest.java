package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "ApplyCouponRequest",
    description = "Request object for applying a coupon at checkout")
public class ApplyCouponRequest {

  @NotBlank(message = "Coupon code is required")
  @Size(max = 50, message = "Coupon code must not exceed 50 characters")
  @Schema(description = "Coupon code to apply", example = "SAVE20")
  private String code;

  @NotNull(message = "Order total is required")
  @DecimalMin(value = "0.00", inclusive = true, message = "Order total must be 0 or greater")
  @Schema(description = "Current cart total used to calculate discount", example = "99.99")
  private BigDecimal orderTotal;
}
