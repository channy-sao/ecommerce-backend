package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.request.ProductSpecRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ProductSpecResponse;
import ecommerce_app.service.impl.ProductSpecService;
import ecommerce_app.util.MessageSourceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/products/{productId}/specs")
@RequiredArgsConstructor
public class ProductSpecController {

  private final ProductSpecService productSpecService;
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<ProductSpecResponse>>> getSpecs(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        productSpecService.getSpecs(productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  // Replaces all specs at once (send the full ordered list)
  @PutMapping
  public ResponseEntity<BaseBodyResponse<List<ProductSpecResponse>>> replaceSpecs(
      @PathVariable Long productId, @RequestBody @Valid List<ProductSpecRequest> requests) {
    return BaseBodyResponse.success(
        productSpecService.replaceSpecs(productId, requests),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @DeleteMapping("/{specId}")
  public ResponseEntity<BaseBodyResponse<Void>> deleteSpec(
      @PathVariable Long productId, @PathVariable Long specId) {
    productSpecService.deleteSpec(specId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
