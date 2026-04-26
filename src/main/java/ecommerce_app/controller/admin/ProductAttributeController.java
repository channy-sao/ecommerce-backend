package ecommerce_app.controller.admin;

import ecommerce_app.dto.request.ProductAttributeDefinitionRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ProductAttributeDefinitionResponse;
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
    description = "Manage attribute definitions (Color, Size…) and their values")
public class ProductAttributeController {

  private final ProductAttributeService attributeService;

  // ── Definitions ───────────────────────────────────────────────────────────

  @GetMapping
  @Operation(summary = "List all attribute definitions")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeDefinitionResponse>>>
      getDefinitions() {
    return BaseBodyResponse.success(attributeService.getAllDefinitions(), "OK");
  }

  @GetMapping("/paged")
  @Operation(summary = "List attribute definitions with pagination")
  public ResponseEntity<BaseBodyResponse<Page<ProductAttributeDefinitionResponse>>>
      getDefinitionsPaged(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "ASC") String direction) {

    Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    return BaseBodyResponse.success(attributeService.getDefinitions(pageable), "OK");
  }

  @GetMapping("/active")
  @Operation(summary = "List active attribute definitions only")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeDefinitionResponse>>>
      getActiveDefinitions() {
    return BaseBodyResponse.success(attributeService.getActiveDefinitions(), "OK");
  }

  @GetMapping("/{definitionId}")
  @Operation(summary = "Get attribute definition by ID")
  public ResponseEntity<BaseBodyResponse<ProductAttributeDefinitionResponse>> getDefinitionById(
      @PathVariable Long definitionId) {
    return BaseBodyResponse.success(attributeService.getDefinitionById(definitionId), "OK");
  }

  @PostMapping
  @Operation(summary = "Create an attribute definition (e.g. Color, Size)")
  public ResponseEntity<BaseBodyResponse<ProductAttributeDefinitionResponse>> createDefinition(
      @RequestBody @Valid ProductAttributeDefinitionRequest request) {
    return BaseBodyResponse.success(
        attributeService.createDefinition(request), "Attribute created");
  }

  @PutMapping("/{definitionId}")
  @Operation(summary = "Update an attribute definition")
  public ResponseEntity<BaseBodyResponse<ProductAttributeDefinitionResponse>> updateDefinition(
      @PathVariable Long definitionId,
      @RequestBody @Valid ProductAttributeDefinitionRequest request) {
    return BaseBodyResponse.success(
        attributeService.updateDefinition(definitionId, request), "Attribute updated");
  }

  @PatchMapping("/{definitionId}/deactivate")
  @Operation(summary = "Deactivate an attribute definition")
  public ResponseEntity<BaseBodyResponse<Void>> deactivateDefinition(
      @PathVariable Long definitionId) {
    attributeService.deactivateDefinition(definitionId);
    return BaseBodyResponse.success("Attribute deactivated");
  }

  @PatchMapping("/{definitionId}/activate")
  @Operation(summary = "Activate an attribute definition")
  public ResponseEntity<BaseBodyResponse<Void>> activateDefinition(
      @PathVariable Long definitionId) {
    attributeService.activateDefinition(definitionId);
    return BaseBodyResponse.success("Attribute activated");
  }

  @DeleteMapping("/{definitionId}")
  @Operation(summary = "Permanently delete an attribute definition")
  public ResponseEntity<BaseBodyResponse<Void>> deleteDefinition(@PathVariable Long definitionId) {
    attributeService.deleteDefinition(definitionId);
    return BaseBodyResponse.success("Attribute deleted");
  }

  // ── Values under a definition ─────────────────────────────────────────────

  @GetMapping("/{definitionId}/values")
  @Operation(summary = "List all values for an attribute definition")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeValueResponse>>> getValues(
      @PathVariable Long definitionId) {
    return BaseBodyResponse.success(attributeService.getValuesByDefinition(definitionId), "OK");
  }

  @GetMapping("/{definitionId}/values/active")
  @Operation(summary = "List active values for an attribute definition")
  public ResponseEntity<BaseBodyResponse<List<ProductAttributeValueResponse>>> getActiveValues(
      @PathVariable Long definitionId) {
    return BaseBodyResponse.success(
        attributeService.getActiveValuesByDefinition(definitionId), "OK");
  }

  @GetMapping("/values/{valueId}")
  @Operation(summary = "Get attribute value by ID")
  public ResponseEntity<BaseBodyResponse<ProductAttributeValueResponse>> getValueById(
      @PathVariable Long valueId) {
    return BaseBodyResponse.success(attributeService.getValueById(valueId), "OK");
  }

  @PostMapping("/{definitionId}/values")
  @Operation(summary = "Add a value to an attribute definition (e.g. Red, XL)")
  public ResponseEntity<BaseBodyResponse<ProductAttributeValueResponse>> createValue(
      @PathVariable Long definitionId, @RequestBody @Valid ProductAttributeValueRequest request) {
    return BaseBodyResponse.success(
        attributeService.createValue(definitionId, request), "Value created");
  }

  @PutMapping("/values/{valueId}")
  @Operation(summary = "Update an attribute value")
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

  @PostMapping("/{definitionId}/values/reorder")
  @Operation(summary = "Reorder attribute values")
  public ResponseEntity<BaseBodyResponse<Void>> reorderValues(
      @PathVariable Long definitionId, @RequestBody List<Long> orderedValueIds) {
    attributeService.reorderValues(definitionId, orderedValueIds);
    return BaseBodyResponse.success("Values reordered");
  }
}
