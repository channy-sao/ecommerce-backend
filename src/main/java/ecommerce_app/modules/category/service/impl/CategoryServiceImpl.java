package ecommerce_app.modules.category.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.category.model.dto.CategoryRequest;
import ecommerce_app.modules.category.model.dto.CategoryResponse;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.category.repository.CategoryRepository;
import ecommerce_app.modules.category.service.CategoryService;
import ecommerce_app.modules.category.specification.CategorySpecification;
import ecommerce_app.util.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;
  private final ModelMapper modelMapper;

  @Transactional(readOnly = true)
  @Override
  public CategoryResponse getCategoryByName(String name) {
    Category category = categoryRepository.findByName(name);
    return toCategoryResponse(category);
  }

  @Transactional(readOnly = true)
  @Override
  public CategoryResponse getCategoryById(Long id) {
    log.info("Find category by id : {}", id);
    return toCategoryResponse(this.findById(id));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public CategoryResponse saveCategory(CategoryRequest categoryRequest) {
    Category category = modelMapper.map(categoryRequest, Category.class);
    Category savedCategory = categoryRepository.save(category);
    return toCategoryResponse(savedCategory);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id) {
    final Category existingCategory = findById(id);
    // map field
    modelMapper.map(categoryRequest, existingCategory);
    // save update
    Category savedCategory = categoryRepository.save(existingCategory);
    return toCategoryResponse(savedCategory);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteCategory(Long id) {
    log.info("Delete category by id : {}", id);
    this.categoryRepository.deleteById(id);
    log.info("Deleted category by id successfully");
  }

  @Transactional(readOnly = true)
  @Override
  public Page<CategoryResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter) {
    log.info("Filter category : filter = {}", filter);
    Specification<Category> specs = CategorySpecification.filter(filter);
    Sort sort = Sort.by(direction, sortBy);
    if (!isPage) {
      List<Category> categories = categoryRepository.findAll(specs, sort);
      List<CategoryResponse> responseList =
          categories.stream().map(this::toCategoryResponse).toList();
      return new PageImpl<>(responseList);
    }
    // default it starts from zero
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);

    return categoryRepository.findAll(specs, pageRequest).map(this::toCategoryResponse);
  }

  private Category findById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
  }

  private CategoryResponse toCategoryResponse(Category category) {
    final CategoryResponse categoryResponse = modelMapper.map(category, CategoryResponse.class);
    if (!CollectionUtils.isEmpty(category.getProducts())) {
      var products =
          category.getProducts().stream()
              .map(ProductMapper::toProductResponse)
              .toList();
      categoryResponse.setProducts(products);
    }
    return categoryResponse;
  }
}
