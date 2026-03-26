package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Locale information response")
public class LocaleInfo {

  @Schema(description = "ISO language code", example = "en")
  private String languageCode;

  @Schema(description = "Human-readable language name", example = "English")
  private String languageName;

  @Schema(description = "Locale-specific message or label", example = "Welcome")
  private String localeMessage;

  public LocaleInfo(String languageCode, String languageName, String localeMessage) {
    this.languageCode = languageCode;
    this.languageName = languageName;
    this.localeMessage = localeMessage;
  }
}
