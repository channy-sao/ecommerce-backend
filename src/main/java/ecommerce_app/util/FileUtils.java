package ecommerce_app.util;

import ecommerce_app.infrastructure.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
  public static void validateExcelFile(MultipartFile file) {
    if (file.isEmpty() || file.getSize() <= 0) {
      log.error("Excel file is empty");
      throw new BadRequestException("Excel file is empty");
    }
    if (!Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename()))
        .equalsIgnoreCase("xlsx")) {
      log.error("Excel file extension is not supported");
      throw new BadRequestException("Excel file extension not supported");
    }
  }
}
