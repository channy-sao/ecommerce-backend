package ecommerce_app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullPaymentRequest {
  private String customerPhone; // Customer's Bakong registered phone
  private BigDecimal amount;
  private String currency; // USD or KHR
  private String orderNumber; // Your order reference
  private String description; // Payment description
  private String customerName; // Optional: Customer name for verification
}
