package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POSOrderRequest {

  @NotNull(message = "Items are required")
  @Size(min = 1, message = "At least one item is required")
  private List<POSOrderItemRequest> items;

  private String customerName;
  private String customerEmail;
  private String customerPhone;

  @NotNull(message = "Payment method is required")
  private String paymentMethod;

  private BigDecimal discountAmount;
  private BigDecimal cashReceived;
  private String discountNote;
  private String notes;
}
