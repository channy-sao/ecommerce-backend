package ecommerce_app.config;

import ecommerce_app.util.JwtService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    String token = accessor.getFirstNativeHeader("Authorization");
    if (token == null || !token.startsWith("Bearer ")) return message;

    token = token.substring(7);
    String email = jwtService.getSubject(token);

    if (email != null && jwtService.isValidToken(token)) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      Authentication auth =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      accessor.setUser(auth);
    }

    return message;
  }
}
