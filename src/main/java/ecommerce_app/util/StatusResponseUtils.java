package ecommerce_app.util;

import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.dto.response.StatusResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class StatusResponseUtils {
  public static StatusResponse createStatusResponse(HttpStatusCode status, String message) {
    return new StatusResponse(
        (short) status.value(), message != null ? message : ResponseMessageConstant.FAILED);
  }
}