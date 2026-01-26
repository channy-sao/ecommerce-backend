package ecommerce_app.api;

import ecommerce_app.infrastructure.model.response.LocaleInfo;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/localization")
@Tag(name = "Internationalization", description = "APIs for Internationalization and Localization")
public class I18nController {

  private final MessageSourceService messageService;

  public I18nController(MessageSourceService messageService) {
    this.messageService = messageService;
  }

  @GetMapping("/greeting")
  public ResponseEntity<BaseBodyResponse> greeting() {
    String message = messageService.getMessage("greeting.message");
    return BaseBodyResponse.success(message, "Successfully retrieved greeting message");
  }

  @GetMapping("/welcome/{name}")
  public ResponseEntity<BaseBodyResponse> welcome(@PathVariable String name) {
    String message = messageService.getMessage("welcome.message", new Object[] {name});
    return BaseBodyResponse.success(message, "Successfully retrieved welcome message");
  }

  @GetMapping("/locale-info")
  public ResponseEntity<LocaleInfo> getLocaleInfo() {
    Locale currentLocale = LocaleContextHolder.getLocale();
    LocaleInfo info =
        new LocaleInfo(
            currentLocale.getLanguage(),
            currentLocale.getDisplayLanguage(),
            messageService.getMessage("locale.info"));
    return ResponseEntity.ok(info);
  }
}
