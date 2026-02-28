package ecommerce_app.service;

public interface EmailService {
  void sendOtpEmail(String to, String name, String otp);

  void sendWelcomeEmail(String to, String name);

  void sendOrderConfirmationEmail(
      String to, String name, String orderNumber, String total, String shippingAddress);

  void sendOrderStatusEmail(
      String to, String name, String orderNumber, String status, String message);

  void sendAccountDeactivatedEmail(String to, String name);
}
