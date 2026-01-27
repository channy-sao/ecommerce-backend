package ecommerce_app.core;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface IExcelTempleBuilder {
  void fillSampleData(Sheet sheet, int rowIndex, List<Long> rangeIds);

  void addValidation(Sheet sheet, int numberOfRows, List<Long> rangeIds);

  static void addInstructions(XSSFWorkbook workbook, String instruction) {
    Sheet instructionSheet = workbook.createSheet("Instructions");
    Row row = instructionSheet.createRow(0);
    row.createCell(0).setCellValue(instruction);
  }
}
