package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompletePhoneProfileRequest {

  @NotBlank(message = "First name is required")
  @Size(max = 50)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50)
  private String lastName;
  
  private String email; // optional
}