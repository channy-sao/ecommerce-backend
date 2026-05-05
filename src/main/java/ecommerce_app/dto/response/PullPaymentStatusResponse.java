package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullPaymentStatusResponse {
  private String paymentId;
  private String transactionId;
  private String status; // PENDING, APPROVED, DECLINED, EXPIRED
  private String customerPhone;
  private String customerName;
  private String amount;
  private String currency;
  private String approvalTimestamp;
  private String rejectionReason;
  private String receiptUrl;
}
