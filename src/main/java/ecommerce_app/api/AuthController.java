package ecommerce_app.api;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.dto.request.LoginRequest;
import ecommerce_app.modules.auth.dto.request.RefreshTokenRequest;
import ecommerce_app.modules.auth.dto.request.SignupRequest;
import ecommerce_app.modules.auth.dto.response.LoginResponse;
import ecommerce_app.modules.auth.dto.response.RefreshTokenResponse;
import ecommerce_app.modules.auth.service.AuthenticationService;
import ecommerce_app.modules.user.model.dto.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "For Management Authentication Login/Logout")
public class AuthController {
  private final AuthenticationService authenticationService;

  @PostMapping("/login/firebase")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> loginWithFirebase(
      @RequestHeader(value = "idToken") String idToken) {
    return BaseBodyResponse.success(
        authenticationService.loginWithFirebase(idToken), "Login successful");
  }

  @PostMapping("/signup")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> signup(
      @RequestBody @Valid SignupRequest signupRequest) {
    return BaseBodyResponse.success(
        authenticationService.signupWithFirebase(signupRequest), "Signup successful");
  }

  @PostMapping("/login/local")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> loginLocal(
      @RequestBody @Valid LoginRequest loginRequest) {
    return BaseBodyResponse.success(
        authenticationService.loginLocal(loginRequest), "Login successful");
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<BaseBodyResponse<RefreshTokenResponse>> refreshToken(
      @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
    return BaseBodyResponse.success(
        authenticationService.refreshToken(refreshTokenRequest.getRefreshToken()),
        "Refresh Token successful");
  }

  @PostMapping("/logout")
  public ResponseEntity<BaseBodyResponse<Void>> logout() {
    this.authenticationService.logout();
    return BaseBodyResponse.success("Logout successful");
  }

  @GetMapping("/me")
  public ResponseEntity<BaseBodyResponse<UserResponse>> getCurrentUser() {
    return BaseBodyResponse.success(
        authenticationService.getCurrentUser(), "Current user successful");
  }
}
