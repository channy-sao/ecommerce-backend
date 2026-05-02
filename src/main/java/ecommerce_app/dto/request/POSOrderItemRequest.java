package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POSOrderItemRequest {
  @NotNull(message = "Product ID is required")
  private Long productId;

  // ✅ NEW - Optional variant ID for products with variants
  private Long variantId;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be greater than 0")
  private Integer quantity;

  private BigDecimal unitPrice;
}
