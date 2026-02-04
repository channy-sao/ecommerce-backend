package ecommerce_app.modules.category.service;

import ecommerce_app.modules.category.model.dto.CategoryRequest;
import ecommerce_app.modules.category.model.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
  CategoryResponse getCategoryByName(String name);

  CategoryResponse getCategoryById(Long id);

  CategoryResponse saveCategory(CategoryRequest categoryRequest);

  CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id);

  void bulkInsertCategories(List<CategoryRequest> categoryRequests);

  void importCategoriesFromExcel(MultipartFile file);

  void deleteCategory(Long id);

  Page<CategoryResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter);
}
