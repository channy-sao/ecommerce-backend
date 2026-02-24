package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.WarrantyType;
import ecommerce_app.constant.enums.WarrantyUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Warranty response", description = "Warranty response for web and mobile")
public class WarrantyResponse {

  @Schema(
      description = "Type of warranty provided for the product",
      example = "MANUFACTURER",
      implementation = WarrantyType.class)
  private WarrantyType type;

  @Schema(description = "Warranty duration value", example = "12")
  private Integer duration;

  @Schema(
      description = "Unit of warranty duration",
      example = "MONTHS",
      implementation = WarrantyUnit.class)
  private WarrantyUnit unit;

  @Schema(
      description = "Detailed description of the warranty coverage",
      example = "Covers manufacturing defects only.")
  private String description;

  @Schema(
      description = "Computed display label for warranty (e.g. '12 Months Manufacturer Warranty')",
      example = "12 Months Manufacturer Warranty",
      accessMode = Schema.AccessMode.READ_ONLY)
  private String label;
}
