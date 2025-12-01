package ecommerce_app.modules.stock.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product Import response DTO containing import product and quantity information")
public class ProductImportResponse {

    @Schema(description = "Unique identifier of the import record", example = "1")
    private Long id;

    @Schema(description = "ID of the imported product", example = "101")
    private Long productId;

    @Schema(description = "Quantity of the product imported", example = "50")
    private int quantity;

    @Schema(description = "Unit price of the product at the time of import", example = "19.99")
    private BigDecimal unitPrice;

    @Schema(description = "Total amount of the product at the time of import", example = "19.99")
    private BigDecimal totalAmount;

    @Schema(description = "Timestamp of when the import record was created", example = "2024-07-25T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the import record", example = "2024-07-26T12:30:45")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the import record", example = "5")
    private Long createdBy;

    @Schema(description = "ID of the user who last updated the import record", example = "8")
    private Long updatedBy;
}
