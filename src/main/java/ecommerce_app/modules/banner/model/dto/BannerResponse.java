package ecommerce_app.modules.banner.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
  private String imageUrl;
  private String linkUrl;
  private String linkType; // PRODUCT, CATEGORY, EXTERNAL, NONE
  private Long linkId;
  private Integer displayOrder;
  private String backgroundColor;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endDate;
}
