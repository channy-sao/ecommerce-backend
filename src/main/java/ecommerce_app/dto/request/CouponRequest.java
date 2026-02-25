// CouponRequest.java
package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.CouponDiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

  @NotBlank
  @Size(min = 3, max = 50)
  private String code;

  @Size(max = 500)
  private String description;

  @NotNull private CouponDiscountType discountType;

  @DecimalMin("0.01")
  private BigDecimal discountValue;

  @DecimalMin("0.00")
  private BigDecimal minOrderAmount;

  @DecimalMin("0.01")
  private BigDecimal maxDiscount; // optional cap

  @Min(1)
  private Integer usageLimit; // null = unlimited

  @Min(1)
  @Builder.Default
  private Integer usagePerUser = 1;

  private Boolean isActive = true;

  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
