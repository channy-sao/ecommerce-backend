package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Store location and contact information")
public class StoreLocation {

  @Schema(
      description = "Physical address of the store",
      example = "123 Main St, Phnom Penh, Cambodia")
  private String address;

  @Schema(description = "Latitude coordinate of the store", example = "11.5564")
  private String latitude;

  @Schema(description = "Longitude coordinate of the store", example = "104.9282")
  private String longitude;

  @Schema(description = "Store phone number", example = "+855 23 123 456")
  private String phone;

  @Schema(description = "Store email address", example = "store@example.com")
  private String email;

  @Schema(description = "Store Telegram handle or link", example = "@mystore")
  private String telegram;

  @Schema(description = "Store opening time", example = "08:00")
  private String storeOpenAt;

  @Schema(description = "Store closing time", example = "21:00")
  private String storeCloseAt;

  @Schema(description = "Store website URL", example = "https://mystore.com")
  private String website;

  @Schema(
      description = "Store Facebook page URL or handle",
      example = "https://facebook.com/mystore")
  private String facebook;
}
