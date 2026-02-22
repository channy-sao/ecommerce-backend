package ecommerce_app.service.impl;

import static ecommerce_app.util.ExcelCellUtils.*;

import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.entity.Category;
import ecommerce_app.repository.CategoryRepository;
import ecommerce_app.dto.response.ImportProductFromExcelResponse;
import ecommerce_app.dto.response.NearEmptyStockResponse;
import ecommerce_app.dto.request.ProductRequest;
import ecommerce_app.dto.response.ProductResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.service.ProductService;
import ecommerce_app.specification.ProductSpecification;
import ecommerce_app.service.SettingService;
import ecommerce_app.util.AuthenticationUtils;
import ecommerce_app.util.FileUtils;
import ecommerce_app.util.ProductMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
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
  private final StaticResourceService staticResourceService;
  private final StorageConfig storageConfig;
  private final SettingService settingService;

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductResponse saveProduct(ProductRequest productRequest) {
    log.info("Saving product request: {}", productRequest);
    try {
      Product product = modelMapper.map(productRequest, Product.class);
      product.setId(null);
      product.setImages(new ArrayList<>()); // must init before saveProductImages
      product.setCategory(findCategoryById(productRequest.getCategoryId()));

      saveProductImages(productRequest.getImages(), product);

      return ProductMapper.toProductResponse(productRepository.save(product));

    } catch (DataIntegrityViolationException e) {
      log.error("Data integrity violation: {}", e.getMessage());
      throw new BadRequestException("Data integrity violation");
    }
  }

  // -------------------------------------------------------------------------
  // UPDATE
  // -------------------------------------------------------------------------

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductResponse updateProduct(ProductRequest productRequest, Long id) {
    log.info("Updating product request: {}", productRequest);
    try {
      final var existingProduct = getById(id);
      updateProductFields(productRequest, existingProduct);

      // Update category if changed
      if (!Objects.equals(productRequest.getCategoryId(), existingProduct.getCategory().getId())) {
        existingProduct.setCategory(findCategoryById(productRequest.getCategoryId()));
      }

      handleImageUpdate(productRequest, existingProduct);

      return ProductMapper.toProductResponse(productRepository.save(existingProduct));

    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  /**
   * Three scenarios: 1. New images provided → delete old files from storage + clear DB rows + save
   * new 2. Empty list sent → delete old files from storage + clear DB rows (user removed all) 3. No
   * images field sent → keep existing (no change)
   */
  // Updated handleImageUpdate in ProductServiceImpl
  private void handleImageUpdate(ProductRequest request, Product product) {

    // Step 1: remove images the user deleted
    if (request.getRemoveImageIds() != null && !request.getRemoveImageIds().isEmpty()) {
      List<ProductImage> toRemove =
          product.getImages().stream()
              .filter(img -> request.getRemoveImageIds().contains(img.getId()))
              .toList();
      toRemove.forEach(
          img -> fileManagerService.deleteFile(storageConfig.getProductPath(), img.getImagePath()));
      product.getImages().removeAll(toRemove);
    }

    // Step 2: add new uploaded images (appended at the end for now)
    if (request.getImages() != null && !request.getImages().isEmpty()) {
      int maxOrder =
          product.getImages().stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1);
      for (MultipartFile file : request.getImages()) {
        if (file == null || file.isEmpty()) continue;
        String path = fileManagerService.saveFile(file, storageConfig.getProductPath());
        if (path != null) {
          ProductImage img = new ProductImage();
          img.setImagePath(path);
          img.setSortOrder(++maxOrder);
          img.setProduct(product);
          product.getImages().add(img);
        }
      }
    }

    // Step 3: apply final order from imageOrder list
    if (request.getImageOrder() != null && !request.getImageOrder().isEmpty()) {
      Map<Long, ProductImage> imageMap =
          product.getImages().stream().collect(Collectors.toMap(ProductImage::getId, img -> img));
      for (int i = 0; i < request.getImageOrder().size(); i++) {
        ProductImage img = imageMap.get(request.getImageOrder().get(i));
        if (img != null) img.setSortOrder(i);
      }
    }
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteProduct(Long id) {
    log.info("Deleting product {}", id);
    var product = getById(id);
    product.softDelete(AuthenticationUtils.getCurrentUserId());
    productRepository.save(product);
    log.info("Deleted product {}", id);
  }

  // -------------------------------------------------------------------------
  // READ
  // -------------------------------------------------------------------------

  @Transactional(readOnly = true)
  @Override
  public ProductResponse getProductById(Long id) {
    return ProductMapper.toProductResponse(getById(id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ProductResponse> getProducts() {
    return productRepository.findAll().stream().map(ProductMapper::toProductResponse).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<ProductResponse>  filter(
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
      List<ProductResponse> list =
          productRepository.findAll(specification, sort).stream()
              .map(ProductMapper::toProductResponse)
              .toList();
      return new PageImpl<>(list);
    }

    PageRequest pageable = PageRequest.of(page - 1, pageSize, sort);
    return productRepository.findAll(specification, pageable).map(ProductMapper::toProductResponse);
  }

  // -------------------------------------------------------------------------
  // EXCEL IMPORT
  // -------------------------------------------------------------------------

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ImportProductFromExcelResponse importProductFromExcel(MultipartFile file) {
    log.info("Importing products from excel: {}", file.getOriginalFilename());
    FileUtils.validateExcelFile(file);

    ImportProductFromExcelResponse response = new ImportProductFromExcelResponse();
    int totalRows = 0, successRows = 0, errorRows = 0;

    try (InputStream is = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(is)) {

      Sheet sheet = workbook.getSheetAt(0);
      validateColumnHeader(sheet.getRow(0));

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;
        totalRows++;

        try {
          ProductRequest request = mapRowToProductRequest(row);
          Product product = modelMapper.map(request, Product.class);
          product.setId(null);
          product.setImages(new ArrayList<>());
          product.setCategory(
              categoryRepository
                  .findById(request.getCategoryId())
                  .orElseThrow(
                      () -> new ResourceNotFoundException("Category", request.getCategoryId())));

          // Default image when Excel has no image column
          addDefaultImageIfEmpty(product);

          productRepository.save(product);
          successRows++;

        } catch (Exception ex) {
          log.error("Error inserting product at row {}", row.getRowNum(), ex);
          errorRows++;
        }
      }

    } catch (Exception e) {
      log.error("Import product from excel failed", e);
      throw new BadRequestException("Import product from excel failed");
    }

    if (successRows == 0) {
      throw new BadRequestException("Import product from excel failed all rows, please try again");
    }

    response.setTotalCount(totalRows);
    response.setSuccessCount(successRows);
    response.setErrorCount(errorRows);
    return response;
  }

  @Transactional(readOnly = true)
  @Override
  public List<NearEmptyStockResponse> getNearEmptyStockProducts() {
    int threshold = settingService.getInt("order.low_stock_threshold");
    return productRepository.findNearEmptyStockProducts(threshold).stream()
        .map(p -> toNearEmptyStockResponse(p, threshold))
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public long countNearEmptyStockProducts() {
    int threshold = settingService.getInt("order.low_stock_threshold");
    return productRepository.countNearEmptyStockProducts(threshold);
  }

  private NearEmptyStockResponse toNearEmptyStockResponse(Product product, int threshold) {
    return NearEmptyStockResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .primaryImage(staticResourceService.getProductImageUrl(product.getPrimaryImagePath()))
        .categoryName(product.getCategory().getName())
        .currentQuantity(product.getStockQuantity())
        .threshold(threshold)
        .stockStatus(product.getStockStatus())
        .build();
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private Category findCategoryById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
  }

  private Product getById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product", id));
  }

  private void updateProductFields(ProductRequest request, Product product) {
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setIsFeature(request.getIsFeature());
  }

  /**
   * Saves each non-empty MultipartFile to storage, creates a ProductImage entity, and adds it to
   * the product's images list. sortOrder is assigned by position in the list (0 = primary).
   */
  private void saveProductImages(List<MultipartFile> files, Product product) {
    if (files == null || files.isEmpty()) return;
    int order = 0;
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) continue;
      String savedPath = fileManagerService.saveFile(file, storageConfig.getProductPath());
      if (savedPath != null) {
        ProductImage img = new ProductImage();
        img.setImagePath(savedPath); // raw filename only
        img.setSortOrder(order++);
        img.setProduct(product);
        product.getImages().add(img);
      }
    }
  }

  /**
   * Deletes physical files from storage by raw filename. Called before clearing the images list in
   * update/delete scenarios.
   */
  private void deleteImageFiles(List<String> imagePaths) {
    if (imagePaths == null) return;
    imagePaths.forEach(path -> fileManagerService.deleteFile(storageConfig.getProductPath(), path));
  }

  /**
   * For Excel imports where no image is provided, assign a default image so the product always has
   * at least one image.
   */
  private void addDefaultImageIfEmpty(Product product) {
    if (product.getImages().isEmpty()) {
      ProductImage defaultImg = new ProductImage();
      defaultImg.setImagePath("default-product.png");
      defaultImg.setSortOrder(0);
      defaultImg.setProduct(product);
      product.getImages().add(defaultImg);
    }
  }

  void validateColumnHeader(Row row) {
    // TODO: implement header validation
  }

  private ProductRequest mapRowToProductRequest(Row row) {
    return ProductRequest.builder()
        .name(getStringCell(row.getCell(0)))
        .description(getStringCell(row.getCell(1)))
        .price(getBigDecimalCell(row.getCell(2)))
        .images(null) // images are not in Excel, handled by addDefaultImageIfEmpty
        .categoryId(getLongCell(row.getCell(4)))
        .isFeature(getBooleanCell(row.getCell(5)))
        .build();
  }
}
