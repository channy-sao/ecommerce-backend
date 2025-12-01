package ecommerce_app.modules.category.controller;

import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.category.model.dto.CategoryRequest;
import ecommerce_app.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "For admin manage category")
public class CategoryController {
  private final CategoryService categoryService;

  @PostMapping
  public ResponseEntity<BaseBodyResponse> createCategory(
      @RequestBody CategoryRequest categoryRequest) {
    return BaseBodyResponse.success(
        this.categoryService.saveCategory(categoryRequest),
        ResponseMessageConstant.CREATE_SUCCESSFULLY);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> deleteCategory(@PathVariable(value = "id") Long id) {
    this.categoryService.deleteCategory(id);
    return BaseBodyResponse.success(null, ResponseMessageConstant.DELETE_SUCCESSFULLY);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getById(@PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        categoryService.getCategoryById(id), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateCategory(
      @RequestBody CategoryRequest categoryRequest, @PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        categoryService.updateCategory(categoryRequest, id),
        ResponseMessageConstant.UPDATE_SUCCESSFULLY);
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<BaseBodyResponse> getByName(@PathVariable(value = "name") String name) {
    return BaseBodyResponse.success(
        categoryService.getCategoryByName(name), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> filter(
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection,
      @RequestParam(value = "filter", required = false) String filter) {
    return BaseBodyResponse.pageSuccess(
        categoryService.filter(isPaged, page, pageSize, sortBy, sortDirection, filter),
        ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
  }
}
