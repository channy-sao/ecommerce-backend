package ecommerce_app.service;

import ecommerce_app.dto.request.ResetPasswordRequest;
import ecommerce_app.dto.request.VerifyOtpRequest;
import ecommerce_app.dto.response.VerifyOtpResponse;

public interface PasswordResetService {
  void sendOtp(String email);

  VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

  void resetPassword(ResetPasswordRequest request);
}
