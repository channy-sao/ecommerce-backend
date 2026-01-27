package ecommerce_app.constant.enums;

import lombok.Getter;

@Getter
public enum ExportFormat {
  EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
  PDF("pdf", "application/pdf");

  private final String extension;
  private final String contentType;

  ExportFormat(String extension, String contentType) {
    this.extension = extension;
    this.contentType = contentType;
  }

  public String getExtension() {
    return extension;
  }

  public String getContentType() {
    return contentType;
  }
}
