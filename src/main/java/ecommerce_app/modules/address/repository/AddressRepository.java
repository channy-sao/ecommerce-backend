package ecommerce_app.modules.address.repository;

import ecommerce_app.modules.address.model.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> getAddressesByUserId (long userId);

  Optional<Address> findByIdAndUserId(long addressId, long userId);
}
