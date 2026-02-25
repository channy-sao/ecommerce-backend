package ecommerce_app.dto.response;

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
public class StoreLocation {
  private String address;
  private String latitude;
  private String longitude;
  private String phone;
  private String email;
  private String telegram;
  private String storeOpenAt;
  private String storeCloseAt;
  private String website;
  private String facebook;
}
