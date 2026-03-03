package ecommerce_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceAppApplication {

  public static void main(String[] args) {
    System.setProperty("GOOGLE_API_USE_CLIENT_CERTIFICATE", "false");
    SpringApplication.run(EcommerceAppApplication.class, args);
  }
}
