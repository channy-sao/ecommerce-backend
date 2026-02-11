package ecommerce_app.modules.product.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private LocalDateTime createdAt;
  private String createdByFullName;
}
