package ecommerce_app.dto.response;

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