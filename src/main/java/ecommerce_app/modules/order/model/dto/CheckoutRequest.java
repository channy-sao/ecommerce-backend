package ecommerce_app.modules.order.model.dto;

import ecommerce_app.constant.enums.PaymentMethod;
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

  @NotNull(message = "Cart ID is required")
  @Schema(description = "ID of the cart to be checked out", example = "1001")
  private Long cartId;

  @NotNull(message = "Payment method is required")
  @Schema(description = "Payment method selected by the user", example = "CASH")
  private PaymentMethod paymentMethod;
}
