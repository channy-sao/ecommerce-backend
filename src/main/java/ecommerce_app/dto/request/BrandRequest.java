package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BrandRequest {
  @NotBlank
  @Size(max = 100)
  private String name;

  @Size(max = 500)
  private String description;

  private MultipartFile logo;

  private Boolean isActive = true;

  private Integer displayOrder = 0;
}
