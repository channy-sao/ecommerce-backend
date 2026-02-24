package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.BannerLinkType;
import ecommerce_app.constant.enums.BannerPosition;
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
public class BannerResponse {
  private Long id;
  private String title;
  private String description;
  private String image;
  private String linkUrl;
  private BannerLinkType linkType; // PRODUCT, CATEGORY, EXTERNAL, NONE
  private Long linkId;
  private BannerPosition position; // HOME_CAROUSEL, SIDEBAR, FOOTER, etc.
  private Integer displayOrder;
  private String backgroundColor;
  private Boolean active;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endDate;
}
