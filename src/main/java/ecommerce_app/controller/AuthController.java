package ecommerce_app.controller;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.request.ResetPasswordRequest;
import ecommerce_app.dto.request.SendOtpRequest;
import ecommerce_app.dto.request.VerifyOtpRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.LoginRequest;
import ecommerce_app.dto.request.RefreshTokenRequest;
import ecommerce_app.dto.request.SignupRequest;
import ecommerce_app.dto.response.LoginResponse;
import ecommerce_app.dto.response.RefreshTokenResponse;
import ecommerce_app.dto.response.VerifyOtpResponse;
import ecommerce_app.service.AuthenticationService;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.service.PasswordResetService;
import ecommerce_app.util.MessageSourceService;
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
  private final MessageSourceService messageSourceService;
  private final PasswordResetService passwordResetService;

  @PostMapping("/login/firebase")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> loginWithFirebase(
      @RequestHeader(value = "idToken") String idToken) {
    return BaseBodyResponse.success(
        authenticationService.loginWithFirebase(idToken),
        messageSourceService.getMessage(MessageKeyConstant.AUTH_MESSAGE_LOGIN_SUCCESS));
  }

  @PostMapping("/signup")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> signup(
      @RequestBody @Valid SignupRequest signupRequest) {
    return BaseBodyResponse.success(
        authenticationService.signupWithFirebase(signupRequest),
        messageSourceService.getMessage(MessageKeyConstant.AUTH_MESSAGE_REGISTER_SUCCESS));
  }

  @PostMapping("/login/local")
  public ResponseEntity<BaseBodyResponse<LoginResponse>> loginLocal(
      @RequestBody @Valid LoginRequest loginRequest) {
    return BaseBodyResponse.success(
        authenticationService.loginLocal(loginRequest),
        messageSourceService.getMessage(MessageKeyConstant.AUTH_MESSAGE_LOGIN_SUCCESS));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<BaseBodyResponse<RefreshTokenResponse>> refreshToken(
      @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
    return BaseBodyResponse.success(
        authenticationService.refreshToken(refreshTokenRequest.getRefreshToken()),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/logout")
  public ResponseEntity<BaseBodyResponse<Void>> logout() {
    this.authenticationService.logout();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.AUTH_MESSAGE_LOGOUT_SUCCESS));
  }

  @GetMapping("/me")
  public ResponseEntity<BaseBodyResponse<UserResponse>> getCurrentUser() {
    return BaseBodyResponse.success(
        authenticationService.getCurrentUser(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  // Step 1 — request OTP
  @PostMapping("/forgot-password")
  public ResponseEntity<BaseBodyResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
    passwordResetService.sendOtp(request.getEmail());
    return BaseBodyResponse.success(
            "If this email is registered, a reset code has been sent");
  }

  // Step 2 — verify OTP → get reset token
  @PostMapping("/verify-otp")
  public ResponseEntity<BaseBodyResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    return BaseBodyResponse.success(
            passwordResetService.verifyOtp(request),
            "OTP verified successfully");
  }

  // Step 3 — reset password using reset token
  @PostMapping("/reset-password")
  public ResponseEntity<BaseBodyResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request);
    return BaseBodyResponse.success("Password reset successfully");
  }
}
