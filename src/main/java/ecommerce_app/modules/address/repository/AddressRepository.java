package ecommerce_app.modules.address.repository;

import ecommerce_app.modules.address.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> getAddressesByUserId(long userId);
}
