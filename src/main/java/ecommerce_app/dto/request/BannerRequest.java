package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.BannerLinkType;
import ecommerce_app.constant.enums.BannerPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BannerRequest", description = "Request for creating or updating banner")
public class BannerRequest {

  @NotBlank(message = "Title is required")
  @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
  @Schema(description = "Banner title", example = "Summer Sale 50% OFF")
  private String title;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(description = "Banner description", example = "Get up to 50% off")
  private String description;

  @Schema(description = "Banner image (jpg, jpeg, png only, max 5MB)", type = "string", format = "binary")
  private MultipartFile image;

  @Size(max = 500, message = "Link URL must not exceed 500 characters")
  @Pattern(
          regexp = "^(https?://.*)?$",
          message = "Link URL must be a valid HTTP or HTTPS URL"
  )
  @Schema(description = "Link URL", example = "https://example.com/sale")
  private String linkUrl;

  @Schema(
          description = "Link type",
          example = "CATEGORY",
          allowableValues = {"PRODUCT", "CATEGORY", "EXTERNAL", "NONE"})
  private BannerLinkType linkType;

  @Positive(message = "Link ID must be greater than 0")
  @Schema(description = "Link ID (product or category ID)", example = "1")
  private Long linkId;

  @Builder.Default
  @Schema(description = "Is banner active", example = "true", defaultValue = "true")
  private Boolean isActive = true;

  @Min(value = 0, message = "Display order must be 0 or greater")
  @Builder.Default
  @Schema(description = "Display order", example = "1", defaultValue = "0")
  private Integer displayOrder = 0;

  @Future(message = "Start date must be in the future")
  @Schema(description = "Start date", example = "2026-02-11T00:00:00")
  private LocalDateTime startDate;

  @Future(message = "End date must be in the future")
  @Schema(description = "End date", example = "2026-03-11T23:59:59")
  private LocalDateTime endDate;

  @Builder.Default
  @Schema(description = "Position", example = "HOME_CAROUSEL")
  private BannerPosition position = BannerPosition.HOME_CAROUSEL;

  @Size(max = 20, message = "Background color must not exceed 20 characters")
  @Pattern(
          regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})?$",
          message = "Background color must be a valid hex color (e.g. #FF6B6B)"
  )
  @Schema(description = "Background color (hex)", example = "#FF6B6B")
  private String backgroundColor;
}