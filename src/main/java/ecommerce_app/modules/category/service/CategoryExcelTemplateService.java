package ecommerce_app.modules.category.service;

import com.github.javafaker.Faker;
import ecommerce_app.core.IExcelTempleBuilder;
import ecommerce_app.util.ExcelTemplateBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryExcelTemplateService implements IExcelTempleBuilder {
  private final ExcelTemplateBuilder excelTemplateBuilder;
  private final Faker faker = new Faker();

  public byte[] generateCategoryExcelTemplate(int numberOfRows) {
    List<String> headers = List.of("Name*", "Description");
    List<Boolean> required = List.of(true, false);
    return excelTemplateBuilder.buildTemplate(
        "Category",
        headers,
        required,
        numberOfRows,
        (sheet, rowIndex) -> fillSampleData(sheet, rowIndex, null),
        sheet -> addValidation(sheet, numberOfRows, null),
        this::addInstructions);
  }

  @Override
  public void fillSampleData(Sheet sheet, int rowIndex, List<Long> rangeIds) {
    Row row = sheet.createRow(rowIndex);
    String name = faker.company().name();
    row.createCell(0).setCellValue(name);
    row.createCell(1).setCellValue(faker.lorem().sentence(10));
  }

  @Override
  public void addValidation(Sheet sheet, int numberOfRows, List<Long> rangeIds) {}

  private void addInstructions(XSSFWorkbook workbook) {
    String instruction = "Please fill in the category details. Fields marked with * are mandatory.";
    IExcelTempleBuilder.addInstructions(workbook, instruction);
  }
}
