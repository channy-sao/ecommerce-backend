package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.BannerLinkType;
import ecommerce_app.constant.enums.BannerPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Banner response payload")
public class BannerResponse {

  @Schema(description = "Unique banner ID", example = "1")
  private Long id;

  @Schema(description = "Banner title", example = "Summer Sale")
  private String title;

  @Schema(description = "Banner description", example = "Up to 50% off on selected items")
  private String description;

  @Schema(
      description = "Image path or URL of the banner",
      example = "/uploads/banners/summer-sale.jpg")
  private String image;

  @Schema(description = "URL the banner links to", example = "https://example.com/sale")
  private String linkUrl;

  @Schema(description = "Type of link the banner points to", example = "PRODUCT")
  private BannerLinkType linkType; // PRODUCT, CATEGORY, EXTERNAL, NONE

  @Schema(description = "ID of the linked entity (product or category)", example = "42")
  private Long linkId;

  @Schema(description = "Position where the banner is displayed", example = "HOME_CAROUSEL")
  private BannerPosition position; // HOME_CAROUSEL, SIDEBAR, FOOTER, etc.

  @Schema(description = "Display order priority (lower = higher priority)", example = "1")
  private Integer displayOrder;

  @Schema(description = "Background color in hex or CSS format", example = "#FF5733")
  private String backgroundColor;

  @Schema(description = "Whether the banner is currently active", example = "true")
  private Boolean active;

  @Schema(description = "Date and time the banner becomes active", example = "2024-06-01T00:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startDate;

  @Schema(description = "Date and time the banner expires", example = "2024-08-31T23:59:59")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endDate;
}
