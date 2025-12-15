package ecommerce_app.modules.auth.service;

import ecommerce_app.modules.auth.dto.request.LoginRequest;
import ecommerce_app.modules.auth.dto.request.SignupRequest;
import ecommerce_app.modules.auth.dto.response.LoginResponse;
import ecommerce_app.modules.auth.dto.response.RefreshTokenResponse;
import ecommerce_app.modules.user.model.dto.UserResponse;

public interface AuthenticationService {
  LoginResponse loginWithFirebase(String idToken);

  LoginResponse signupWithFirebase(SignupRequest signupRequest);

  LoginResponse loginLocal(LoginRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);

  UserResponse getCurrentUser();

  void logout();
}
