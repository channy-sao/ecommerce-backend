package ecommerce_app.modules.category.repository;

import ecommerce_app.modules.category.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository
    extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
  Category findByName(String name);

  // Get all active categories ordered by name
  @Query("SELECT c FROM Category c ORDER BY c.name ASC")
  List<Category> findAllOrderByName();

  @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC")
  List<Category> findAllOrderedByDisplayOrder();

  //  Get top categories ordered by name
  @Query("SELECT c FROM Category c ORDER BY c.name ASC")
  List<Category> findTopCategoriesOrderByName();

  @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC")
  List<Category> findTopCategories();
}
