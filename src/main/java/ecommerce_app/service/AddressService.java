package ecommerce_app.service;

import ecommerce_app.dto.request.AddressRequest;
import ecommerce_app.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
  AddressResponse save(AddressRequest addressRequest);

  AddressResponse getAddressById(Long id);

  AddressResponse updateAddress(AddressRequest updateAddressRequest, Long id);

  void deleteAddressById(Long addressId);

  List<AddressResponse> getAllAddresses();

  List<AddressResponse> getAddressesByUserId(Long userId);
}
