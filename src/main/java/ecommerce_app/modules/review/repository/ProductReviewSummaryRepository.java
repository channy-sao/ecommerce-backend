package ecommerce_app.modules.review.repository;

import ecommerce_app.modules.review.model.entity.ProductReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewSummaryRepository extends JpaRepository<ProductReviewSummary, Long> {}
