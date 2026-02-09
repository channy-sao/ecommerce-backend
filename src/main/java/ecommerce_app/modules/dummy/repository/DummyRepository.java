package ecommerce_app.modules.dummy.repository;

import ecommerce_app.modules.dummy.model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<Dummy, Long> {
  boolean existsByNameAndAgainFalse(String name);
  Dummy findByName(String name);
}
