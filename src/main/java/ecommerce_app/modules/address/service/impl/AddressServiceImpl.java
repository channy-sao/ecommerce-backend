package ecommerce_app.modules.address.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.AddressMapper;
import ecommerce_app.modules.address.model.dto.AddressRequest;
import ecommerce_app.modules.address.model.dto.AddressResponse;
import ecommerce_app.modules.address.model.entity.Address;
import ecommerce_app.modules.address.repository.AddressRepository;
import ecommerce_app.modules.address.service.AddressService;
import ecommerce_app.modules.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
  private final AddressRepository addressRepository;
  private final ModelMapper modelMapper;
  private final UserRepository userRepository;
  private final AddressMapper addressMapper;

  @Override
  public AddressResponse save(AddressRequest addressRequest) {
    log.info("Start saving address: {}", addressRequest);
    // check user for make relationship with address
    final var user =
        userRepository
            .findById(addressRequest.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", addressRequest.getUserId()));
    Address address = modelMapper.map(addressRequest, Address.class);
    address.setUser(user);

    // save address and map to response
    return addressMapper.toResponse(addressRepository.save(address));
  }

  @Override
  public AddressResponse getAddressById(Long id) {
    log.info("Start getting address: {}", id);
    var address = this.getById(id);
    return addressMapper.toResponse(address);
  }

  private Address getById(Long id) {
    return addressRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Address", id));
  }

  @Override
  public AddressResponse updateAddress(AddressRequest updateRequest, Long addressId) {
    log.info("Start updating address: {} by address id {}", updateRequest, addressId);
    // get existing address
    Address existing = this.getById(addressId);

    // modify existing
    modelMapper.map(updateRequest, existing);
    existing.setId(addressId);

    // save change and map to response
    return addressMapper.toResponse(addressRepository.save(existing));
  }

  @Override
  public void deleteAddressById(Long addressId) {
    log.info("Start deleting address: {}", addressId);
    addressRepository.deleteById(addressId);
  }

  @Override
  public List<AddressResponse> getAllAddresses() {
    log.info("Start getting all addresses");
    return addressRepository.findAll().stream().map(addressMapper::toResponse).toList();
  }

  @Override
  public List<AddressResponse> getAddressesByUserId(Long userId) {
    log.info("Start getting all addresses by user: {}", userId);
    return addressRepository.getAddressesByUserId(userId).stream()
        .map(addressMapper::toResponse)
        .toList();
  }
}
