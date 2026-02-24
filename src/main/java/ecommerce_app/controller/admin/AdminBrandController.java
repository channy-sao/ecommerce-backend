package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.request.BrandRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.BrandResponse;
import ecommerce_app.service.BrandService;
import ecommerce_app.util.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/brands")
@RequiredArgsConstructor
public class AdminBrandController {

  private final BrandService brandService;
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<BrandResponse>>> getAll() {
    return BaseBodyResponse.success(
        brandService.getAllBrands(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<BrandResponse>> getById(@PathVariable Long id) {
    return BaseBodyResponse.success(
        brandService.getById(id),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse<BrandResponse>> create(
      @ModelAttribute BrandRequest request) {
    return BaseBodyResponse.success(
        brandService.createBrand(request),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse<BrandResponse>> update(
      @PathVariable Long id, @ModelAttribute BrandRequest request) {
    return BaseBodyResponse.success(
        brandService.updateBrand(id, request),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PatchMapping("/{id}/toggle-status")
  public ResponseEntity<BaseBodyResponse<Void>> toggleStatus(@PathVariable Long id) {
    brandService.toggleStatus(id);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    brandService.deleteBrand(id);
    return BaseBodyResponse.success(
        null, messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
