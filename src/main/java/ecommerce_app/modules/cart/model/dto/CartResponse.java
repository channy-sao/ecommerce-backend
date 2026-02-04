package ecommerce_app.modules.cart.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.CartStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {
  private Long id;
  private UUID uuid;
  private BigDecimal total;
  private Integer totalItems;
  private BigDecimal subtotal;
  private BigDecimal discount;
  private CartStatus status;
  private Instant createdAt;
  private Instant updatedAt;
  private List<CartItemResponse> items;

  // User info (optional)
  private Long userId;
  private String userName;
  private String userEmail;
}
