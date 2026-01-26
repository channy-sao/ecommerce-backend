package ecommerce_app.config;

import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.auth.service.impl.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security. It enables web security and configures the {@link
 * SecurityFilterChain} to handle JWT-based authentication.
 */
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDenied customAccessDenied;
  private final JwtAuthFilter jwtAuthFilter;
  private final TraceFilter traceFilter;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(Customizer.withDefaults()) // Enable CSRF protection with default settings
        .cors(Customizer.withDefaults())
        // disable csrf filter
        .csrf(AbstractHttpConfigurer::disable)
        // disable logout filter
        .logout(AbstractHttpConfigurer::disable)
        // disable default login page
        .formLogin(AbstractHttpConfigurer::disable)
        // disable request cache aware filter
        .requestCache(AbstractHttpConfigurer::disable)
        // disable http basic
        .httpBasic(AbstractHttpConfigurer::disable) // Enable CORS with default
        .authorizeHttpRequests(
            authorizationRequest ->
                authorizationRequest
                    .requestMatchers(
                        "/api/v1/auth/login/**",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/refresh-token")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/me", "/api/admin/v1/**", "/api/client/v1/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //        .authenticationProvider(authenticationProvider())
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDenied))
        .addFilterBefore(traceFilter, JwtAuthFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
