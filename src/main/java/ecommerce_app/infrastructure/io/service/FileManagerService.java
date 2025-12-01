package ecommerce_app.infrastructure.io.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileManagerService {
    String saveFile(MultipartFile file, String uploadDir);
    void deleteFile(String uploadDir, String fileName);
    String getResourceUrl(String uploadDir, String fileName);
}
