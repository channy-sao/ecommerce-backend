package ecommerce_app.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO used to populate the RecentOrders.jrxml JasperReport.
 * Mapped from List<RecentOrderResponse> by ReportMapper.
 */
@Data
@Builder
public class RecentOrderReportDto {

    // ── Company ──────────────────────────────────────────────────────
    private String companyName;
    private String companyLogo;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;

    // ── Report meta ──────────────────────────────────────────────────
    private String fromDate;   // formatted "MMMM dd, yyyy"
    private String toDate;
    private String generatedAt;
    private Integer totalRecords;

    // ── Summary totals ───────────────────────────────────────────────
    private BigDecimal grandTotal;
    private Integer totalItems;

    // ── Line rows (each order = one row) ────────────────────────────
    private List<RecentOrderRowItem> rows;
}