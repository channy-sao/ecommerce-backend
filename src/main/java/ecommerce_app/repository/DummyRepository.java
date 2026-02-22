package ecommerce_app.repository;

import ecommerce_app.entity.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<Dummy, Long> {
  boolean existsByNameAndAgainFalse(String name);
  Dummy findByName(String name);
}
