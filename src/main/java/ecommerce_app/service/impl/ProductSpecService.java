package ecommerce_app.service.impl;

import ecommerce_app.dto.request.ProductSpecRequest;
import ecommerce_app.dto.response.ProductSpecResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductSpec;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.ProductSpecRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSpecService {

  private final ProductSpecRepository productSpecRepository;
  private final ProductRepository productRepository;

  public List<ProductSpecResponse> getSpecs(Long productId) {
    return productSpecRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public List<ProductSpecResponse> replaceSpecs(Long productId, List<ProductSpecRequest> requests) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

    productSpecRepository.deleteAllByProductId(productId);

    List<ProductSpec> specs = new ArrayList<>();
    for (int i = 0; i < requests.size(); i++) {
      ProductSpec spec = new ProductSpec();
      spec.setProduct(product);
      spec.setSpecText(requests.get(i).getSpecText());
      spec.setSortOrder(
          requests.get(i).getSortOrder() != null ? requests.get(i).getSortOrder() : i);
      specs.add(spec);
    }

    return productSpecRepository.saveAll(specs).stream().map(this::toResponse).toList();
  }

  @Transactional
  public void deleteSpec(Long specId) {
    if (!productSpecRepository.existsById(specId)) {
      throw new EntityNotFoundException("Spec not found");
    }
    productSpecRepository.deleteById(specId);
  }

  private ProductSpecResponse toResponse(ProductSpec spec) {
    ProductSpecResponse res = new ProductSpecResponse();
    res.setId(spec.getId());
    res.setSpecText(spec.getSpecText());
    res.setSortOrder(spec.getSortOrder());
    return res;
  }
}
