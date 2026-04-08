package ecommerce_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReceiptItem {
  private String productName;
  private String productCode;
  private Integer quantity;
  private BigDecimal price;
  private BigDecimal total;
}
