package ecommerce_app.controller.admin;

import ecommerce_app.dto.request.ProductAttributeRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ProductAttributeResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import ecommerce_app.service.ProductAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/attributes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
@Tag(
    name = "Product Attributes",
    description = "Manage product attribute (Color, Size…) and their values")
public class ProductAttributeController {

  private final ProductAttributeService attributeService;

  // ── Definitions ───────────────────────────────────────────────────────────

  @GetMapping
  @Operation(summary = "List all product attributes")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeResponse>>>
      getProductAttributes() {
    return BaseBodyResponse.success(attributeService.getAllProductAttributes(), "OK");
  }

  @GetMapping("/paged")
  @Operation(summary = "List product attribute with pagination")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeResponse>>>
      getProductAttributePaged(
          @RequestParam(defaultValue = "1") int page,
          @RequestParam(defaultValue = "10") int pageSize,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "ASC") String direction) {

    Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
    Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by(sortDirection, sortBy));
    return BaseBodyResponse.pageSuccess(attributeService.getProductAttributes(pageable), "OK");
  }

  @GetMapping("/active")
  @Operation(summary = "List active product attributes only")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeResponse>>>
      getActiveProductAttributes() {
    return BaseBodyResponse.success(attributeService.getActiveProductAttributes(), "OK");
  }

  @GetMapping("/{attributeId}")
  @Operation(summary = "Get product attribute by ID")
  public ResponseEntity<BaseBodyResponse<ProductAttributeResponse>> getProductAttributeById(
      @PathVariable Long attributeId) {
    return BaseBodyResponse.success(attributeService.getProductAttributeById(attributeId), "OK");
  }

  @PostMapping
  @Operation(summary = "Create an product attribute (e.g. Color, Size)")
  public ResponseEntity<BaseBodyResponse<ProductAttributeResponse>> createProductAttribute(
      @RequestBody @Valid ProductAttributeRequest request) {
    return BaseBodyResponse.success(
        attributeService.createProductAttribute(request), "Product Attribute created");
  }

  @PutMapping("/{attributeId}")
  @Operation(summary = "Update an product attribute definition")
  public ResponseEntity<BaseBodyResponse<ProductAttributeResponse>> updateProductAttribute(
      @PathVariable Long attributeId,
      @RequestBody @Valid ProductAttributeRequest request) {
    return BaseBodyResponse.success(
        attributeService.updateProductAttribute(attributeId, request), "Product Attribute updated");
  }

  @PatchMapping("/{attributeId}/deactivate")
  @Operation(summary = "Deactivate a product attribute")
  public ResponseEntity<BaseBodyResponse<Void>> deactivateProductAttribute(
      @PathVariable Long attributeId) {
    attributeService.deactivateProductAttribute(attributeId);
    return BaseBodyResponse.success("Product Attribute toggle status changed");
  }

  @PatchMapping("/{attributeId}/activate")
  @Operation(summary = "Activate a product attribute")
  public ResponseEntity<BaseBodyResponse<Void>> activateProductAttribute(
      @PathVariable Long attributeId) {
    attributeService.activateProductAttribute(attributeId);
    return BaseBodyResponse.success("Product Attribute activated");
  }

  @DeleteMapping("/{attributeId}")
  @Operation(summary = "Permanently delete a product attribute")
  public ResponseEntity<BaseBodyResponse<Void>> deleteDefinition(@PathVariable Long attributeId) {
    attributeService.deleteDefinition(attributeId);
    return BaseBodyResponse.success("Product Attribute deleted");
  }

  // ── Values under a definition ─────────────────────────────────────────────

  @GetMapping("/{attributeId}/values")
  @Operation(summary = "List all values for a product attribute")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeValueResponse>>> getValues(
      @PathVariable Long attributeId) {
    return BaseBodyResponse.success(attributeService.getValuesByProductAttribute(attributeId), "OK");
  }

  @GetMapping("/{attributeId}/values/active")
  @Operation(summary = "List active values for an product attribute")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeValueResponse>>> getActiveValues(
      @PathVariable Long attributeId) {
    return BaseBodyResponse.success(
        attributeService.getActiveValuesByProductAttribute(attributeId), "OK");
  }

  @GetMapping("/values/{valueId}")
  @Operation(summary = "Get product attribute value by ID")
  public ResponseEntity<BaseBodyResponse<ProductAttributeValueResponse>> getValueById(
      @PathVariable Long valueId) {
    return BaseBodyResponse.success(attributeService.getValueById(valueId), "OK");
  }

  @PostMapping("/{attributeId}/values")
  @Operation(summary = "Add a value to a product attribute (e.g. Red, XL)")
  public ResponseEntity<BaseBodyResponse<ProductAttributeValueResponse>> createValue(
      @PathVariable Long attributeId, @RequestBody @Valid ProductAttributeValueRequest request) {
    return BaseBodyResponse.success(
        attributeService.createValue(attributeId, request), "Value created");
  }

  @PutMapping("/values/{valueId}")
  @Operation(summary = "Update a product attribute value")
  public ResponseEntity<BaseBodyResponse<ProductAttributeValueResponse>> updateValue(
      @PathVariable Long valueId, @RequestBody @Valid ProductAttributeValueRequest request) {
    return BaseBodyResponse.success(
        attributeService.updateValue(valueId, request), "Value updated");
  }

  @PatchMapping("/values/{valueId}/deactivate")
  @Operation(summary = "Deactivate an attribute value (soft delete)")
  public ResponseEntity<BaseBodyResponse<Void>> deactivateValue(@PathVariable Long valueId) {
    attributeService.deactivateValue(valueId);
    return BaseBodyResponse.success("Value deactivated");
  }

  @PatchMapping("/values/{valueId}/activate")
  @Operation(summary = "Activate an attribute value")
  public ResponseEntity<BaseBodyResponse<Void>> activateValue(@PathVariable Long valueId) {
    attributeService.activateValue(valueId);
    return BaseBodyResponse.success("Value activated");
  }

  @DeleteMapping("/values/{valueId}")
  @Operation(summary = "Permanently delete an attribute value")
  public ResponseEntity<BaseBodyResponse<Void>> deleteValue(@PathVariable Long valueId) {
    attributeService.deleteValue(valueId);
    return BaseBodyResponse.success("Value deleted");
  }

  @PostMapping("/{attributeId}/values/reorder")
  @Operation(summary = "Reorder attribute values")
  public ResponseEntity<BaseBodyResponse<Void>> reorderValues(
      @PathVariable Long attributeId, @RequestBody List<Long> orderedValueIds) {
    attributeService.reorderValues(attributeId, orderedValueIds);
    return BaseBodyResponse.success("Values reordered");
  }
}
