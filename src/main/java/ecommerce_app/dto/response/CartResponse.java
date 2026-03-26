package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.CartStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Schema(description = "Shopping cart response payload")
public class CartResponse {

  @Schema(description = "Unique cart ID", example = "1")
  private Long id;

  @Schema(description = "Unique cart UUID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID uuid;

  @Schema(description = "Final total after discount", example = "89.99")
  private BigDecimal total;

  @Schema(description = "Total number of items in the cart", example = "3")
  private Integer totalItems;

  @Schema(description = "Subtotal before discount", example = "99.99")
  private BigDecimal subtotal;

  @Schema(description = "Total discount applied to the cart", example = "10.00")
  private BigDecimal discount;

  @Schema(description = "Current status of the cart", example = "ACTIVE")
  private CartStatus status;

  @Schema(description = "Date and time the cart was created", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Date and time the cart was last updated", example = "2024-01-01T12:00:00")
  private LocalDateTime updatedAt;

  @Schema(description = "List of items in the cart")
  private List<CartItemResponse> items;

  @Schema(description = "ID of the user who owns the cart", example = "42")
  private Long userId;

  @Schema(description = "Full name of the cart owner", example = "John Doe")
  private String userName;

  @Schema(description = "Email of the cart owner", example = "john.doe@example.com")
  private String userEmail;
}
