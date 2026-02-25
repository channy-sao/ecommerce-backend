package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.WarrantyType;
import ecommerce_app.constant.enums.WarrantyUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductRequest", description = "Request object for product creation or update")
public class ProductRequest {

  @Schema(description = "Name of the product", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Description of the product",
      example = "Latest Apple smartphone with A17 chip")
  private String description;

  @Schema(description = "Price of the product", example = "1299.99")
  private BigDecimal price;

  // CHANGED: List<MultipartFile> instead of single MultipartFile
  @Schema(description = "Image files for the product", type = "array", format = "binary")
  private List<MultipartFile> images;

  // IDs of existing images to remove
  @Schema(description = "IDs of existing images to remove (for updates)", example = "[1, 2]")
  private List<Long> removeImageIds;

  // Full ordered list of existing image IDs (after removals) for reordering
  @Schema(
      description = "Ordered list of existing image IDs for reordering (after removals)",
      example = "[3, 4, 5]")
  private List<Long> imageOrder;

  @Schema(
      description = "ID of the brand the product belongs to (optional, null = no brand)",
      example = "1")
  private Long brandId; // optional, null = no brand

  @Schema(description = "ID of the category the product belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Indicates whether the product is featured", example = "true")
  private Boolean isFeature;

  @Schema(
      description = "List of product spec texts",
      example = "[\"Bluetooth 5.1\", \"IP67 waterproof\", \"30W RMS output\"]")
  private List<String> specs;

  @Builder.Default
  @Schema(
      description = "Type of warranty provided",
      example = "MANUFACTURER",
      defaultValue = "NONE",
      implementation = WarrantyType.class)
  private WarrantyType warrantyType = WarrantyType.NONE;

  @Schema(
      description = "Warranty duration value (required if warrantyType is not NONE)",
      example = "12",
      minimum = "1")
  private Integer warrantyDuration;

  @Schema(
      description = "Unit of warranty duration (required if warrantyType is not NONE)",
      example = "MONTHS",
      implementation = WarrantyUnit.class)
  private WarrantyUnit warrantyUnit;

  @Size(max = 500)
  @Schema(
      description = "Detailed warranty description (max 500 characters)",
      example = "Covers manufacturing defects only.",
      maxLength = 500)
  private String warrantyDescription;
}
