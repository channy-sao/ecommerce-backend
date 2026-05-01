package ecommerce_app.service.impl;

import static ecommerce_app.util.ExcelCellUtils.*;

import ecommerce_app.constant.enums.WarrantyType;
import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.request.ProductRequest;
import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.response.ImportProductFromExcelResponse;
import ecommerce_app.dto.response.NearEmptyStockResponse;
import ecommerce_app.dto.response.ProductResponse;
import ecommerce_app.entity.Category;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.entity.ProductSpec;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.DuplicateResourceException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.BrandRepository;
import ecommerce_app.repository.CategoryRepository;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.ProductVariantRepository;
import ecommerce_app.service.ProductService;
import ecommerce_app.service.ProductVariantService;
import ecommerce_app.service.SettingService;
import ecommerce_app.specification.ProductSpecification;
import ecommerce_app.util.AuthenticationUtils;
import ecommerce_app.util.FileUtils;
import ecommerce_app.util.ProductMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
  private final BrandRepository brandRepository;
  private final ProductVariantService variantService;
  private final ProductVariantRepository variantRepository; // add to fields

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductResponse saveProduct(ProductRequest productRequest) {
    log.info("Saving product request: {}", productRequest);
    try {
      // check a duplicate product name
      final var productName = productRequest.getName().trim(); // remove leading/trailing spaces
      if (productRepository.existsByName(productName)) {
        throw new DuplicateResourceException("Product", "name", productName);
      }
//      Product product = modelMapper.map(productRequest, Product.class);
      Product product = new Product();
      product.setName(productName);
      product.setPrice(productRequest.getPrice());
      product.setDescription(productRequest.getDescription());
      product.setIsFeature(productRequest.getIsFeature());
      product.setUuid(UUID.randomUUID());
      product.setCode("TEMP");
      if (productRequest.getBrandId() != null) {
        product.setBrand(
            brandRepository
                .findById(productRequest.getBrandId())
                .orElseThrow(
                    () -> new ResourceNotFoundException("Brand" + productRequest.getBrandId())));
      } else {
        product.setBrand(null);
      }
      product.setImages(new ArrayList<>()); // must init before saveProductImages
      product.setSpecs(new ArrayList<>()); // init specs list
      product.setCategory(findCategoryById(productRequest.getCategoryId()));

      saveProductImages(productRequest.getImages(), product);
      saveProductSpecs(productRequest.getSpecs(), product);

      // warranty
      product.setWarrantyType(
          productRequest.getWarrantyType() != null
              ? productRequest.getWarrantyType()
              : WarrantyType.NONE);
      product.setWarrantyDuration(productRequest.getWarrantyDuration());
      product.setWarrantyUnit(productRequest.getWarrantyUnit());
      product.setWarrantyDescription(productRequest.getWarrantyDescription());
      Product saved = productRepository.save(product);
      // Generate clean business code
      String code = "PRD-" + String.format("%04d", saved.getId());

      // Update-only code (no full save again)
      productRepository.updateCode(saved.getId(), code);

      // Set back for response
      saved.setCode(code);

      // set variant
      if (Boolean.TRUE.equals(productRequest.getHasVariants())
              && productRequest.getVariants() != null
              && !productRequest.getVariants().isEmpty()) {

        saved.setHasVariants(true);
        productRepository.save(saved);

        for (ProductVariantRequest varReq : productRequest.getVariants()) {
          variantService.createVariant(saved.getId(), varReq);
        }
        // re fetch product
        Product finalSaved = saved;
        saved = productRepository.findById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", finalSaved.getId()));
      }

      return ProductMapper.toProductResponse(saved);

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
      final var productName = productRequest.getName().trim(); // prevent leading/trailing spaces

      // check a duplicate product name
      if (!existingProduct.getName().equals(productName)
          && productRepository.existsByName(productName)) {
        throw new DuplicateResourceException("Product", "name", productName);
      }
      productRequest.setName(productName);
      updateProductFields(productRequest, existingProduct);

      if (productRequest.getBrandId() != null) {
        existingProduct.setBrand(
            brandRepository
                .findById(productRequest.getBrandId())
                .orElseThrow(
                    () -> new ResourceNotFoundException("Brand" + productRequest.getBrandId())));
      } else {
        existingProduct.setBrand(null);
      }

      // warranty
      existingProduct.setWarrantyType(
          productRequest.getWarrantyType() != null
              ? productRequest.getWarrantyType()
              : WarrantyType.NONE);
      existingProduct.setWarrantyDuration(productRequest.getWarrantyDuration());
      existingProduct.setWarrantyUnit(productRequest.getWarrantyUnit());
      existingProduct.setWarrantyDescription(productRequest.getWarrantyDescription());

      // Update category if changed
      if (!Objects.equals(productRequest.getCategoryId(), existingProduct.getCategory().getId())) {
        existingProduct.setCategory(findCategoryById(productRequest.getCategoryId()));
      }

      handleImageUpdate(productRequest, existingProduct);
      handleSpecUpdate(productRequest, existingProduct);
      handleVariantUpdate(productRequest, existingProduct);

      return ProductMapper.toProductResponse(productRepository.save(existingProduct));

    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  /**
   * Three scenarios:
   * 1. variants = null           → no change (user didn't touch variants)
   * 2. hasVariants switched to false → deactivate all existing variants
   * 3. variants = [...] provided → update existing, add new ones
   */
  private void handleVariantUpdate(ProductRequest request, Product product) {
    // null = no change intended — skip entirely
    if (request.getVariants() == null && request.getHasVariants() == null) return;

    // Switching hasVariants flag
    if (request.getHasVariants() != null) {
      product.setHasVariants(request.getHasVariants());
    }

    // Turned OFF variants → deactivate all existing variants
    if (!Boolean.TRUE.equals(product.getHasVariants())) {
      variantRepository.findByProductId(product.getId())
              .forEach(v -> {
                v.setIsActive(false);
                variantRepository.save(v);
              });
      return;
    }

    // No variant list sent → nothing to add or update
    if (request.getVariants() == null || request.getVariants().isEmpty()) return;

    for (ProductVariantRequest varReq : request.getVariants()) {
      if (varReq.getId() != null) {
        // ── Update existing variant ───────────────────────────
        variantService.updateVariant(varReq.getId(), varReq);
      } else {
        // ── Add new variant ───────────────────────────────────
        variantService.createVariant(product.getId(), varReq);
      }
    }
  }

  /**
   * Three scenarios: 1. New images provided → delete old files from storage + clear DB rows + save
   * new 2. Empty list sent → delete old files from storage + clear DB rows (user removed all) 3. No
   * images field sent → keep existing (no change)
   */
  private void handleImageUpdate(ProductRequest request, Product product) {

    // Step 1: remove deleted images
    if (request.getRemoveImageIds() != null && !request.getRemoveImageIds().isEmpty()) {
      List<ProductImage> toRemove =
          product.getImages().stream()
              .filter(img -> request.getRemoveImageIds().contains(img.getId()))
              .toList();
      toRemove.forEach(
          img -> fileManagerService.deleteFile(storageConfig.getProductPath(), img.getImagePath()));
      product.getImages().removeAll(toRemove);
    }

    // Step 2: upload new files, keep them in a list indexed by upload order
    List<ProductImage> newlyAdded = new ArrayList<>();
    if (request.getImages() != null && !request.getImages().isEmpty()) {
      for (MultipartFile file : request.getImages()) {
        if (file == null || file.isEmpty()) continue;
        String path = fileManagerService.saveFile(file, storageConfig.getProductPath());
        if (path != null) {
          ProductImage img = new ProductImage();
          img.setImagePath(path);
          img.setSortOrder(-1);
          img.setProduct(product);
          product.getImages().add(img);
          newlyAdded.add(img);
        }
      }
    }

    // Step 3: apply unified order
    // imageOrder uses existing IDs for saved images, -1 for each new image slot (in upload order)
    // e.g. user dragged: [new-A, existing-1, existing-2, new-B]
    //      imageOrder  = [-1, 1, 2, -1]
    //      images      = [fileA, fileB]
    if (request.getImageOrder() != null && !request.getImageOrder().isEmpty()) {
      Map<Long, ProductImage> existingMap =
          product.getImages().stream()
              .filter(img -> img.getId() != null)
              .collect(Collectors.toMap(ProductImage::getId, img -> img));

      int newIndex = 0;
      for (int sortOrder = 0; sortOrder < request.getImageOrder().size(); sortOrder++) {
        Long id = request.getImageOrder().get(sortOrder);
        if (id == -1L) {
          // slot for next new image
          if (newIndex < newlyAdded.size()) {
            newlyAdded.get(newIndex++).setSortOrder(sortOrder);
          }
        } else {
          ProductImage img = existingMap.get(id);
          if (img != null) img.setSortOrder(sortOrder);
        }
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
  public ProductResponse getProductByCode(String code) {
    log.info("Getting product by code: {}", code);
    Product product =
        productRepository
            .findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Product", code));
    return ProductMapper.toProductResponse(product);
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
      Long brandId,
      String filter) {

    Specification<Product> specification = Specification.allOf();
    if (categoryId != null) {
      specification = specification.and(ProductSpecification.withCategory(categoryId));
    }
    if (brandId != null) {
      specification = specification.and(ProductSpecification.withBrand(brandId));
    }
    if (filter != null) {
      specification =
          specification.and(
              ProductSpecification.withName(filter)
                  .or(ProductSpecification.withDescription(filter))
                  .or(ProductSpecification.withCategoryName(filter))
                  .or(ProductSpecification.withCode(filter)));
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
    int totalRows = 0;
    int successRows = 0;
    int errorRows = 0;

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

  @Transactional(readOnly = true)
  @Override
  public Page<ProductResponse> getProductsByBrandForAdmin(
      Long brandId, String search, int page, int size) {

    brandRepository
        .findById(brandId)
        .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + brandId));

    String searchParam = (search == null || search.isBlank()) ? null : search.trim();

    return productRepository
        .findByBrandIdForAdmin(brandId, searchParam, PageRequest.of(page - 1, size))
        .map(ProductMapper::toProductResponse);
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

  private void saveProductSpecs(List<String> specTexts, Product product) {
    if (specTexts == null || specTexts.isEmpty()) return;
    for (int i = 0; i < specTexts.size(); i++) {
      String text = specTexts.get(i);
      if (text == null || text.isBlank()) continue;
      ProductSpec spec = new ProductSpec();
      spec.setSpecText(text.trim());
      spec.setSortOrder(i);
      spec.setProduct(product);
      product.getSpecs().add(spec);
    }
  }

  private void handleSpecUpdate(ProductRequest request, Product product) {
    if (request.getSpecs() == null) return; // null = no change
    product.getSpecs().clear(); // orphanRemoval = true will delete old rows
    for (int i = 0; i < request.getSpecs().size(); i++) {
      String text = request.getSpecs().get(i);
      if (text == null || text.isBlank()) continue;
      ProductSpec spec = new ProductSpec();
      spec.setSpecText(text.trim());
      spec.setSortOrder(i);
      spec.setProduct(product);
      product.getSpecs().add(spec);
    }
  }
}
