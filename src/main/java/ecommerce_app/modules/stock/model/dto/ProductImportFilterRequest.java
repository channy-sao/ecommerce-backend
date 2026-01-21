package ecommerce_app.modules.stock.model.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Filter request for querying product import records")
public class ProductImportFilterRequest {

    @Schema(description = "Product ID", example = "1001")
    private Long productId;

    @Schema(description = "Product name", example = "iPhone 15")
    private String productName;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Filter start date (inclusive)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "Filter end date (inclusive)", example = "2024-12-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "Supplier name", example = "Apple Inc.")
    private String supplierName;

    @Schema(description = "Remark or note for filtering", example = "Urgent imports")
    private String remark;

    // Pagination

    @Schema(description = "Enable pagination", example = "true")
    private boolean isPaged = true;

    @Schema(description = "Page number (0-based)", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "Number of records per page", example = "20", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "Field to sort by", example = "createdAt", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(
            description = "Sort direction",
            example = "DESC",
            allowableValues = {"ASC", "DESC"},
            defaultValue = "DESC"
    )
    private Sort.Direction sortDirection = Sort.Direction.DESC;
}
