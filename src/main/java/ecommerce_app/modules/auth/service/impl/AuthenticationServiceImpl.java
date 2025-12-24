package ecommerce_app.modules.auth.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import ecommerce_app.constant.TokenTypeConstant;
import ecommerce_app.infrastructure.exception.UnauthorizedException;
import ecommerce_app.infrastructure.mapper.UserMapper;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.auth.dto.request.LoginRequest;
import ecommerce_app.modules.auth.dto.request.SignupRequest;
import ecommerce_app.modules.auth.dto.response.LoginResponse;
import ecommerce_app.modules.auth.dto.response.RefreshTokenResponse;
import ecommerce_app.modules.auth.service.AuthenticationService;
import ecommerce_app.modules.user.model.dto.UserResponse;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import ecommerce_app.util.JwtService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final ModelMapper mapper;
  private final UserMapper userMapper;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public LoginResponse loginWithFirebase(String idToken) {
    return this.verifyAndGenerateToken(idToken, null, null);
  }

  private LoginResponse verifyAndGenerateToken(String idToken, String firstName, String lastName) {
    try {
      log.info("Login with firebase with idToken: {}", idToken);
      FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
      final String email = decodedToken.getEmail();
      final String uid = decodedToken.getUid();
      final String name = decodedToken.getName();

      // extract name from Google login else use from user register or other Google
      if (StringUtils.isNoneEmpty(name)) {
        firstName = name.substring(0, name.indexOf(" "));
        lastName = name.substring(name.indexOf(" ") + 1);
      }

      final String picture = decodedToken.getPicture();
      final Map<String, Object> claims = decodedToken.getClaims();
      Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
      final var signInProvider = String.valueOf(firebase.get("sign_in_provider"));

      // prepare user
      User userBuilder =
          User.builder()
              .email(email)
              .uid(uid)
              .avatar(picture)
              .firstName(firstName == null ? "" : firstName)
              .lastName(lastName == null ? "" : lastName)
              .phone(null)
              .password(null)
              .rememberMe(true)
              .emailVerifiedAt(LocalDateTime.now())
              .isActive(true)
              .uuid(UUID.randomUUID())
              .provider(signInProvider)
              .lastLoginAt(LocalDateTime.now())
              .build();

      // create user if not exist
      User user = this.createOrDefault(userBuilder);
      log.info("User created or default : {}", user);

      var userResponse = userMapper.toUserResponse(user);
      // generate access and refresh token
      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setAccessToken(jwtService.generateAccessToken(user));
      loginResponse.setRefreshToken(jwtService.generateRefreshToken(user.getEmail(), false));
      loginResponse.setTokenType(TokenTypeConstant.BEARER);
      loginResponse.setAccessTokenExpireInMs(JwtService.ACCESS_TOKEN_VALIDITY_MINUTES * 60 * 1000);
      loginResponse.setRefreshTokenExpireInMs(
          (long) JwtService.REFRESH_TOKEN_VALIDITY_DAYS * 86400 * 1000);
      loginResponse.setTokenType("Bearer");
      loginResponse.setUserInfo(userResponse);

      log.info("Login response: {}", loginResponse);
      return loginResponse;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new UnauthorizedException("Unauthorized");
    }
  }

  @Override
  public LoginResponse signupWithFirebase(SignupRequest signupRequest) {
    return this.verifyAndGenerateToken(
        signupRequest.getIdToken(), signupRequest.getFirstName(), signupRequest.getLastName());
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public LoginResponse loginLocal(LoginRequest loginRequest) {
    log.info("Login local with email: {}", loginRequest.getEmail());
    final Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));
    if (authentication.isAuthenticated()) {
      log.info("Authentication is authenticated");
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      SecurityContextHolder.getContext().setAuthentication(authentication);
      final var user =
          userRepository
              .findByEmail(loginRequest.getEmail())
              .orElseThrow(() -> new UsernameNotFoundException(loginRequest.getEmail()));

      String refreshToken =
          jwtService.generateRefreshToken(userDetails.getUsername(), loginRequest.isRememberMe());
      String accessToken = jwtService.generateAccessToken(user);
      var loggedInUser = this.getCurrentUser();
      return getLoginResponse(loggedInUser, accessToken, refreshToken);
    }
    throw new UnauthorizedException("Username or Password incorrect");
  }

  @Override
  public RefreshTokenResponse refreshToken(String refreshToken) {
    log.info("Refresh Token Calling");
    String subject = jwtService.getSubject(refreshToken);
    final var user =
        userRepository
            .findByEmail(subject)
            .orElseThrow(() -> new UsernameNotFoundException(subject));
    final String accessToken = jwtService.generateAccessToken(user);
    final String generatedRefreshToken = jwtService.generateRefreshToken(subject, false);
    log.info("Refresh token: {}", generatedRefreshToken);
    return RefreshTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(generatedRefreshToken)
        .tokenType(TokenTypeConstant.BEARER)
        .accessTokenExpireInMs(JwtService.ACCESS_TOKEN_VALIDITY_MINUTES * 60 * 1000)
        .refreshTokenExpireInMs((long) JwtService.REFRESH_TOKEN_VALIDITY_DAYS * 86400 * 1000)
        .build();
  }

  @Override
  public UserResponse getCurrentUser() {
    log.info("Start Fetch Current User  {'/me'}");
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!authentication.isAuthenticated()) {
      throw new UnauthorizedException("Unauthorized");
    }
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    User currentUser =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return userMapper.toUserResponse(currentUser);
  }

  @Override
  public void logout() {
    SecurityContextHolder.clearContext();
    log.info("Logout successful");
  }

  public User createOrDefault(User user) {
    return userRepository.findByEmail(user.getEmail()).orElseGet(() -> userRepository.save(user));
  }

  private LoginResponse getLoginResponse(
      UserResponse user, String accessToken, String refreshToken) {
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setAccessToken(accessToken);
    loginResponse.setRefreshToken(refreshToken);
    loginResponse.setTokenType(TokenTypeConstant.BEARER);
    loginResponse.setRefreshTokenExpireInMs(
        (long) JwtService.REFRESH_TOKEN_VALIDITY_DAYS * 86400 * 1000);
    loginResponse.setAccessTokenExpireInMs(
        (long) JwtService.ACCESS_TOKEN_VALIDITY_MINUTES * 60 * 1000);
    loginResponse.setUserInfo(user);
    return loginResponse;
  }
}
