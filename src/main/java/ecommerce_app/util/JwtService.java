package ecommerce_app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final String SECRET_KEY = "cuQMzcVqFdJ2nmAkskDlCzuv21mqGUcZ";
  private static final String ISSUER = "ecommerce_app";
  private static final int ACCESS_TOKEN_VALIDITY_MINUTES = 60;
  private static final int REFRESH_TOKEN_VALIDITY_DAYS = 90;

  public String generateAccessToken(String subject) {
    return Jwts.builder()
        .setIssuer(ISSUER)
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(
            Date.from(Instant.now().plus(ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES)))
        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject) {
    return Jwts.builder()
        .setIssuer(ISSUER)
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(Date.from(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS)))
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

  private Key getKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }
}
