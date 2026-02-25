package ecommerce_app.service;

import ecommerce_app.dto.request.BrandRequest;
import ecommerce_app.dto.response.BrandResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BrandService {

  /**
   * Get all brands regardless of their active status.
   *
   * @return list of BrandResponse
   */
  List<BrandResponse> getAllBrands();

  /**
   * Get only active brands sorted by display order ascending.
   *
   * @return list of BrandResponse
   */
  List<BrandResponse> getActiveBrands();

  /**
   * Create a new brand.
   *
   * @param request BrandRequest containing brand details
   * @return BrandResponse of created brand
   */
  BrandResponse createBrand(BrandRequest request);

  /**
   * Update an existing brand by its ID.
   *
   * @param id ID of the brand to update
   * @param request BrandRequest containing updated brand details
   * @return BrandResponse of updated brand
   */
  BrandResponse updateBrand(Long id, BrandRequest request);

  /**
   * Delete a brand by its ID.
   *
   * @param id ID of the brand to delete
   */
  void deleteBrand(Long id);

  BrandResponse getById(Long id);

  void toggleStatus(Long id);

  /**
   * Search active brands by name with pagination.
   * Used for brand browse/search screen.
   *
   * @param search optional name filter, null returns all
   * @param page   page number
   * @param size   page size
   */
  Page<BrandResponse> searchBrands(String search, int page, int size);
}
