package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SimpleProductResponse", description = "Response object for product creation or update")
public class SimpleProductResponse {

    @Schema(description = "Id of the product", example = "1")
    private Long id;

    @Schema(description = "UUID of the product")
    private UUID uuid;

    @Schema(description = "Name of the product", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Price of the product", example = "1299.99")
    private BigDecimal price;

    @Schema(description = "Primary image URL (first by sort order)")
    private String primaryImage;

    @Schema(description = "Category name of the product belongs to", example = "Electronic")
    private String categoryName;

    @Schema(description = "Brand of the product")
    private SimpleBrandResponse brand;

    @Schema(
            name = "stockStatus",
            description = "Stock status (e.g., In Stock, Out of Stock, Limited Stock)",
            example = "In Stock")
    private StockStatus stockStatus;

}
