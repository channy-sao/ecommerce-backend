// ecommerce_app/modules/promotion/model/dto/PromotionResponse.java
package ecommerce_app.modules.promotion.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionResponse {
    private Long id;
    private String code;
    private String name;
    private PromotionType discountType;
    private BigDecimal discountValue;
    private Integer buyQuantity;
    private Integer getQuantity;
    private Boolean active;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer maxUsage;
    private BigDecimal minPurchaseAmount;
    private Integer currentUsage;
    private List<ProductResponse> products;
    private Instant createdAt;
    private Instant updatedAt;
}