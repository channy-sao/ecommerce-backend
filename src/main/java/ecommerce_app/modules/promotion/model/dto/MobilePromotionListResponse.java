package ecommerce_app.modules.promotion.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobilePromotionListResponse {
    
    private Long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private Integer buyQuantity;
    private Integer getQuantity;
    private Boolean active;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal minPurchaseAmount;
    
    // Display fields
    private String displayText; // e.g., "20% OFF", "Buy 2 Get 1 Free"
    private String status; // ACTIVE, UPCOMING, EXPIRED, INACTIVE
    private Boolean isCurrentlyValid;
    private Integer remainingUsage;
    
    // Product count
    private Integer applicableProductsCount;
}