package ecommerce_app.infrastructure.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.i18n")
@Validated
public class I18nProperties {

  // Getters and Setters
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
}
