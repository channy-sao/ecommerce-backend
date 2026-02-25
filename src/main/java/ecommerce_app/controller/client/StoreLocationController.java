package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.StoreLocation;
import ecommerce_app.service.impl.StoreLocationService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/client/v1/store-location")
@Tag(name = "Store Location Controller", description = "API for fetching store location and contact information")
public class StoreLocationController {
  private final StoreLocationService storeLocationService;
  private final MessageSourceService messageSourceService;

  // ================= GET STORE LOCATION =================
  @GetMapping
  public ResponseEntity<BaseBodyResponse<StoreLocation>> getStoreLocation() {
    return BaseBodyResponse.success(
        storeLocationService.getStoreLocation(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
