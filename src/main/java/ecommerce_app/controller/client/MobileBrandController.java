package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.service.MobileBrandService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/v1/brands")
@RequiredArgsConstructor
@Tag(
    name = "Mobile Brand Controller",
    description = "APIs for mobile clients to retrieve brand information")
public class MobileBrandController {

  private final MobileBrandService mobileBrandService;
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<SimpleBrandResponse>>> getActiveBrands() {
    return BaseBodyResponse.success(
        mobileBrandService.getActiveBrands(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  /** GET Search and browse all active brands with pagination. */
  @GetMapping("/filter")
  public ResponseEntity<BaseBodyResponse<List<SimpleBrandResponse>>> searchBrands(
      @RequestParam(required = false, value = "search", defaultValue = "") String search,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

    return BaseBodyResponse.pageSuccess(
        mobileBrandService.searchBrands(search, page, pageSize),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
