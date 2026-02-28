package ecommerce_app.util;

import ecommerce_app.property.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailTemplateBuilder {

  private final MailProperties mailProperties;

  // ── Base layout ───────────────────────────────────────────────────────────
  private String wrap(String accentColor, String content) {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Email</title>
            </head>
            <body style="margin:0;padding:0;background:#f4f4f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                      <!-- Header -->
                      <tr>
                        <td style="background:%s;padding:32px 40px;border-radius:12px 12px 0 0;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;letter-spacing:-0.5px;">%s</h1>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="background:#ffffff;padding:40px;border-radius:0 0 12px 12px;">
                          %s
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="padding:24px 40px;text-align:center;">
                          <p style="margin:0;color:#9ca3af;font-size:12px;">
                            © %d %s · All rights reserved<br/>
                            If you did not request this email, you can safely ignore it.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """
        .formatted(
            accentColor,
            mailProperties.getFromName(),
            content,
            java.time.Year.now().getValue(),
            mailProperties.getFromName());
  }

  // ── Greeting helper ───────────────────────────────────────────────────────
  private String greeting(String name) {
    return "<p style='margin:0 0 16px;color:#111827;font-size:16px;'>Hi <strong>%s</strong>,</p>"
        .formatted(name);
  }

  // ── Button helper ─────────────────────────────────────────────────────────
  private String button(String url, String label, String color) {
    return """
            <div style='text-align:center;margin:24px 0;'>
              <a href='%s' style='display:inline-block;background:%s;color:#ffffff;text-decoration:none;
                 padding:14px 32px;border-radius:8px;font-size:15px;font-weight:600;'>%s</a>
            </div>
            """
        .formatted(url, color, label);
  }

  // ── OTP box helper ────────────────────────────────────────────────────────
  private String otpBox(String otp) {
    return """
            <div style='text-align:center;margin:24px 0;'>
              <div style='display:inline-block;background:#f3f4f6;border:2px dashed #d1d5db;
                          border-radius:12px;padding:20px 40px;'>
                <p style='margin:0 0 4px;font-size:12px;color:#6b7280;text-transform:uppercase;
                           letter-spacing:2px;font-weight:600;'>Your Reset Code</p>
                <p style='margin:0;font-size:40px;font-weight:800;letter-spacing:12px;
                           color:#111827;font-family:monospace;'>%s</p>
              </div>
            </div>
            """
        .formatted(otp);
  }

  // ── Info row helper ───────────────────────────────────────────────────────
  private String infoRow(String label, String value) {
    return """
            <tr>
              <td style='padding:10px 0;border-bottom:1px solid #f3f4f6;'>
                <span style='color:#6b7280;font-size:13px;'>%s</span>
              </td>
              <td style='padding:10px 0;border-bottom:1px solid #f3f4f6;text-align:right;'>
                <span style='color:#111827;font-size:13px;font-weight:600;'>%s</span>
              </td>
            </tr>
            """
        .formatted(label, value);
  }

  // ── Alert box helper ──────────────────────────────────────────────────────
  private String alertBox(String message, String bgColor, String borderColor) {
    return """
            <div style='background:%s;border-left:4px solid %s;border-radius:6px;
                        padding:14px 16px;margin:16px 0;'>
              <p style='margin:0;font-size:13px;color:#374151;'>%s</p>
            </div>
            """
        .formatted(bgColor, borderColor, message);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // TEMPLATES
  // ─────────────────────────────────────────────────────────────────────────

  // ── 1. OTP Reset Password ─────────────────────────────────────────────────
  public String buildOtpEmail(String name, String otp) {
    String content =
        greeting(name)
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 16px;'>"
            + "We received a request to reset your password. Use the code below to continue.</p>"
            + otpBox(otp)
            + alertBox(
                "⏱ This code expires in <strong>5 minutes</strong> and can only be used once.",
                "#fffbeb",
                "#f59e0b")
            + alertBox(
                "🔒 If you did not request this, please ignore this email. Your password will not change.",
                "#f0fdf4",
                "#22c55e")
            + "<p style='margin:24px 0 0;color:#6b7280;font-size:13px;text-align:center;'>"
            + "Do not share this code with anyone, including our support team.</p>";

    return wrap("#1d4ed8", content);
  }

  // ── 2. Welcome ────────────────────────────────────────────────────────────
  public String buildWelcomeEmail(String name) {
    String content =
        greeting(name)
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 16px;'>"
            + "Welcome to <strong>"
            + mailProperties.getFromName()
            + "</strong>! "
            + "Your account has been created successfully.</p>"
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 24px;'>"
            + "Start exploring our products and enjoy a seamless shopping experience.</p>"
            + button(mailProperties.getFrontendUrl(), "Start Shopping", "#1d4ed8")
            + alertBox("✅ Your account is now active and ready to use.", "#f0fdf4", "#22c55e");

    return wrap("#059669", content);
  }

  // ── 3. Order Confirmation ─────────────────────────────────────────────────
  public String buildOrderConfirmationEmail(
      String name, String orderNumber, String total, String shippingAddress) {
    String content =
        greeting(name)
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 16px;'>"
            + "Thank you for your order! We have received it and it is now being processed.</p>"
            + "<table width='100%%' cellpadding='0' cellspacing='0' style='margin:0 0 24px;'>"
            + infoRow("Order Number", orderNumber)
            + infoRow("Total Amount", total)
            + infoRow("Shipping To", shippingAddress)
            + infoRow("Status", "Pending")
            + "</table>"
            + button(
                mailProperties.getFrontendUrl() + "/orders/" + orderNumber,
                "Track Your Order",
                "#1d4ed8")
            + alertBox(
                "📦 You will receive another email once your order is shipped.",
                "#eff6ff",
                "#3b82f6");

    return wrap("#1d4ed8", content);
  }

  // ── 4. Order Status Update ────────────────────────────────────────────────
  public String buildOrderStatusEmail(
      String name, String orderNumber, String status, String message) {
    String statusColor =
        switch (status.toUpperCase()) {
          case "SHIPPED" -> "#7c3aed";
          case "DELIVERED" -> "#059669";
          case "CANCELLED" -> "#dc2626";
          default -> "#1d4ed8";
        };

    String statusEmoji =
        switch (status.toUpperCase()) {
          case "SHIPPED" -> "🚚";
          case "DELIVERED" -> "✅";
          case "CANCELLED" -> "❌";
          default -> "📋";
        };

    String content =
        greeting(name)
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 16px;'>"
            + "Your order status has been updated.</p>"
            + "<table width='100%%' cellpadding='0' cellspacing='0' style='margin:0 0 16px;'>"
            + infoRow("Order Number", orderNumber)
            + infoRow("New Status", statusEmoji + " " + status)
            + "</table>"
            + (message != null ? alertBox(message, "#eff6ff", "#3b82f6") : "")
            + button(
                mailProperties.getFrontendUrl() + "/orders/" + orderNumber,
                "View Order",
                statusColor);

    return wrap(statusColor, content);
  }

  // ── 5. Account Deactivated ────────────────────────────────────────────────
  public String buildAccountDeactivatedEmail(String name) {
    String content =
        greeting(name)
            + "<p style='color:#374151;font-size:15px;line-height:1.6;margin:0 0 16px;'>"
            + "Your account has been deactivated. You will no longer be able to sign in.</p>"
            + alertBox(
                "If you believe this was a mistake, please contact our support team.",
                "#fef2f2",
                "#ef4444")
            + button(
                "mailto:support@"
                    + mailProperties.getFromName().toLowerCase().replace(" ", "")
                    + ".com",
                "Contact Support",
                "#dc2626");

    return wrap("#dc2626", content);
  }
}
