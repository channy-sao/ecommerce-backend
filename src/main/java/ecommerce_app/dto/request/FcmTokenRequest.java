// request/FcmTokenRequest.java
package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {
    @NotBlank(message = "FCM token cannot be blank")
    private String token;
}