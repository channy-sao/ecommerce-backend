package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.BrandResponse;
import ecommerce_app.service.BrandService;

import ecommerce_app.util.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/v1/brands")
@RequiredArgsConstructor
public class MobileBrandController {

  private final BrandService brandService;
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<BrandResponse>>> getActiveBrands() {
    return BaseBodyResponse.success(
        brandService.getActiveBrands(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<BrandResponse>> getBrandById(@PathVariable Long id) {
    return BaseBodyResponse.success(
        brandService.getById(id),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
