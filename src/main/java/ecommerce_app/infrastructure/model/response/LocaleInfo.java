package ecommerce_app.infrastructure.model.response;

public class LocaleInfo {
  private String languageCode;
  private String languageName;
  private String localeMessage;

  public LocaleInfo(String languageCode, String languageName, String localeMessage) {
    this.languageCode = languageCode;
    this.languageName = languageName;
    this.localeMessage = localeMessage;
  }

  // Getters and Setters
  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public String getLanguageName() {
    return languageName;
  }

  public void setLanguageName(String languageName) {
    this.languageName = languageName;
  }

  public String getLocaleMessage() {
    return localeMessage;
  }

  public void setLocaleMessage(String localeMessage) {
    this.localeMessage = localeMessage;
  }
}
