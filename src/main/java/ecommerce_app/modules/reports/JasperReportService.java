package ecommerce_app.common.service;

import ecommerce_app.constant.enums.ExportFormat;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JasperReportService {

  /**
   * Generic method to export report in various formats
   *
   * @param data List of data objects
   * @param reportName Report template name (without .jrxml extension)
   * @param format Export format (EXCEL or PDF)
   * @param parameters Additional parameters for the report
   * @return byte array of generated report
   */
  public byte[] exportReport(
      List<?> data,
      String reportName,
      ExportFormat format,
      Map<String, Object> parameters,
      String sheetName) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Load .jrxml file from reports directory
      String reportPath = String.format("reports/%s.jrxml", reportName);
      InputStream reportStream = new ClassPathResource(reportPath).getInputStream();

      // Compile report
      JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

      // Prepare parameters (merge with defaults)
      Map<String, Object> reportParams = new HashMap<>();
      if (parameters != null) {
        reportParams.putAll(parameters);
      }

      // Data source
      JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

      // Fill report
      JasperPrint jasperPrint =
          JasperFillManager.fillReport(jasperReport, reportParams, dataSource);

      // Export based on format
      switch (format) {
        case EXCEL:
          exportToExcel(jasperPrint, outputStream, sheetName);
          break;
        case PDF:
          exportToPdf(jasperPrint, outputStream);
          break;
        default:
          throw new IllegalArgumentException("Unsupported export format: " + format);
      }

      log.info("Successfully exported report: {} as {}", reportName, format);
      return outputStream.toByteArray();

    } catch (Exception e) {
      log.error("Error generating Jasper report {}: {}", reportName, e.getMessage(), e);
      throw new RuntimeException("Failed to generate report: " + reportName, e);
    }
  }

  /** Overloaded method without custom parameters */
  public byte[] exportReport(
      List<?> data, String reportName, ExportFormat format, String sheetName) {
    return exportReport(data, reportName, format, null, sheetName);
  }

  private void exportToExcel(
      JasperPrint jasperPrint, ByteArrayOutputStream outputStream, String sheetName)
      throws JRException {
    JRXlsxExporter exporter = new JRXlsxExporter();
    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

    SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();

    // Basic settings
    configuration.setDetectCellType(true);
    configuration.setWhitePageBackground(false);

    // Layout settings
    configuration.setRemoveEmptySpaceBetweenRows(true);
    configuration.setRemoveEmptySpaceBetweenColumns(true);
    configuration.setCollapseRowSpan(false);

    // Content settings
    configuration.setIgnoreGraphics(true);
    configuration.setIgnorePageMargins(true);
    configuration.setOnePagePerSheet(false);

    // Cell settings
    configuration.setWrapText(false);
    configuration.setCellLocked(false);
    configuration.setCellHidden(false);

    // Sheet settings
    configuration.setSheetNames(new String[] {sheetName});
    configuration.setFreezeRow(2); // Freeze header row

    // Column width
    configuration.setAutoFitPageHeight(false);
    configuration.setForcePageBreaks(false);
    configuration.setShrinkToFit(false);

    // Font settings
    configuration.setFontSizeFixEnabled(true);
    configuration.setImageBorderFixEnabled(false);

    exporter.setConfiguration(configuration);
    exporter.exportReport();
  }

  private void exportToPdf(JasperPrint jasperPrint, ByteArrayOutputStream outputStream)
      throws JRException {
    JRPdfExporter exporter = new JRPdfExporter();
    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

    SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
    configuration.setMetadataAuthor("E-Commerce System");
    configuration.setMetadataCreator("JasperReports");

    exporter.setConfiguration(configuration);
    exporter.exportReport();
  }
}
