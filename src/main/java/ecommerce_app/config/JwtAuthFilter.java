package ecommerce_app.config;

import ecommerce_app.service.impl.UserDetailServiceImpl;
import ecommerce_app.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    log.info("Request URL : {}", request.getRequestURI());
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    try {
      if (authHeader != null
          && (authHeader.startsWith("bearer ") || authHeader.startsWith("Bearer "))) {
        String token = authHeader.substring(7);
        String subject = jwtService.getSubject(token);

        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          Long userId = Long.parseLong(subject);
          UserDetails userDetails = userDetailsService.loadUserById(userId);
          if (jwtService.isValidToken(token)) {
            Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
      }

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator");
  }
}
