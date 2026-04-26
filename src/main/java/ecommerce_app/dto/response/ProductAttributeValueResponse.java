package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Response DTO for attribute value */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueResponse {
  private Long id;
  private Long attributeDefinitionId;
  private String attributeDefinitionName;
  private String value;
  private Integer displayOrder;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
