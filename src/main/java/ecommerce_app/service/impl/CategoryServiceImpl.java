package ecommerce_app.service.impl;

import static ecommerce_app.util.ExcelCellUtils.getStringCell;

import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.DuplicateResourceException;
import ecommerce_app.exception.InternalServerErrorException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.dto.request.CategoryRequest;
import ecommerce_app.dto.response.CategoryResponse;
import ecommerce_app.entity.Category;
import ecommerce_app.mapper.CategoryMapper;
import ecommerce_app.repository.CategoryRepository;
import ecommerce_app.service.CategoryService;
import ecommerce_app.specification.CategorySpecification;
import ecommerce_app.util.FileUtils;
import ecommerce_app.util.ProductMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;
  private final ModelMapper modelMapper;
  private final CategoryMapper categoryMapper;

  @Transactional(readOnly = true)
  @Override
  public CategoryResponse getCategoryByName(String name) {
    Category category = categoryRepository.findByName(name);
    return categoryMapper.toResponse(category);
  }

  @Transactional(readOnly = true)
  @Override
  public CategoryResponse getCategoryById(Long id) {
    log.info("Find category by id : {}", id);
    return categoryMapper.toResponse(this.findById(id));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public CategoryResponse saveCategory(CategoryRequest categoryRequest) {
    log.info("Save category : {}", categoryRequest);
    final var categoryName = categoryRequest.getName().trim(); // remove leading/trailing spaces
    final var categoryDescription =
        categoryRequest.getDescription().trim(); // remove leading/trailing spaces
    if (Boolean.TRUE.equals(categoryRepository.existsByName(categoryName))) {
      throw new DuplicateResourceException("Category", "name", categoryName);
    }
    Category category = new Category();
    category.setName(categoryName);
    category.setDescription(categoryDescription);
    category.setIcon(categoryRequest.getIcon());
    if (category.getDisplayOrder() == null) {
      category.setDisplayOrder(0);
    }
    Category savedCategory = categoryRepository.save(category);
    return categoryMapper.toResponse(savedCategory);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id) {
    final Category existingCategory = findById(id);
    final var categoryName = categoryRequest.getName().trim(); // remove leading/trailing spaces
    final var categoryDescription = categoryRequest.getDescription().trim();
    // check duplicate name only if the name is actually changing
    if (!existingCategory.getName().equalsIgnoreCase(categoryName)
        && Boolean.TRUE.equals(categoryRepository.existsByName(categoryName))) {
      throw new DuplicateResourceException("Category", "name", categoryName);
    }
    // map field
    existingCategory.setName(categoryName);
    existingCategory.setDescription(categoryDescription);
    existingCategory.setIcon(categoryRequest.getIcon());
    existingCategory.setDisplayOrder(categoryRequest.getDisplayOrder());
    existingCategory.setDisplayOrder(
        categoryRequest.getDisplayOrder() == null ? 0 : categoryRequest.getDisplayOrder());
    // save update
    Category savedCategory = categoryRepository.save(existingCategory);
    return categoryMapper.toResponse(savedCategory);
  }

  @Override
  public void bulkInsertCategories(List<CategoryRequest> categoryRequests) {
    log.error("bulkInsertCategories not implemented yet");
    throw new InternalServerErrorException("Not implemented yet");
  }

  @Override
  public void importCategoriesFromExcel(MultipartFile file) {
    log.info("Importing product from excel file {}", file.getOriginalFilename());

    // 1. Validate excel file
    FileUtils.validateExcelFile(file);

    try {
      try (InputStream is = file.getInputStream();
          Workbook workbook = WorkbookFactory.create(is)) {
        Sheet sheet = workbook.getSheetAt(0);

        // 3. Iterate rows (skip header)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

          Row row = sheet.getRow(i);
          if (row == null) {
            continue;
          }

          CategoryRequest request = mapRowToProductRequest(row);
          // Map request → entity
          Category category = modelMapper.map(request, Category.class);
          // Set the default display order if not in Excel
          if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(i); // Use row number as the default order
          }
          categoryRepository.save(category);
        }
      }
    } catch (Exception e) {
      log.error("Import product from excel failed", e);
      throw new BadRequestException("Import product from excel failed");
    }
  }

  // get category request from row
  private CategoryRequest mapRowToProductRequest(Row row) {
    return CategoryRequest.builder()
        .name(getStringCell(row.getCell(0)))
        .description(getStringCell(row.getCell(1)))
        .icon(getStringCell(row.getCell(2)))
        .displayOrder(getIntegerCellValue(row))
        .build();
  }

  /** Helper method to safely get integer value from Excel cell */
  private Integer getIntegerCellValue(Row row) {
    try {
      var cell = row.getCell(3);
      if (cell == null) {
        return null;
      }

      return switch (cell.getCellType()) {
        case NUMERIC -> (int) cell.getNumericCellValue();
        case STRING -> {
          String value = cell.getStringCellValue().trim();
          yield value.isEmpty() ? null : Integer.parseInt(value);
        }
        default -> null;
      };
    } catch (Exception e) {
      log.warn("Error parsing integer from cell at index {}: {}", 3, e.getMessage());
      return null;
    }
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
          categories.stream().map(categoryMapper::toResponse).toList();
      return new PageImpl<>(responseList);
    }
    // by default, it starts from zero
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);

    return categoryRepository.findAll(specs, pageRequest).map(categoryMapper::toResponse);
  }

  private Category findById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
  }
}
