package ecommerce_app.infrastructure.model.response.body;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageResponse {
  private Integer totalPage;
  private Integer page;
  private Long totalCount;
  private Integer pageSize;

  public PageResponse(Page<?> page) {
    this.totalPage = page.getTotalPages();
    this.page = page.getNumber() + 1;
    this.totalCount = page.getTotalElements();
    this.pageSize = page.getSize();
  }
}
