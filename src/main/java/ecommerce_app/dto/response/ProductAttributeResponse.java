package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Response DTO for attribute definition */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeResponse {
  private Long id;
  private String name;
  private String displayName;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private AuditUserDto createdBy;
  private AuditUserDto updatedBy;
  private List<ProductAttributeValueResponse> values;
}
