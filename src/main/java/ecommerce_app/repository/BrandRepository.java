package ecommerce_app.repository;

import ecommerce_app.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository
    extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {

  List<Brand> findAllByIsActiveTrueOrderByDisplayOrderAsc();

  Optional<Brand> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);
}
