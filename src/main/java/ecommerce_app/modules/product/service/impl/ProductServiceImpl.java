package ecommerce_app.modules.product.service.impl;

import static ecommerce_app.util.ExcelCellUtils.getBigDecimalCell;
import static ecommerce_app.util.ExcelCellUtils.getBooleanCell;
import static ecommerce_app.util.ExcelCellUtils.getLongCell;
import static ecommerce_app.util.ExcelCellUtils.getStringCell;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.category.repository.CategoryRepository;
import ecommerce_app.modules.product.model.dto.ImportProductFromExcelResponse;
import ecommerce_app.modules.product.model.dto.ProductRequest;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.product.service.ProductService;
import ecommerce_app.modules.product.specification.ProductSpecification;
import ecommerce_app.util.AuthenticationUtils;
import ecommerce_app.util.FileUtils;
import ecommerce_app.util.ProductMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ModelMapper modelMapper;
  private final FileManagerService fileManagerService;
  private final StorageConfigProperty storageConfigProperty;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductResponse saveProduct(ProductRequest productRequest) {
    log.info("Saving product request: {}", productRequest);

    try {
      Product product = modelMapper.map(productRequest, Product.class);

      // 🚨 Force null id to prevent detached update attempt
      product.setId(null);

      product.setCategory(this.findCategoryById(productRequest.getCategoryId()));

      if (productRequest.getImage() != null) {
        String imagePath =
            fileManagerService.saveFile(
                productRequest.getImage(), storageConfigProperty.getProduct());

        if (imagePath != null) {
          product.setImage(imagePath);
        }
      }

      Product saved = productRepository.save(product);

      return ProductMapper.toProductResponse(saved);

    } catch (DataIntegrityViolationException e) {
      log.error("Data integrity violation: {}", e.getMessage());
      throw new BadRequestException("Data integrity violation");
    }
  }

  private Category findCategoryById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductResponse updateProduct(ProductRequest productRequest, Long id) {
    log.info("Updating product request: {}", productRequest);
    try {
      final var existingProduct = this.getById(id);
      final var existingImage = existingProduct.getImage();
      updateProductFromRequest(productRequest, existingProduct);
      // if update category
      if (!Objects.equals(productRequest.getCategoryId(), existingProduct.getCategory().getId())) {
        final var category = this.findCategoryById(productRequest.getCategoryId());
        existingProduct.setCategory(category);
      }

      // FIXED: Explicit image handling with clear scenarios
      boolean hasImageInRequest = productRequest.getImage() != null;
      boolean isImageEmpty = hasImageInRequest && productRequest.getImage().isEmpty();
      boolean isImageFile = hasImageInRequest && !productRequest.getImage().isEmpty();

      if (isImageFile) {
        // Scenario 1: New image file provided
        log.info("Image: REPLACING with new file");
        if (existingImage != null) {
          this.fileManagerService.deleteFile(storageConfigProperty.getProduct(), existingImage);
        }
        String savedProductImage =
            fileManagerService.saveFile(
                productRequest.getImage(), storageConfigProperty.getProduct());
        existingProduct.setImage(savedProductImage);

      } else if (isImageEmpty) {
        // Scenario 2: Empty file explicitly sent (user removed image)
        log.info("Image: REMOVING (empty file received)");
        if (existingImage != null) {
          this.fileManagerService.deleteFile(storageConfigProperty.getProduct(), existingImage);
        }
        existingProduct.setImage(null);

      } else if (!hasImageInRequest) {
        // Scenario 3: No image field in request (keep existing)
        log.info("Image: KEEPING existing (no image field in request)");
        existingProduct.setImage(existingImage);
      }

      return ProductMapper.toProductResponse(productRepository.save(existingProduct));

    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteProduct(Long id) {
    log.info("Deleting product {}", id);
    var product = this.getById(id);
    product.softDelete(AuthenticationUtils.getCurrentUserId());
    productRepository.save(product);
    log.info("Deleted product {}", id);
  }

  @Transactional(readOnly = true)
  @Override
  public ProductResponse getProductById(Long id) {
    log.info("Retrieving product {}", id);
    return ProductMapper.toProductResponse(this.getById(id));
  }

  private Product getById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product", id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ProductResponse> getProducts() {
    return productRepository.findAll().stream().map(ProductMapper::toProductResponse).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<ProductResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      Long categoryId,
      String filter) {
    Specification<Product> specification = Specification.allOf();
    if (categoryId != null) {
      specification = specification.and(ProductSpecification.withCategory(categoryId));
    }
    if (filter != null) {
      specification =
          specification.and(
              ProductSpecification.withName(filter)
                  .or(ProductSpecification.withDescription(filter))
                  .or(ProductSpecification.withCategoryName(filter)));
    }
    Sort sort = Sort.by(direction, sortBy);
    if (!isPage) {
      final List<Product> productList = productRepository.findAll(specification, sort);
      final List<ProductResponse> productResponseList =
          productList.stream().map(ProductMapper::toProductResponse).toList();
      return new PageImpl<>(productResponseList);
    }
    // page start from 0
    final var pageable = PageRequest.of(page - 1, pageSize, sort);
    final Page<Product> productPage = productRepository.findAll(specification, pageable);
    return productPage.map(ProductMapper::toProductResponse);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ImportProductFromExcelResponse importProductFromExcel(MultipartFile file) {

    log.info("Importing product from excel file {}", file.getOriginalFilename());

    // 1. Validate excel file
    FileUtils.validateExcelFile(file);

    ImportProductFromExcelResponse response = new ImportProductFromExcelResponse();

    int totalRows = 0;
    int totalSuccessRows = 0;
    int totalErrorRows = 0;

    try (InputStream is = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(is)) {

      Sheet sheet = workbook.getSheetAt(0);

      // 2. Validate header
      Row headerRow = sheet.getRow(0);
      validateColumnHeader(headerRow);

      // 3. Iterate rows (skip header)
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {

        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }

        totalRows++;

        try {
          ProductRequest request = mapRowToProductRequest(row);

          // Map request → entity
          Product product = modelMapper.map(request, Product.class);

          // Force insert (important)
          product.setId(null);

          // Set category
          Category category =
              categoryRepository
                  .findById(request.getCategoryId())
                  .orElseThrow(
                      () -> new ResourceNotFoundException("Category", request.getCategoryId()));

          product.setCategory(category);

          // Default image if Excel doesn't contain one
          if (product.getImage() == null) {
            product.setImage("default-product.png");
          }

          productRepository.save(product);

          totalSuccessRows++;

        } catch (Exception ex) {
          log.error("Error inserting product at row {}", row.getRowNum(), ex);
          totalErrorRows++;
        }
      }

    } catch (Exception e) {
      log.error("Import product from excel failed", e);
      throw new BadRequestException("Import product from excel failed");
    }
    if (totalSuccessRows == 0) {
      throw new BadRequestException("Import product from excel failed all rows, please try again");
    }
    response.setTotalCount(totalRows);
    response.setSuccessCount(totalSuccessRows);
    response.setErrorCount(totalErrorRows);

    return response;
  }

  // Create a custom mapping method
  private void updateProductFromRequest(ProductRequest request, Product existingProduct) {
    // Map simple fields
    existingProduct.setName(request.getName());
    existingProduct.setDescription(request.getDescription());
    existingProduct.setPrice(request.getPrice());
    existingProduct.setIsFeature(request.getIsFeature());
  }

  void validateColumnHeader(Row row) {
    // TODO: check validation
  }

  private ProductRequest mapRowToProductRequest(Row row) {

    return ProductRequest.builder()
        .name(getStringCell(row.getCell(0)))
        .description(getStringCell(row.getCell(1)))
        .price(getBigDecimalCell(row.getCell(2)))
        // image handled separately (see below)
        .image(null)
        .categoryId(getLongCell(row.getCell(4)))
        .isFeature(getBooleanCell(row.getCell(5)))
        .build();
  }
}
