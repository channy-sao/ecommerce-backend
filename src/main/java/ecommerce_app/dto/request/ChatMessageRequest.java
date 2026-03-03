package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
  @NotBlank(message = "Message content cannot be blank")
  @Size(max = 2000, message = "Message cannot exceed 2000 characters")
  private String content;
}
