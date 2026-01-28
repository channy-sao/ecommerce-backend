package ecommerce_app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import ecommerce_app.infrastructure.exception.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

@Configuration
@Slf4j
public class FirebaseConfig {

  @Bean
  public FirebaseApp initializeFirebase() throws IOException {
    try {
      // Load from classpath
      ClassPathResource resource = new ClassPathResource("firebase/flutter-auth.json");

      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
              .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
        log.info("Firebase has been initialized successfully.");
        return firebaseApp;
      }
      return FirebaseApp.getInstance();
    } catch (IOException e) {
      log.error("Error initializing Firebase: {}", e.getMessage());
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize Firebase");
    }
  }

  @Bean
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
