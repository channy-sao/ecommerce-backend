package ecommerce_app.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Single row in the Recent Orders report.
 * Field names MUST match <field> names in RecentOrders.jrxml.
 */
@Data
@Builder
public class RecentOrderRowItem {
    private Long      id;
    private String    orderNumber;
    private String    customer;
    private String    date;          // formatted string
    private BigDecimal amount;
    private String    status;
    private Integer   items;
}