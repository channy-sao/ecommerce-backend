package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.ShippingMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Schema(description = "Request payload for checking out a cart")
public class CheckoutRequest {

  @NotNull(message = "Payment method is required")
  @Schema(description = "Payment method selected by the user", example = "CASH")
  private PaymentMethod paymentMethod;

  @Schema(description = "Shipping address for the order")
  private Long shippingAddress;

  @Schema(
      description = "Shipping method selected",
      example = "STANDARD",
      allOf = ShippingMethod.class)
  // Require it or set default
  @NotNull(message = "Shipping method is required")
  private ShippingMethod shippingMethod;

  @Schema(description = "Promotion code for order-level discounts (e.g., free shipping)")
  private String promotionCode;
}
