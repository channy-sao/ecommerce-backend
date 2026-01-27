package ecommerce_app.api.admin;

import ecommerce_app.constant.enums.ExportFormat;
import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.product.model.dto.ProductRequest;
import ecommerce_app.modules.product.service.ProductExcelTemplateService;
import ecommerce_app.modules.product.service.ProductService;
import ecommerce_app.modules.reports.ProductReportService;
import ecommerce_app.util.ExcelTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "For admin manage product")
public class ProductController {
  private final ProductService productService;
  private final ProductExcelTemplateService productExcelTemplateService;
  private final ProductReportService productReportService;

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

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

  @PostMapping(value = "/import-from-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse> importFromExcel(
      @RequestParam("file") MultipartFile file) {
    return BaseBodyResponse.success(
        productService.importProductFromExcel(file), "Imported product from Excel File");
  }

  /** Download Excel template for product import with dynamic sample data */
  @GetMapping("/import-template")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @Operation(
      summary = "Download product import template",
      description = "Generate and download Excel template with optional sample data")
  public ResponseEntity<Resource> getTemplateImportExcel(
      @Parameter(description = "Number of sample rows to generate (0-1000)", name = "rows")
          @RequestParam(defaultValue = "5")
          int rows,
      @Parameter(description = "Include sample data generated by Faker", name = "includeSampleData")
          @RequestParam(defaultValue = "true")
          boolean includeSampleData) {

    // Validate rows parameter
    if (rows < 0 || rows > 1000) {
      throw new BadRequestException("'rows' parameter must be between 0 and 1000");
    }

    log.info(
        "Generating product import template (rows: {}, includeSampleData: {})",
        rows,
        includeSampleData);

    byte[] excelData = productExcelTemplateService.generateTemplate(rows);

    ByteArrayResource resource = new ByteArrayResource(excelData);

    // Generate filename with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename = String.format("product_import_template_%s.xlsx", timestamp);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(excelData.length)
        .body(resource);
  }

  /** Download Product Report */
  @GetMapping("/export-report")
  @Operation(
      summary = "Download product report",
      description = "Download product report as Excel and PDF")
  public ResponseEntity<Resource> exportProductReport(
      @RequestParam(defaultValue = "PDF", name = "format") ExportFormat format) {

    // Report generation
    byte[] reportData = productReportService.export(format);
    ByteArrayResource resource = new ByteArrayResource(reportData);

    // Generate filename with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename =
        String.format(
            "%s_%s.%s",
            productReportService.getBaseFilename(),
            timestamp,
            format == ExportFormat.PDF ? "pdf" : "xlsx");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType(format.getContentType()))
        .contentLength(reportData.length)
        .body(resource);
  }
}
