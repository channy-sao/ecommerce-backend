package ecommerce_app.modules.product.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReportDto {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private String categoryName;
  private Boolean isFeature;
  private Instant createdAt;
  private String createdByFullName;
}
