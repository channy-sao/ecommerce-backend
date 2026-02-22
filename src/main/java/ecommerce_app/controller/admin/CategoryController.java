package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.BulkCategoryRequest;
import ecommerce_app.dto.request.CategoryRequest;
import ecommerce_app.dto.response.CategoryResponse;
import ecommerce_app.service.impl.CategoryExcelTemplateService;
import ecommerce_app.service.CategoryService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "For admin manage category")
public class CategoryController {
  private final CategoryService categoryService;
  private final CategoryExcelTemplateService categoryExcelTemplateService;
  private final MessageSourceService messageSourceService;

  @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
  @PostMapping
  public ResponseEntity<BaseBodyResponse<CategoryResponse>> createCategory(
      @RequestBody CategoryRequest categoryRequest) {
    return BaseBodyResponse.success(
        this.categoryService.saveCategory(categoryRequest),
        messageSourceService.getMessage(MessageKeyConstant.CATEGORY_MESSAGE_ADD_SUCCESS));
  }

  @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> deleteCategory(
      @PathVariable(value = "id") Long id) {
    this.categoryService.deleteCategory(id);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.CATEGORY_MESSAGE_DELETE_SUCCESS));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<CategoryResponse>> getById(
      @PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        categoryService.getCategoryById(id),
        messageSourceService.getMessage(MessageKeyConstant.CATEGORY_MESSAGE_UPDATE_SUCCESS));
  }

  @PreAuthorize("hasAuthority('CATEGORY_CREATE') or hasAuthority('CATEGORY_UPDATE')")
  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<CategoryResponse>> updateCategory(
      @RequestBody CategoryRequest categoryRequest, @PathVariable(value = "id") Long id) {
    return BaseBodyResponse.success(
        categoryService.updateCategory(categoryRequest, id),
        messageSourceService.getMessage(MessageKeyConstant.CATEGORY_TITLE_DETAIL));
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<BaseBodyResponse<CategoryResponse>> getByName(
      @PathVariable(value = "name") String name) {
    return BaseBodyResponse.success(
        categoryService.getCategoryByName(name),
        messageSourceService.getMessage(MessageKeyConstant.CATEGORY_TITLE_LIST));
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<CategoryResponse>>> filter(
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

  @PostMapping(path = "/import-from-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse<Void>> importCategoriesFromExcel(
      @RequestParam("file") MultipartFile file) {
    // Implementation for importing categories from an Excel file
    this.categoryService.importCategoriesFromExcel(file);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/bulk-insert")
  public ResponseEntity<BaseBodyResponse<Void>> bulkInsertCategories(
      @RequestBody @Valid BulkCategoryRequest bulkCategoryRequest) {
    // Implementation for importing categories from an Excel file
    this.categoryService.bulkInsertCategories(bulkCategoryRequest.getCategories());
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  /** Download Excel template for category import with dynamic sample data */
  @GetMapping("/import-template")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('CATEGORY_CREATE')")
  @Operation(
      summary = "Download category import template",
      description = "Generate and download Excel template with optional sample data")
  public ResponseEntity<Resource> getTemplateImportExcel(
      @Parameter(description = "Number of sample rows to generate (0-100)", name = "rows")
          @RequestParam(defaultValue = "5")
          int rows,
      @Parameter(description = "Include sample data generated by Faker", name = "includeSampleData")
          @RequestParam(defaultValue = "true")
          boolean includeSampleData) {

    // Validate rows parameter
    if (rows < 0 || rows > 100) {
      throw new BadRequestException(
          messageSourceService.getMessage(MessageKeyConstant.COMMON_VALIDATION_MIN_VALUE));
    }

    log.info(
        "Generating categories import template (rows: {}, includeSampleData: {})",
        rows,
        includeSampleData);

    byte[] excelData = categoryExcelTemplateService.generateCategoryExcelTemplate(rows);

    ByteArrayResource resource = new ByteArrayResource(excelData);

    // Generate filename with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename = String.format("categories_import_template_%s.xlsx", timestamp);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(excelData.length)
        .body(resource);
  }
}
