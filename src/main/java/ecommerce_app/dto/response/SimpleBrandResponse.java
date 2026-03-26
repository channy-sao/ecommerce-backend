package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Simplified brand response for embedded use")
public class SimpleBrandResponse {

  @Schema(description = "Unique brand ID", example = "1")
  private Long id;

  @Schema(description = "Brand name", example = "Nike")
  private String name;

  @Schema(description = "Brand logo URL", example = "https://cdn.example.com/brands/nike.png")
  private String logo;
}
