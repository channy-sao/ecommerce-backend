package ecommerce_app.modules.stock.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Product import request")
public class ProductImportRequest {
  @Schema(description = "Product Id", example = "1")
  @NotNull(message = "Product Id is Required")
  private Long productId;

  @Positive(message = "quantity must be > 0")
  @Schema(description = "Initial stock quantity", example = "100")
  private Integer quantity;

  @NotNull(message = "unit price is require")
  @Positive(message = "quantity must be > 0")
  @Schema(description = "Unit price of product", example = "100.00")
  private BigDecimal unitPrice;

  @Schema(description = "Remark when import product", example = "Import product remark")
  private String remark;
}
