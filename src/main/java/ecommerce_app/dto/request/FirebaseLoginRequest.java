package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirebaseLoginRequest {
  @NotBlank(message = "idToken could not be null")
  @Schema(description = "idToken of Firebase")
  private String idToken;
}
