package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP verification response")
public class VerifyOtpResponse {

  @Schema(
      description = "Short-lived reset token to be used in the password reset step",
      example = "eyJhbGciOiJIUzI1NiJ9...")
  private String resetToken;

  @Schema(
      description = "Human-readable message about the OTP verification result",
      example = "OTP verified successfully")
  private String message;
}
