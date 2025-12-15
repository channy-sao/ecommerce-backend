package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.address.model.dto.AddressRequest;
import ecommerce_app.modules.address.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/addresses")
@Tag(name = "Address Controller", description = "Address Management")
@RequiredArgsConstructor
public class AddressController {
  private final AddressService addressService;

  @GetMapping("/{addressId}")
  public ResponseEntity<BaseBodyResponse> getAddressById(
      @PathVariable(value = "addressId") Long id) {
    return BaseBodyResponse.success(
        addressService.getAddressById(id), "Fetch address successfully");
  }

  @GetMapping("/list")
  public ResponseEntity<BaseBodyResponse> getAllAddresses() {
    return BaseBodyResponse.success(addressService.getAllAddresses(), "Fetch all addresses");
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<BaseBodyResponse> getAddressByUserId(
      @PathVariable(value = "userId") Long userId) {
    return BaseBodyResponse.success(
        addressService.getAddressesByUserId(userId), "Fetch address successfully");
  }

  @PostMapping("/users")
  public ResponseEntity<BaseBodyResponse> saveAddress(
      @RequestBody @Valid AddressRequest addressRequest) {
    return BaseBodyResponse.success(
        addressService.save(addressRequest), "Save address successfully");
  }

  @DeleteMapping("/{addressId}")
  public ResponseEntity<BaseBodyResponse> deleteAddress(@PathVariable("addressId") Long addressId) {
    this.addressService.deleteAddressById(addressId);
    return BaseBodyResponse.success(null, " Delete address successfully");
  }

  @PutMapping("/{addressId}")
  public ResponseEntity<BaseBodyResponse> updateAddress(
      @RequestBody @Valid AddressRequest updateRequest,
      @PathVariable(value = "addressId") Long addressId) {
    return BaseBodyResponse.success(
        addressService.updateAddress(updateRequest, addressId), "Update address successfully");
  }
}
