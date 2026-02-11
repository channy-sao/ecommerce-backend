package ecommerce_app.modules.category.service.impl;

import ecommerce_app.infrastructure.mapper.CategoryMapper;
import ecommerce_app.modules.category.model.dto.SimpleCategoryResponse;
import ecommerce_app.modules.category.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MobileCategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  /**
   * Get simple categories for home screen Returns categories ordered by display_order, limited to
   * 12 items
   */
  @Transactional(readOnly = true)
  public List<SimpleCategoryResponse> getSimpleCategories() {
    return categoryRepository.findAllOrderedByDisplayOrder().stream()
        .limit(12) // Limit to 12 categories for home screen
        .map(categoryMapper::toSimpleResponse)
        .collect(Collectors.toList());
  }

  /** Get minimal categories (without description) Even lighter for home screen */
  @Transactional(readOnly = true)
  public List<SimpleCategoryResponse> getMinimalCategories() {
    return categoryRepository.findAllOrderedByDisplayOrder().stream()
        .limit(12)
        .map(categoryMapper::toMinimalResponse)
        .collect(Collectors.toList());
  }

  /** Get all categories (for dedicated category screen) */
  @Transactional(readOnly = true)
  public List<SimpleCategoryResponse> getAllCategories() {
    return categoryRepository.findAllOrderedByDisplayOrder().stream()
        .map(categoryMapper::toSimpleResponse)
        .collect(Collectors.toList());
  }
}
