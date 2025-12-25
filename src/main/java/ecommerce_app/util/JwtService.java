package ecommerce_app.util;

import ecommerce_app.infrastructure.property.AppProperty;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final String issuer;
  private final AppProperty appProperty;

  public JwtService(AppProperty appProperty) {
    this.appProperty = appProperty;
    this.issuer = appProperty.getAppName();
  }

  public String generateAccessToken(CustomUserDetails userDetails) {
    // Extract authorities from CustomUserDetails
    List<String> authorities =
        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    return Jwts.builder()
        .setIssuer(appProperty.getAppName())
        .setSubject(userDetails.getUsername())
        .claim("authorities", authorities)
        .setIssuedAt(new Date())
        .setExpiration(
            Date.from(
                Instant.now()
                    .plus(appProperty.getJwt().getAccessExpiredInMinute(), ChronoUnit.MINUTES)))
        .signWith(getKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject, boolean rememberMe) {
    var expirationAt =
        rememberMe
            ? Date.from(Instant.now().plus(30, ChronoUnit.DAYS))
            : Date.from(
                Instant.now()
                    .plus(appProperty.getJwt().getRefreshExpiredInMinute(), ChronoUnit.DAYS));
    return Jwts.builder()
        .setIssuer(issuer)
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(expirationAt)
        .signWith(getKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isValidToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getSubject(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();

      return claims.getSubject();
    } catch (JwtException | IllegalArgumentException e) {
      return null;
    }
  }

  public Long getAccessExpirationMs() {
    return appProperty.getJwt().getAccessExpiredInMinute() * 60 * 1000;
  }

  public Long getRefreshExpirationMs() {
    return appProperty.getJwt().getAccessExpiredInMinute() * 86400 * 1000;
  }

  public List<String> extractAuthorities(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();

      return claims.get("authorities", List.class);
    } catch (JwtException | IllegalArgumentException e) {
      return List.of();
    }
  }

  private Key getKey() {
    return Keys.hmacShaKeyFor(appProperty.getJwt().getSecretKey().getBytes());
  }
}
