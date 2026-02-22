package ecommerce_app.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LocaleInfo {
    // Getters and Setters
    private String languageCode;
  private String languageName;
  private String localeMessage;

  public LocaleInfo(String languageCode, String languageName, String localeMessage) {
    this.languageCode = languageCode;
    this.languageName = languageName;
    this.localeMessage = localeMessage;
  }

}
