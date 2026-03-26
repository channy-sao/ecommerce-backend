package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order status distribution entry for dashboard charts")
public class StatusDistributionResponse {

    @Schema(description = "Order status label", example = "PENDING")
    private String status;

    @Schema(description = "Number of orders with this status", example = "42")
    private Long count;

    @Schema(description = "Percentage of total orders with this status", example = "8.4")
    private Double percentage;
}