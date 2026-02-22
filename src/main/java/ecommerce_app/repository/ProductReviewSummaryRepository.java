package ecommerce_app.repository;

import ecommerce_app.entity.ProductReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewSummaryRepository extends JpaRepository<ProductReviewSummary, Long> {}
