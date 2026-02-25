package ecommerce_app.repository;

import ecommerce_app.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository
    extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {

  List<Brand> findAllByIsActiveTrueOrderByDisplayOrderAsc();

  Optional<Brand> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);

  Page<Brand> findByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

  @Query(
      "SELECT b FROM Brand b WHERE "
          + "b.isActive = true AND "
          + "LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')) "
          + "ORDER BY b.displayOrder ASC")
  Page<Brand> findActiveBrandsByNameContaining(@Param("name") String name, Pageable pageable);

  @Query(
          "SELECT b FROM Brand b WHERE "
                  + "LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')) "
                  + "ORDER BY b.displayOrder ASC")
  Page<Brand> findBrandsByNameContaining(@Param("name") String name, Pageable pageable);
}
