package ecommerce_app.modules.product.service;

import com.github.javafaker.Faker;
import ecommerce_app.core.IExcelTempleBuilder;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.category.repository.CategoryRepository;
import ecommerce_app.util.ExcelTemplateBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProductExcelTemplateService implements IExcelTempleBuilder {

  private final ExcelTemplateBuilder builder;
  private final Faker faker = new Faker();
  private final CategoryRepository categoryRepository;

  public byte[] generateTemplate(int numberOfRows) {
    List<String> headers =
        List.of("Name*", "Description", "Price*", "Image URL", "Category ID*", "Is Feature");
    List<Boolean> required = List.of(true, false, true, false, true, false);
    List<Long> categoryIds = categoryRepository.findAll().stream().map(Category::getId).toList();

    return builder.buildTemplate(
        "Products",
        headers,
        required,
        numberOfRows,
        (sheet, rowIndex) -> fillSampleData(sheet, rowIndex, categoryIds),
        sheet -> addValidation(sheet, numberOfRows, categoryIds),
        this::addInstructions);
  }

  public void fillSampleData(Sheet sheet, int rowIndex, List<Long> categoryIds) {
    Row row = sheet.createRow(rowIndex);
    String name = faker.commerce().productName();
    row.createCell(0).setCellValue(name);
    row.createCell(1).setCellValue(faker.lorem().sentence(10));
    row.createCell(2).setCellValue(faker.number().randomDouble(2, 10, 3000));
    row.createCell(3)
        .setCellValue("https://via.placeholder.com/800x600?text=" + name.replace(" ", "+"));
    row.createCell(4)
        .setCellValue(
            categoryIds.isEmpty() ? 1 : categoryIds.get(new Random().nextInt(categoryIds.size())));
    row.createCell(5).setCellValue(new Random().nextBoolean() ? "TRUE" : "FALSE");
  }

  public void addValidation(Sheet sheet, int numberOfRows, List<Long> categoryIds) {
    DataValidationHelper helper = sheet.getDataValidationHelper();

    // TRUE/FALSE dropdown
    CellRangeAddressList range = new CellRangeAddressList(1, numberOfRows, 5, 5);
    DataValidationConstraint constraint =
        helper.createExplicitListConstraint(new String[] {"TRUE", "FALSE"});
    DataValidation validation = helper.createValidation(constraint, range);
    validation.setShowErrorBox(true);
    sheet.addValidationData(validation);
  }

  private void addInstructions(XSSFWorkbook workbook) {
    IExcelTempleBuilder.addInstructions(
        workbook,
"""
  added Instructions:
  1. Fill in the product details in the 'Products' sheet.
  2. Required fields are marked with an asterisk (*).
  3. For 'Is Feature', select either TRUE or FALSE from the dropdown.
  4. Ensure that the 'Category ID' corresponds to an existing category in the system.
""");
  }
}
