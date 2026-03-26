package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Payment status response used by polling and webhook callbacks")
public class PaymentStatusResponse {

  @Schema(description = "Unique payment ID", example = "1")
  private Long paymentId;

  @Schema(description = "ID of the order associated with this payment", example = "42")
  private Long orderId;

  @Schema(description = "Payment gateway used to process the payment", example = "STRIPE")
  private PaymentGateway gateway;

  @Schema(description = "Current payment status", example = "PAID")
  private PaymentStatus status;

  @Schema(description = "Amount charged for the payment", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Currency code for the payment", example = "USD")
  private String currency;

  @Schema(description = "Date and time the payment was completed", example = "2024-01-01T10:00:00")
  private LocalDateTime paidAt;

  @Schema(
      description = "Human-readable message about the payment result",
      example = "Payment successful")
  private String message;
}
