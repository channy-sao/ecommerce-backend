package ecommerce_app.util;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

import java.util.UUID;

public class ImageDownloadUtils {

  private static final HttpClient CLIENT =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

  private ImageDownloadUtils() {
    // prevent instantiation
  }

  public static String downloadAndSave(String imageUrl, String upload) throws Exception {

    // Generate unique filename
    String fileName = UUID.randomUUID() + ".jpg";

    Path path = Path.of(upload);
    if(!Files.exists(path)) {
      Files.createDirectories(path);
    }

    // Prepare request
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(imageUrl)).GET().build();

    // Send request
    HttpResponse<InputStream> response =
        CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

    // Save file
    Path filePath = path.resolve(fileName);
    Files.copy(response.body(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Return relative path for DB
    return upload + "/" + fileName;
  }
}
