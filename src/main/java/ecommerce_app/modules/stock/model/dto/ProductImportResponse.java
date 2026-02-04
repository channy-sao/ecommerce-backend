package ecommerce_app.modules.stock.model.dto;

import ecommerce_app.infrastructure.model.response.AuditUserDto;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "Product Import response DTO containing import product and quantity information")
public class ProductImportResponse {

  @Schema(description = "Unique identifier of the import record", example = "1")
  private Long id;

  @Schema(description = "Representation of product")
  private ProductResponse product;

  @Schema(description = "Quantity of the product imported", example = "50")
  private int quantity;

  @Schema(description = "Unit price of the product at the time of import", example = "19.99")
  private BigDecimal unitPrice;

  @Schema(description = "Total amount of the product at the time of import", example = "19.99")
  private BigDecimal totalAmount;

  @Schema(description = "Supplier name that we import", example = "TechGadgets Inc.")
  private String supplierName;

  @Schema(description = "Supplier address that we import", example = "Warehouse A.")
  private String supplierAddress;

  @Schema(description = "Supplier phone that we import", example = "+855 2354763")
  private String supplierPhone;

  @Schema(description = "Remark when import product to stock", example = "Test remark")
  private String remark;

  @Schema(
      description = "Timestamp of when the import record was created",
      example = "2024-07-25T10:15:30")
  private Instant createdAt;

  @Schema(
      description = "Timestamp of the last update to the import record",
      example = "2024-07-26T12:30:45")
  private Instant updatedAt;

  @Schema(description = "User who created the import record")
  private AuditUserDto createdBy;

  @Schema(description = "User who last updated the import record")
  private AuditUserDto updatedBy;
}
