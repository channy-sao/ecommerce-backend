package ecommerce_app.service;

import ecommerce_app.dto.request.LoginRequest;
import ecommerce_app.dto.request.SignupRequest;
import ecommerce_app.dto.response.LoginResponse;
import ecommerce_app.dto.response.RefreshTokenResponse;
import ecommerce_app.dto.response.UserResponse;

public interface AuthenticationService {
  LoginResponse loginWithFirebase(String idToken);

  LoginResponse signupWithFirebase(SignupRequest signupRequest);

  LoginResponse loginLocal(LoginRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);

  UserResponse getCurrentUser();

  void logout();
}
