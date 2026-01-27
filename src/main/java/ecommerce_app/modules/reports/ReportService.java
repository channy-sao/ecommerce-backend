package ecommerce_app.modules.reports;

import ecommerce_app.constant.enums.ExportFormat;

public interface ReportService<T> {
  /**
   * Export data as report
   *
   * @param format Export format
   * @return byte array of report
   */
  byte[] export(ExportFormat format);

  /**
   * Get report template name
   *
   * @return template name without extension
   */
  String getReportTemplateName();

  /**
   * Get base filename for export
   *
   * @return base filename
   */
  String getBaseFilename();
}
