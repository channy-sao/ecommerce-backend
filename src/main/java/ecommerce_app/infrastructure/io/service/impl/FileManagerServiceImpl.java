package ecommerce_app.infrastructure.io.service.impl;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.AppProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of {@link FileManagerService} that handles saving and deleting files on the local
 * file system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileManagerServiceImpl implements FileManagerService {
  private final AppProperty appProperty;

  /**
   * Saves the provided multipart file to the specified upload directory.
   *
   * @param file the multipart file to be saved
   * @param uploadDir the target directory where the file should be stored
   * @return the unique filename under which the file was saved
   * @throws IllegalArgumentException if the file is empty or has no valid filename
   * @throws BadRequestException if the file cannot be saved due to I/O error
   */
  @Override
  public String saveFile(MultipartFile file, String uploadDir) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Cannot save empty file.");
    }
    try {
      // Ensure the upload directory exists
      Path uploadPath = Paths.get(uploadDir);
      if (Files.notExists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // Generate unique file name
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null || originalFilename.isBlank()) {
        throw new IllegalArgumentException("Original filename is missing.");
      }
      String uniqueFileName = System.currentTimeMillis() + "_" + originalFilename;

      Path filePath = uploadPath.resolve(uniqueFileName);
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      log.info("File saved: {}", filePath);
      return uniqueFileName;

    } catch (IOException e) {
      log.error("Error saving file", e);
      throw new BadRequestException("Failed to save file");
    }
  }

  /**
   * Deletes the specified file from the given directory if it exists.
   *
   * @param uploadDir the directory containing the file
   * @param fileName the name of the file to delete
   * @throws IllegalArgumentException if the directory does not exist
   * @throws BadRequestException if the file cannot be deleted due to I/O error
   */
  @Override
  public void deleteFile(String uploadDir, String fileName) {
    // 1️⃣ Nothing to delete
    if (fileName == null || fileName.isBlank()) {
      log.warn("deleteFile called with null or empty fileName");
      return;
    }

    try {
      Path uploadPath = Paths.get(uploadDir);
      if (Files.notExists(uploadPath)) {
        log.warn("Upload path does not exist: {}", uploadDir);
        return;
      }
      Path filePath = uploadPath.resolve(fileName).normalize();
      // Delete safely
      boolean deleted = Files.deleteIfExists(filePath);

      if (!deleted) {
        log.warn("File not found, nothing deleted: {}", filePath);
      }
    } catch (IOException e) {
      log.error("Error deleting file", e);
      throw new BadRequestException("Failed to delete file");
    }
  }

  /**
   * Constructs the absolute file path by resolving the given file name within the specified upload
   * directory.
   *
   * @param uploadDir the directory where the file is or will be stored
   * @param fileName the name of the file to resolve
   * @return the absolute path of the file as a String
   */
  @Override
  public String getResourceUrl(String uploadDir, String fileName) {
    if (uploadDir.startsWith(".")) {
      uploadDir = uploadDir.substring(1);
    }
    return appProperty.getBaseUrl() + uploadDir + "/" + fileName;
  }

  /**
   * Extracts the file extension from the given file name.
   *
   * @param fileName the file name to extract extension from
   * @return the file extension including the dot (e.g., ".png"), or empty string if none found
   */
  private String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    return (dotIndex != -1) ? fileName.substring(dotIndex) : "";
  }
}
