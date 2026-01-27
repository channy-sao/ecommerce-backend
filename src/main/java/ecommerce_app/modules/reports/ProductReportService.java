package ecommerce_app.modules.reports;

import ecommerce_app.common.service.JasperReportService;
import ecommerce_app.constant.enums.ExportFormat;
import ecommerce_app.modules.product.model.dto.ProductReportDto;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReportService implements ReportService<Product> {

  private final ProductRepository productRepository;
  private final JasperReportService jasperReportService;

  @Transactional(readOnly = true)
  @Override
  public byte[] export(ExportFormat format) {
    log.info("Exporting products as {}", format);

    List<Product> products = productRepository.findAll();
    List<ProductReportDto> reportData = products.stream().map(this::toReportDto).toList();

    return jasperReportService.exportReport(reportData, getReportTemplateName(), format, "Products");
  }

  @Override
  public String getReportTemplateName() {
    return "product-report";
  }

  @Override
  public String getBaseFilename() {
    return "products_export";
  }

  private ProductReportDto toReportDto(Product product) {
    return ProductReportDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .categoryName(product.getCategory() != null ? product.getCategory().getName() : "N/A")
        .isFeature(product.getIsFeature())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
