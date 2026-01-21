package ecommerce_app.modules.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionResponse {
    private String status;
    private Long count;
    private Double percentage;
}