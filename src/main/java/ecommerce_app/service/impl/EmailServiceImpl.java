package ecommerce_app.service.impl;

import ecommerce_app.exception.InternalServerErrorException;
import ecommerce_app.property.MailProperties;
import ecommerce_app.service.EmailService;
import ecommerce_app.util.EmailTemplateBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final MailProperties mailProperties;
  private final EmailTemplateBuilder templateBuilder;

  // ── Core send method ──────────────────────────────────────────────────────
  private void send(String to, String subject, String htmlBody) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(new InternetAddress(mailProperties.getFrom(), mailProperties.getFromName()));
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true); // true = html

      mailSender.send(message);
      log.info("Email sent to: {} | Subject: {}", to, subject);

    } catch (MessagingException | UnsupportedEncodingException e) {
      log.error("Failed to send email to: {} | Error: {}", to, e.getMessage(), e);
      throw new InternalServerErrorException("Failed to send email", e);
    }
  }

  // ── Templates ─────────────────────────────────────────────────────────────

  @Override
  public void sendOtpEmail(String to, String name, String otp) {
    send(
        to,
        "Your Password Reset Code — " + mailProperties.getFromName(),
        templateBuilder.buildOtpEmail(name, otp));
  }

  @Override
  public void sendWelcomeEmail(String to, String name) {
    send(
        to,
        "Welcome to " + mailProperties.getFromName() + "! 🎉",
        templateBuilder.buildWelcomeEmail(name));
  }

  @Override
  public void sendOrderConfirmationEmail(
      String to, String name, String orderNumber, String total, String shippingAddress) {
    send(
        to,
        "Order Confirmed — " + orderNumber,
        templateBuilder.buildOrderConfirmationEmail(name, orderNumber, total, shippingAddress));
  }

  @Override
  public void sendOrderStatusEmail(
      String to, String name, String orderNumber, String status, String message) {
    send(
        to,
        "Order Update — " + orderNumber + " is now " + status,
        templateBuilder.buildOrderStatusEmail(name, orderNumber, status, message));
  }

  @Override
  public void sendAccountDeactivatedEmail(String to, String name) {
    send(
        to,
        "Your account has been deactivated",
        templateBuilder.buildAccountDeactivatedEmail(name));
  }
}
