package ecommerce_app.modules.stock.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;

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

  @NotNull
  @Schema(description = "Supplier name that we import", example = "TechGadgets Inc.")
  private String supplierName;

  @Schema(description = "Supplier address that we import", example = "Warehouse A.")
  @Nullable
  private String supplierAddress;

  @Schema(description = "Supplier phone that we import", example = "+855 2354763")
  @Nullable
  private String supplierPhone;

  @Schema(description = "Remark when import product", example = "Import product remark")
  private String remark;
}
