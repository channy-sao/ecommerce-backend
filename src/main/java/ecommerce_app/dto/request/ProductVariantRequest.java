package ecommerce_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ProductVariantRequest {
    @NotBlank
    private String sku;

    private BigDecimal price;           // null = use product price

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @Min(0)
    private Integer lowStockThreshold;

    @NotEmpty
    private List<Long> attributeValueIds;  // [colorId, sizeId]
}