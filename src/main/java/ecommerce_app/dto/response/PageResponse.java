package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Pagination metadata response")
public class PageResponse {

  @Schema(description = "Total number of pages", example = "10")
  private Integer totalPage;

  @Schema(description = "Current page number (1-based)", example = "1")
  private Integer page;

  @Schema(description = "Total number of records across all pages", example = "100")
  private Long totalCount;

  @Schema(description = "Number of records per page", example = "10")
  private Integer pageSize;

  public PageResponse(Page<?> page) {
    this.totalPage = page.getTotalPages();
    this.page = page.getNumber() + 1;
    this.totalCount = page.getTotalElements();
    this.pageSize = page.getSize();
  }
}
