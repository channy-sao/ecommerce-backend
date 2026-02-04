package ecommerce_app.modules.category.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Bulk Category Request DTO")
public class BulkCategoryRequest {
  @NotEmpty(message = "Category list must not be empty")
  @NotNull(message = "Category list must not be null")
  @Schema(description = "List of Category Requests")
  private List<CategoryRequest> categories;
}
