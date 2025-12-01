package ecommerce_app.modules.address.service;

import ecommerce_app.modules.address.model.dto.AddressRequest;
import ecommerce_app.modules.address.model.dto.AddressResponse;
import java.util.List;

public interface AddressService {
  AddressResponse save(AddressRequest addressRequest);

  AddressResponse getAddressById(Long id);

  AddressResponse updateAddress(AddressRequest updateAddressRequest, Long id);

  void deleteAddressById(Long addressId);

  List<AddressResponse> getAllAddresses();

  List<AddressResponse> getAddressesByUserId(Long userId);
}
