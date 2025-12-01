package ecommerce_app.modules.product.controller;

import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.product.model.dto.ProductRequest;
import ecommerce_app.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "For admin manage product")
public class ProductController {
  private final ProductService productService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse> createProduct(
      @ModelAttribute ProductRequest productRequest) {
    return BaseBodyResponse.success(
        this.productService.saveProduct(productRequest),
        ResponseMessageConstant.CREATE_SUCCESSFULLY);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> deleteProduct(@PathVariable(value = "id") Long id) {
    this.productService.deleteProduct(id);
    return BaseBodyResponse.success(null, ResponseMessageConstant.DELETE_SUCCESSFULLY);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getById(@PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        productService.getProductById(id), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  @PutMapping(value = "/{id}", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse> updateCategory(
      @ModelAttribute ProductRequest productRequest, @PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        productService.updateProduct(productRequest, id),
        ResponseMessageConstant.UPDATE_SUCCESSFULLY);
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getProducts() {
    return BaseBodyResponse.success(
        this.productService.getProducts(), ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
  }

  @GetMapping("/filter")
  public ResponseEntity<BaseBodyResponse> filter(
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection,
      @RequestParam(value = "categoryId", required = false) Long categoryId,
      @RequestParam(value = "filter", required = false) String filter) {
    return BaseBodyResponse.pageSuccess(
        productService.filter(isPaged, page, pageSize, sortBy, sortDirection, categoryId, filter),
        ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
  }
}
