package ecommerce_app.modules.banner.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BannerRequest", description = "Request for creating or updating banner")
public class BannerRequest {

  @NotBlank(message = "Title is required")
  @Size(max = 200)
  @Schema(description = "Banner title", example = "Summer Sale 50% OFF")
  private String title;

  @Size(max = 500)
  @Schema(description = "Banner description", example = "Get up to 50% off")
  private String description;

  @Schema(description = "Banner image")
  private MultipartFile image;

  @Size(max = 500)
  @Schema(description = "Link URL", example = "https://example.com/sale")
  private String linkUrl;

  @Schema(
      description = "Link type",
      example = "CATEGORY",
      allowableValues = {"PRODUCT", "CATEGORY", "EXTERNAL", "NONE"})
  private String linkType;

  @Schema(description = "Link ID (product or category ID)", example = "1")
  private Long linkId;

  @Schema(description = "Is banner active", example = "true")
  @Builder.Default
  private Boolean isActive = true;

  @Schema(description = "Display order", example = "1")
  @Builder.Default
  private Integer displayOrder = 0;

  @Schema(description = "Start date", example = "2026-02-11T00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "End date", example = "2026-03-11T23:59:59")
  private LocalDateTime endDate;

  @Schema(description = "Position", example = "HOME_CAROUSEL")
  @Builder.Default
  private String position = "HOME_CAROUSEL";

  @Size(max = 20)
  @Schema(description = "Background color (hex)", example = "#FF6B6B")
  private String backgroundColor;
}
