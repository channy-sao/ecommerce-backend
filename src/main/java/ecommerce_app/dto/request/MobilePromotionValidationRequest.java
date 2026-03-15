package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    name = "MobilePromotionValidationRequest",
    description = "Request object for validating a promotion code")
public class MobilePromotionValidationRequest {

  @NotBlank(message = "Promotion code is required")
  @Size(max = 50, message = "Promotion code must not exceed 50 characters")
  @Schema(description = "Promotion code to validate", example = "SUMMER2026")
  private String code;

  @NotNull(message = "User ID is required")
  @Positive(message = "User ID must be greater than 0")
  @Schema(description = "ID of the user applying the promotion", example = "1")
  private Long userId;

  @NotNull(message = "Cart total is required")
  @DecimalMin(value = "0.00", inclusive = true, message = "Cart total must be 0 or greater")
  @Schema(description = "Total cart amount before discount", example = "99.99")
  private BigDecimal cartTotal;
}
