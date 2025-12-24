package ecommerce_app.util;

import ecommerce_app.modules.user.model.entity.User;
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
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final String SECRET_KEY = "cuQMzcVqFdJ2nmAkskDlCzuv21mqGUcZ";
  private static final String ISSUER = "ecommerce_app";
  public static final int ACCESS_TOKEN_VALIDITY_MINUTES = 1;
  public static final int REFRESH_TOKEN_VALIDITY_DAYS = 90;

  public String generateAccessToken(User user) {

    return Jwts.builder()
        .setIssuer(ISSUER)
        .setSubject(user.getEmail())
        .claim("authorities", getAuthorities(user))
        .setIssuedAt(new Date())
        .setExpiration(
            Date.from(Instant.now().plus(ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES)))
        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject, boolean rememberMe) {
      var expirationAt = rememberMe? Date.from(Instant.now().plus(30, ChronoUnit.DAYS)):  Date.from(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS));
    return Jwts.builder()
        .setIssuer(ISSUER)
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

  public List<String> getAuthorities(User user) {

    // Flatten roles and permissions
    return user.getRoles().stream()
        .flatMap(
            role -> {
              Stream<String> roleStream = Stream.of("ROLE_" + role.getName());
              Stream<String> permissionStream =
                  role.getPermissions().stream().map(p -> "PERMISSION_" + p.getName());
              return Stream.concat(roleStream, permissionStream);
            })
        .toList();
  }

  private Key getKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }
}
