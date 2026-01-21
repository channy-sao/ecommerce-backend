package ecommerce_app.modules.stock.specification;

import ecommerce_app.modules.stock.model.dto.ProductImportFilterRequest;
import ecommerce_app.modules.stock.model.entity.ProductImport;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProductImportSpecification {

    public static Specification<ProductImport> filter(ProductImportFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with Product entity if needed
            Join<Object, Object> productJoin = null;

            // Filter by productId
            if (filter.getProductId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), filter.getProductId()));
            }

            // Filter by productName (requires join)
            if (StringUtils.hasText(filter.getProductName())) {
                if (productJoin == null) {
                    productJoin = root.join("product", JoinType.INNER);
                }
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(productJoin.get("name")),
                        "%" + filter.getProductName().toLowerCase() + "%"
                ));
            }

            // Filter by date range
            if (filter.getStartDate() != null) {
                LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startDateTime
                ));
            }

            if (filter.getEndDate() != null) {
                LocalDateTime endDateTime = filter.getEndDate().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), endDateTime
                ));
            }

            // Filter by supplier name
            if (StringUtils.hasText(filter.getSupplierName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("supplierName")),
                        "%" + filter.getSupplierName().toLowerCase() + "%"
                ));
            }

            // Filter by remark
            if (StringUtils.hasText(filter.getRemark())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("remark")),
                        "%" + filter.getRemark().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}