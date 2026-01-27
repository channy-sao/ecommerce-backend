package ecommerce_app.util;

import ecommerce_app.infrastructure.property.AppProperty;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@Slf4j
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
        .issuer(appProperty.getAppName())
        .subject(userDetails.getUsername())
        .claim("authorities", authorities)
        .issuedAt(new Date())
        .expiration(
            Date.from(
                Instant.now()
                    .plus(appProperty.getJwt().getAccessExpiredInMinute(), ChronoUnit.MINUTES)))
        .signWith(getSignInKey(), Jwts.SIG.HS256)
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
        .issuer(issuer)
        .subject(subject)
        .issuedAt(new Date())
        .expiration(expirationAt)
        .signWith(getSignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  /** Validate token - check if token is expired */
  public boolean isValidToken(String token) {
    try {
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /** Extract subject (username) from token - UPDATED API */
  public String getSubject(String token) {
    try {
      Claims claims = extractAllClaims(token);
      return claims.getSubject();
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Failed to extract subject from token: {}", e.getMessage());
      return null;
    }
  }

  /** Get access token expiration time in milliseconds */
  public Long getAccessExpirationMs() {
    return appProperty.getJwt().getAccessExpiredInMinute() * 60 * 1000;
  }

  /** Get refresh token expiration time in milliseconds */
  public Long getRefreshExpirationMs() {
    return appProperty.getJwt().getAccessExpiredInMinute() * 86400 * 1000;
  }

  /** Get signing key from secret */
  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(appProperty.getJwt().getSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /** Check if token is expired */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /** Extract expiration date from token */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /** Extract all claims from token - UPDATED METHOD (No Deprecation) */
  private Claims extractAllClaims(String token) {
    return Jwts.parser() // Updated from parserBuilder()
        .verifyWith(getSignInKey()) // Updated API
        .build()
        .parseSignedClaims(token) // Updated from parseClaimsJws()
        .getPayload(); // Updated from getBody()
  }

  /** Extract username from token */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /** Extract a specific claim from token */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }
}
