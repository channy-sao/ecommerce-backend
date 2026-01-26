package ecommerce_app.infrastructure.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;

@ConfigurationProperties(prefix = "app.i18n")
@Validated
public class I18nProperties {

  @NotEmpty private String basename = "i18n/messages";

  @NotNull private Locale defaultLocale = Locale.ENGLISH;

  @NotEmpty
  private List<Locale> supportedLocales =
      List.of(
          Locale.ENGLISH,
          Locale.forLanguageTag("es"),
          Locale.forLanguageTag("fr"),
          Locale.forLanguageTag("km"));

  private String paramName = "lang";

  private Integer cacheSeconds = 3600; // 1 hour in production

  // Getters and Setters
  public String getBasename() {
    return basename;
  }

  public void setBasename(String basename) {
    this.basename = basename;
  }

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  public List<Locale> getSupportedLocales() {
    return supportedLocales;
  }

  public void setSupportedLocales(List<Locale> supportedLocales) {
    this.supportedLocales = supportedLocales;
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public Integer getCacheSeconds() {
    return cacheSeconds;
  }

  public void setCacheSeconds(Integer cacheSeconds) {
    this.cacheSeconds = cacheSeconds;
  }
}
