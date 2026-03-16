package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneLoginRequest {

  @NotBlank(message = "Firebase ID token is required")
  private String idToken;
}