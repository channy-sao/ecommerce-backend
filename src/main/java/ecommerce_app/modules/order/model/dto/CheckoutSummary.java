package ecommerce_app.modules.order.model.dto;

import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSummary {
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal shippingCost;
    private BigDecimal shippingDiscount;
    private BigDecimal finalTotal;
    private boolean freeShipping;
    private Promotion appliedPromotion;
    private Map<Long, BigDecimal> itemDiscounts; // productId -> discount amount
    private String promotionError;
}
