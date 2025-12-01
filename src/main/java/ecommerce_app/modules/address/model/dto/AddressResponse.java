package ecommerce_app.modules.address.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "Address response DTO containing full address information")
public class AddressResponse {

  @Schema(description = "Unique identifier of the address", example = "1")
  private Long id;

  @Schema(description = "Street name", example = "123 Main St")
  private String street;

  @Schema(description = "City name", example = "Phnom Penh")
  private String city;

  @Schema(description = "State or province", example = "Phnom Penh")
  private String state;

  @Schema(description = "ZIP or postal code", example = "12345")
  private String zip;

  @Schema(description = "Country name", example = "Cambodia")
  private String country;

  @Schema(description = "Latitude coordinate", example = "11.5564")
  private double latitude;

  @Schema(description = "Longitude coordinate", example = "104.9282")
  private double longitude;

  @Schema(description = "Indicates if this is the default address", example = "true")
  private boolean isDefault;

  @Schema(description = "First line of the address", example = "Building A, Room 101")
  private String line1;

  @Schema(description = "Second line of the address", example = "Street 271")
  private String line2;

  @Schema(description = "Postal code (alternative to ZIP)", example = "12000")
  private String postalCode;

  @Schema(description = "ID of the user this address belongs to", example = "42")
  private Long userId;
}
