package ecommerce_app.service;

import ecommerce_app.dto.response.SimpleBrandResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface MobileBrandService {
  
  /**
   * Get only active brands sorted by display order ascending.
   *
   * @return list of BrandResponse
   */
  List<SimpleBrandResponse> getActiveBrands();


  /**
   * Search active brands by name with pagination.
   * Used for brand browse/search screen.
   *
   * @param search optional name filter, null returns all
   * @param page   page number
   * @param size   page size
   */
  Page<SimpleBrandResponse> searchBrands(String search, int page, int size);
}
