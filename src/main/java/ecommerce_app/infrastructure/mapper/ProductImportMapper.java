package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import ecommerce_app.modules.stock.model.entity.ProductImport;
import ecommerce_app.util.AuditUserResolver;
import ecommerce_app.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductImportMapper {
  private final ModelMapper modelMapper;
  private final AuditUserResolver auditUserResolver;

  public ProductImportResponse toProductImportResponse(ProductImport productImport) {
    final var importResponse = modelMapper.map(productImport, ProductImportResponse.class);
    importResponse.setProduct(ProductMapper.toProductResponse(productImport.getProduct()));
    importResponse.setCreatedBy(auditUserResolver.resolve(productImport.getCreatedBy()));
    importResponse.setUpdatedBy(auditUserResolver.resolve(productImport.getUpdatedBy()));
    return importResponse;
  }
}
