package ecommerce_app.modules.address.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AddressRequest {
  @NotBlank(message = "Line1 is required")
  @Size(max = 50)
  @Schema(description = "Primary address line", example = "House 24, Street 136")
  private String line1;

  @Size(max = 50)
  @Schema(description = "Secondary address line (optional)", example = "Apartment 5B")
  private String line2;

  @NotBlank(message = "Street is required")
  @Size(max = 100)
  @Schema(description = "Street name", example = "Street 136")
  private String street;

  @NotBlank(message = "State is required")
  @Size(max = 100)
  @Schema(description = "State or province", example = "Phnom Penh")
  private String state;

  @NotBlank(message = "City is required")
  @Size(max = 100)
  @Schema(description = "City name", example = "Phnom Penh")
  private String city;

  @NotBlank(message = "Country is required")
  @Size(max = 100)
  @Schema(description = "Country name", example = "Cambodia")
  private String country;

  @Size(max = 100)
  @Schema(description = "Postal or ZIP code", example = "12000")
  private String postalCode;

  @Schema(description = "Set this address as the default", example = "true")
  private boolean isDefault;

  @Schema(description = "Latitude coordinate", example = "11.5564")
  private Double latitude;

  @Schema(description = "Longitude coordinate", example = "104.9282")
  private Double longitude;

  @Schema(description = "User Id", example = "125")
  private Long userId;
}
