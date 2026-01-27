package ecommerce_app.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@Service
public class ExcelTemplateBuilder {

  /**
   * Generic method to create an Excel template.
   *
   * @param sheetName Name of the sheet
   * @param headers List of column headers
   * @param requiredColumns List of booleans indicating if the column is required
   * @param numberOfRows Number of sample rows to generate
   * @param fillSampleData Callback to populate sample data for a row (rowIndex starts at 1)
   * @param addDataValidation Callback to add column validations (optional)
   * @param createInstructions Callback to add instructions sheet (optional)
   * @return byte array representing the Excel file
   */
  public byte[] buildTemplate(
      String sheetName,
      List<String> headers,
      List<Boolean> requiredColumns,
      int numberOfRows,
      BiConsumer<Sheet, Integer> fillSampleData,
      Consumer<Sheet> addDataValidation,
      Consumer<XSSFWorkbook> createInstructions) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      XSSFSheet sheet = workbook.createSheet(sheetName);

      // Styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle requiredStyle = createRequiredHeaderStyle(workbook);

      // Header row
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers.get(i));
        cell.setCellStyle(
            Boolean.TRUE.equals(requiredColumns.get(i)) ? requiredStyle : headerStyle);
      }

      // Fill sample data
      if (numberOfRows > 0 && fillSampleData != null) {
        for (int i = 0; i < numberOfRows; i++) {
          fillSampleData.accept(sheet, i + 1); // Row index starts at 1
        }
      }

      // Data validation
      if (addDataValidation != null) {
        addDataValidation.accept(sheet);
      }

      // Instruction sheet
      if (createInstructions != null) {
        createInstructions.accept(workbook);
      }

      // Freeze header
      sheet.createFreezePane(0, 1);

      workbook.write(out);
      log.info("Generated Excel template: {}", sheetName);
      return out.toByteArray();

    } catch (IOException e) {
      log.error("Error generating Excel template: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  // ------------------ Styles -------------------

  private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private CellStyle createRequiredHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = createHeaderStyle(workbook);
    style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
    return style;
  }

  private CellStyle createSampleDataStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setWrapText(true);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }
}
