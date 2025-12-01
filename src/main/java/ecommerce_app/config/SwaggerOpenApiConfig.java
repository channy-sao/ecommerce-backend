package ecommerce_app.config;

import ecommerce_app.constant.app.AppConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOpenApiConfig {
  @Bean
  public OpenAPI customize() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Small E-Commerce")
                .version("1.0")
                .description("Some description of Small E-Commerce API")
                .contact(
                    new Contact()
                        .name("Channy SAO")
                        .email("channy.sao@gmail.com")
                        .url("https://github.com/channy/sao"))
                .license(new License().name("Licence of API").url("API license URL")))
        .addSecurityItem(
            new SecurityRequirement().addList(AppConstant.JWT_SCHEME_NAME)) // Add JWT
        .components(
            new Components().addSecuritySchemes(AppConstant.JWT_SCHEME_NAME, jwtSecurityScheme()));
  }

  /**
   * Defines the JWT security scheme for API authentication.
   *
   * @return A SecurityScheme instance configured for JWT authentication.
   */
  private SecurityScheme jwtSecurityScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme(AppConstant.JWT_SCHEME)
        .bearerFormat(AppConstant.JWT_BEARER_FORMAT)
        .description("E-Commerce security with JWT")
        .in(SecurityScheme.In.HEADER);
  }
}
