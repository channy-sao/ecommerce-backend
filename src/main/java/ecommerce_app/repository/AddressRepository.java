package ecommerce_app.repository;

import ecommerce_app.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> getAddressesByUserId (long userId);

  Optional<Address> findByIdAndUserId(long addressId, long userId);

  Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}
