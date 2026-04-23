package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    @NotNull
    private Long variantId;

    @NotNull
    private StockMovementType movementType;  // IN, OUT, ADJUSTMENT, RETURN

    @NotNull @Min(1)
    private Integer quantity;

    private Long referenceId;
    private String referenceType;
    private String note;
}