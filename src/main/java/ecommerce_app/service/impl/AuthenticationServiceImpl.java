package ecommerce_app.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import ecommerce_app.constant.app.TokenTypeConstant;
import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.core.SimpleTry;
import ecommerce_app.core.identify.custom.AuthUserLoader;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.CompletePhoneProfileRequest;
import ecommerce_app.dto.request.LoginRequest;
import ecommerce_app.dto.request.PhoneLoginRequest;
import ecommerce_app.dto.request.SignupRequest;
import ecommerce_app.dto.response.LoginResponse;
import ecommerce_app.dto.response.PhoneLoginResponse;
import ecommerce_app.dto.response.RefreshTokenResponse;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.UnauthorizedException;
import ecommerce_app.mapper.UserMapper;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.AuthenticationService;
import ecommerce_app.util.JwtService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
  private final UserMapper userMapper;
  private final AuthUserLoader authUserLoader;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public LoginResponse loginWithFirebase(String idToken) {
    return this.verifyAndGenerateToken(idToken, null, null);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Core Firebase token verifier — handles ALL sign_in_provider values
  // ─────────────────────────────────────────────────────────────────────────────

  private LoginResponse verifyAndGenerateToken(String idToken, String firstName, String lastName) {
    try {
      log.info("Verifying Firebase idToken");

      FirebaseToken decodedToken =
              SimpleTry.ofReThrowChecked(
                      () -> FirebaseAuth.getInstance().verifyIdToken(idToken),
                      throwable -> {
                        log.error("Firebase token verification failed", throwable);
                        throw new BadRequestException("Firebase idToken verification failed");
                      });

      final String uid = decodedToken.getUid();
      final Map<String, Object> claims  = decodedToken.getClaims();
      final Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
      final String signInProvider = String.valueOf(firebase.get("sign_in_provider"));

      // ── Route to phone handler ────────────────────────────────────────────
      if ("phone".equals(signInProvider)) {
        return verifyAndGeneratePhoneToken(decodedToken, uid, signInProvider);
      }

      // ── Email / Google / Facebook handler (original logic) ────────────────
      final String email   = decodedToken.getEmail();
      final String name    = decodedToken.getName();
      final String picture = decodedToken.getPicture();

      if (StringUtils.isNoneEmpty(name)) {
        firstName = name.substring(0, name.indexOf(" "));
        lastName  = name.substring(name.indexOf(" ") + 1);
      }

      User userBuilder = User.builder()
              .email(email)
              .firebaseUid(uid)
              .avatar(picture)
              .firstName(firstName == null ? "" : firstName)
              .lastName(lastName == null ? "" : lastName)
              .phone(null)
              .password(null)
              .rememberMe(true)
              .emailVerifiedAt(LocalDateTime.now())
              .isActive(true)
              .uuid(UUID.randomUUID())
              .authProvider(AuthProvider.fromProviderString(signInProvider))
              .lastLoginAt(LocalDateTime.now())
              .build();

      User user = this.createOrDefault(userBuilder);
      log.info("User created or default: {}", user);

      userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

      CustomUserDetails userDetails = authUserLoader.loadByEmail(user.getEmail());

      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setAccessToken(jwtService.generateAccessToken(userDetails));
      loginResponse.setRefreshToken(jwtService.generateRefreshToken(user.getEmail(), false));
      loginResponse.setTokenType(TokenTypeConstant.BEARER);
      loginResponse.setAccessTokenExpireInMs(jwtService.getAccessExpirationMs());
      loginResponse.setRefreshTokenExpireInMs(jwtService.getRefreshExpirationMs());
      loginResponse.setUserInfo(userMapper.toUserResponse(user));

      log.info("Firebase login response ready for uid={}", uid);
      return loginResponse;

    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new UnauthorizedException("Unauthorized");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Phone OTP handler (called from verifyAndGenerateToken)
  // ─────────────────────────────────────────────────────────────────────────────

  private PhoneLoginResponse verifyAndGeneratePhoneToken(
          FirebaseToken decodedToken, String uid, String signInProvider) {

    final Map<String, Object> claims = decodedToken.getClaims();
    final String phone = (String) claims.get("phone_number");

    if (phone == null || phone.isBlank()) {
      throw new BadRequestException("No phone number found in Firebase token");
    }

    log.info("Phone login for uid={}, phone={}", uid, phone);

    boolean isNew = !userRepository.existsByPhone(phone);

    User user = userRepository.findByPhone(phone).orElseGet(() ->
            userRepository.save(User.builder()
                    .phone(phone)
                    .firebaseUid(uid)
                    // Placeholder satisfies the UNIQUE NOT NULL email constraint.
                    // Format is deterministic so it can never clash with a real email.
                    .email("phone_" + uid + "@phone.placeholder")
                    .firstName("")
                    .lastName("")
                    .password(null)
                    .rememberMe(true)
                    .isActive(true)
                    .uuid(UUID.randomUUID())
                    .authProvider(AuthProvider.fromProviderString(signInProvider)) // → PHONE
                    .lastLoginAt(LocalDateTime.now())
                    .build())
    );

    userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

    CustomUserDetails userDetails = authUserLoader.loadByFirebaseUid(uid);

    PhoneLoginResponse response = new PhoneLoginResponse();
    response.setAccessToken(jwtService.generateAccessToken(userDetails));
    response.setRefreshToken(jwtService.generateRefreshToken(user.getEmail(), false));
    response.setTokenType(TokenTypeConstant.BEARER);
    response.setAccessTokenExpireInMs(jwtService.getAccessExpirationMs());
    response.setRefreshTokenExpireInMs(jwtService.getRefreshExpirationMs());
    response.setUserInfo(userMapper.toUserResponse(user));
    response.setProfileIncomplete(isNew); // frontend shows "complete profile" form if true

    log.info("Phone login success uid={}, isNew={}", uid, isNew);
    return response;
  }


  // ─────────────────────────────────────────────────────────────────────────────
  // Phone — complete profile (new users only)
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void completePhoneProfile(CompletePhoneProfileRequest request) {
    UserResponse currentUserResponse = getCurrentUser();

    User user = userRepository.findByEmail(currentUserResponse.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getAuthProvider() != AuthProvider.PHONE_NUMBER) {
      throw new BadRequestException("Only phone-authenticated users can use this endpoint");
    }

    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());

    if (StringUtils.isNotBlank(request.getEmail())) {
      if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
        throw new BadRequestException("Email is already in use");
      }
      user.setEmail(request.getEmail());
    }

    userRepository.save(user);
    log.info("Profile completed for phone user id={}", user.getId());
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // loginWithPhone — thin delegate, detection happens inside verifyAndGenerateToken
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public PhoneLoginResponse loginWithPhone(PhoneLoginRequest request) {
    return (PhoneLoginResponse) this.verifyAndGenerateToken(request.getIdToken(), null, null);
  }

  @Transactional
  @Override
  public LoginResponse signupWithFirebase(SignupRequest signupRequest) {
    return this.verifyAndGenerateToken(
        signupRequest.getIdToken(), signupRequest.getFirstName(), signupRequest.getLastName());
  }


  // ─────────────────────────────────────────────────────────────────────────────
  // Local email/password login
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public LoginResponse loginLocal(LoginRequest loginRequest) {
    try {
      log.info("Login local with email: {}", loginRequest.getEmail());
      String email = loginRequest.getEmail().trim(); // remove leading/trailing spaces
      String password = loginRequest.getPassword().trim(); // remove leading/trailing spaces
      final Authentication authentication =
              authenticationManager.authenticate(
                      new UsernamePasswordAuthenticationToken(email, password));
      if (authentication.isAuthenticated()) {
        log.info("Authentication is authenticated");
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Update last login here
        userRepository.updateLastLogin(userDetails.getId(), LocalDateTime.now());

        String refreshToken =
                jwtService.generateRefreshToken(userDetails.getUsername(), loginRequest.isRememberMe());

        String accessToken = jwtService.generateAccessToken(userDetails);
        var loggedInUser = this.getCurrentUser();
        return getLoginResponse(loggedInUser, accessToken, refreshToken);
      }
      log.error("Username or password is incorrect");
      throw new UnauthorizedException("Username or Password incorrect");
    } catch (Exception ex){
      log.error(ex.getMessage(), ex);
      throw new UnauthorizedException(ex.getMessage());
    }
  }

  @Override
  public RefreshTokenResponse refreshToken(String refreshToken) {
    log.info("Refresh Token Calling");
    String subject = jwtService.getSubject(refreshToken);
    CustomUserDetails userDetails = authUserLoader.loadByEmail(subject);

    final String accessToken = jwtService.generateAccessToken(userDetails);
    final String generatedRefreshToken = jwtService.generateRefreshToken(subject, false);
    log.info("Refresh token: {}", generatedRefreshToken);
    return RefreshTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(generatedRefreshToken)
        .tokenType(TokenTypeConstant.BEARER)
        .accessTokenExpireInMs(jwtService.getAccessExpirationMs())
        .refreshTokenExpireInMs(jwtService.getRefreshExpirationMs())
        .build();
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponse getCurrentUser() {
    log.info("Start Fetch Current User  {'/me'}");
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      log.error("Authentication Failed");
      throw new UnauthorizedException("Unauthorized");
    }
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    if (userDetails == null) {
      log.error("Authentication Failed, user details is null");
      throw new UnauthorizedException("Unauthorized");
    }
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
    loginResponse.setRefreshTokenExpireInMs(jwtService.getRefreshExpirationMs());
    loginResponse.setAccessTokenExpireInMs(jwtService.getAccessExpirationMs());
    loginResponse.setUserInfo(user);
    return loginResponse;
  }
}
